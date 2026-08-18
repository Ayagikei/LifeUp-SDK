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
  const host = pickIpv4(input.addresses) ?? input.addresses?.find(Boolean)
  if (!host) return null
  return { host, port, name }
}

function pickIpv4(addresses?: Array<string | undefined>): string | undefined {
  return addresses?.find((address) => address != null && /^\d{1,3}(\.\d{1,3}){3}$/.test(address))
}

export async function discoverCloud(timeoutMs = 5000): Promise<CloudEndpoint[]> {
  const Bonjour = (await import("bonjour-service")).default
  const bonjour = new Bonjour()
  const found = new Map<string, CloudEndpoint>()

  return await new Promise((resolve) => {
    const browser = bonjour.find({ type: "lifeup", protocol: "tcp" }, (service) => {
      const endpoint = parseCloudService({
        name: service.name,
        addresses: service.addresses,
        txt: service.txt as Record<string, string | undefined> | undefined,
      })
      if (endpoint) found.set(`${endpoint.host}:${endpoint.port}`, endpoint)
    })

    const finish = () => {
      clearTimeout(timer)
      try {
        browser.stop()
      } catch {
        // ignore
      }
      try {
        bonjour.destroy()
      } catch {
        // ignore
      }
      resolve([...found.values()])
    }

    const timer = setTimeout(finish, timeoutMs)
  })
}

