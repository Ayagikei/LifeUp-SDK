import { LifeUpClient } from "./client.js"
import {
  envHost,
  loadConfig,
  saveConfig,
  parseHostInput,
  resolveToken,
  tokenToPersist,
} from "./config.js"
import { discoverCloud, type CloudEndpoint } from "./discover.js"

export type SessionStatus = {
  connected: boolean
  host?: string
  port?: number
  name?: string
  tokenSet: boolean
  lastError?: string
}

export function withDiscoveredName(parsed: CloudEndpoint, discovered: CloudEndpoint[]): CloudEndpoint {
  return discovered.find((item) => item.host === parsed.host && item.port === parsed.port) ?? parsed
}

export class Session {
  client: LifeUpClient | undefined
  lastError: string | undefined
  private endpoint: CloudEndpoint | undefined
  private discovered: CloudEndpoint[] = []

  status(): SessionStatus {
    return {
      connected: this.client != null,
      host: this.client?.host,
      port: this.client?.port,
      name: this.endpoint?.name,
      tokenSet: Boolean(this.client?.token),
      lastError: this.lastError,
    }
  }

  rememberDiscover(found: CloudEndpoint[]): void {
    this.discovered = found
  }

  requireClient(): LifeUpClient {
    if (!this.client) {
      throw new Error("Not connected. Call discover or connect first.")
    }
    return this.client
  }

  async connect(input: { host?: string; token?: string } = {}): Promise<CloudEndpoint> {
    const saved = await loadConfig()
    const endpoint = await this.resolveEndpoint(input.host, saved)
    const token = resolveToken(input, saved, endpoint)
    const client = new LifeUpClient(endpoint.host, endpoint.port, token)
    try {
      await client.get("/info")
    } catch (error) {
      this.lastError = error instanceof Error ? error.message : String(error)
      throw error
    }
    this.client = client
    this.endpoint = endpoint
    this.lastError = undefined
    await saveConfig({
      host: endpoint.host,
      port: endpoint.port,
      token: tokenToPersist(input, saved, endpoint),
    })
    return endpoint
  }

  private async resolveEndpoint(
    host: string | undefined,
    saved: { host?: string; port?: number },
  ): Promise<CloudEndpoint> {
    if (host) return withDiscoveredName(parseHostInput(host), this.discovered)
    const fromEnv = envHost()
    if (fromEnv) return withDiscoveredName(fromEnv, this.discovered)
    if (saved.host && saved.port) {
      return withDiscoveredName({ host: saved.host, port: saved.port, name: "saved" }, this.discovered)
    }
    const found = await discoverCloud()
    this.rememberDiscover(found)
    if (found.length === 1) return found[0]
    if (found.length > 1) {
      throw new Error(
        `Multiple LifeUp Cloud instances: ${found.map((item) => `${item.host}:${item.port} (${item.name})`).join(", ")}. Pass host.`,
      )
    }
    throw new Error("No LifeUp Cloud found. Pass host like 192.168.1.8:13276, or set LIFEUP_HOST.")
  }
}
