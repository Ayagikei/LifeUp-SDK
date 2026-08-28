import { spawn } from "node:child_process"

export type CloudEndpoint = { host: string; port: number; name: string }

export function parseCloudService(input: {
  name?: string
  addresses?: Array<string | undefined>
  txt?: Record<string, string | undefined> | null
}): CloudEndpoint | null {
  const name = input.name ?? ""
  if (!name.includes("lifeup_cloud")) return null
  const portText = input.txt?.port
  const port = Number(portText)
  if (!portText || !Number.isInteger(port) || port <= 0) return null
  const host = pickIpv4(input.addresses) ?? pickIpv4([input.txt?.ipv4]) ?? input.addresses?.find(Boolean)
  if (!host) return null
  return { host, port, name }
}

function pickIpv4(addresses?: Array<string | undefined>): string | undefined {
  return addresses?.find((address) => address != null && /^\d{1,3}(\.\d{1,3}){3}$/.test(address))
}

function remember(found: Map<string, CloudEndpoint>, endpoint: CloudEndpoint | null): void {
  if (endpoint) found.set(`${endpoint.host}:${endpoint.port}`, endpoint)
}

export async function discoverCloud(timeoutMs = 8000): Promise<CloudEndpoint[]> {
  const found = await discoverBonjour(timeoutMs)
  if (found.length > 0 || process.platform !== "darwin") return found
  return discoverDnsSd(Math.min(timeoutMs, 6000))
}

async function discoverBonjour(timeoutMs: number): Promise<CloudEndpoint[]> {
  const Bonjour = (await import("bonjour-service")).default
  const bonjour = new Bonjour()
  const found = new Map<string, CloudEndpoint>()

  return await new Promise((resolve) => {
    const take = (service: { name?: string; addresses?: string[]; txt?: Record<string, string | undefined> }) => {
      remember(found, parseCloudService({
        name: service.name,
        addresses: service.addresses,
        txt: service.txt,
      }))
    }
    const browser = bonjour.find({ type: "lifeup", protocol: "tcp" }, take)
    browser.on("up", take)
    browser.on("txt-update", take)
    browser.on("srv-update", take)

    const finish = () => {
      clearTimeout(timer)
      try { browser.stop() } catch { /* ignore */ }
      try { bonjour.destroy() } catch { /* ignore */ }
      resolve([...found.values()])
    }
    const timer = setTimeout(finish, timeoutMs)
  })
}

/** macOS: system mDNS often sees Cloud when bonjour-service does not (multi-homed / corporate LAN). */
export async function discoverDnsSd(timeoutMs = 6000): Promise<CloudEndpoint[]> {
  const browse = await runTimed("dns-sd", ["-B", "_lifeup._tcp", "local."], Math.min(2500, timeoutMs))
  const names = [...new Set([...browse.matchAll(/\s_lifeup\._tcp\.\s+(\S*lifeup_cloud\S*)/g)].map((m) => m[1]))]
  if (names.length === 0 && /lifeup_cloud/.test(browse)) names.push("lifeup_cloud")
  if (names.length === 0) return []
  const found = new Map<string, CloudEndpoint>()
  for (const name of names) {
    const lookup = await runTimed("dns-sd", ["-L", name, "_lifeup._tcp", "local."], 2500)
    const hostMatch = lookup.match(/can be reached at (\S+)\.:(\d+)/)
    const portMatch = lookup.match(/\bport=(\d+)/)
    if (!hostMatch || !portMatch) continue
    const hostname = hostMatch[1].replace(/\.$/, "")
    const httpPort = Number(portMatch[1])
    const resolved = await runTimed("dns-sd", ["-G", "v4", hostname], 2500)
    const ipv4 = resolved.match(/\b(\d{1,3}(?:\.\d{1,3}){3})\b/)
    if (!ipv4 || !Number.isInteger(httpPort) || httpPort <= 0) continue
    remember(found, { host: ipv4[1], port: httpPort, name })
  }
  return [...found.values()]
}

function runTimed(cmd: string, args: string[], ms: number): Promise<string> {
  return new Promise((resolve) => {
    const child = spawn(cmd, args, { stdio: ["ignore", "pipe", "ignore"] })
    let out = ""
    const timer = setTimeout(() => {
      child.kill("SIGTERM")
    }, ms)
    child.stdout?.setEncoding("utf8")
    child.stdout?.on("data", (chunk: string) => { out += chunk })
    child.on("close", () => {
      clearTimeout(timer)
      resolve(out)
    })
    child.on("error", () => {
      clearTimeout(timer)
      resolve(out)
    })
  })
}
