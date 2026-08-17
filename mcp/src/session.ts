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
  tokenSet: boolean
  lastError?: string
}

export class Session {
  client: LifeUpClient | undefined
  lastError: string | undefined

  status(): SessionStatus {
    return {
      connected: this.client != null,
      host: this.client?.host,
      port: this.client?.port,
      tokenSet: Boolean(this.client?.token),
      lastError: this.lastError,
    }
  }

  requireClient(): LifeUpClient {
    if (!this.client) {
      throw new Error("Not connected. Call lifeup_connect or lifeup_discover first.")
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
    if (host) return parseHostInput(host)
    const fromEnv = envHost()
    if (fromEnv) return fromEnv
    if (saved.host && saved.port) {
      return { host: saved.host, port: saved.port, name: "saved" }
    }
    const found = await discoverCloud()
    if (found.length === 1) return found[0]
    if (found.length > 1) {
      throw new Error(
        `Multiple LifeUp Cloud instances: ${found.map((item) => `${item.host}:${item.port}`).join(", ")}. Pass host.`,
      )
    }
    throw new Error("No LifeUp Cloud found. Pass host like 192.168.1.8:13276, or set LIFEUP_HOST.")
  }
}
