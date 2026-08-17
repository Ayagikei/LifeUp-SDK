import { chmod, mkdir, readFile, writeFile } from "node:fs/promises"
import { homedir } from "node:os"
import { dirname, join } from "node:path"
import type { CloudEndpoint } from "./discover.js"

export type PersistedConfig = {
  host?: string
  port?: number
  token?: string
}

export function configPath(): string {
  return process.env.LIFEUP_MCP_CONFIG ?? join(homedir(), ".lifeup-mcp.json")
}

export function parseHostInput(input: string): CloudEndpoint {
  const trimmed = input.trim()
  const withScheme = trimmed.includes("://") ? trimmed : `http://${trimmed}`
  const url = new URL(withScheme)
  const port = url.port ? Number(url.port) : 13276
  if (!url.hostname || !Number.isInteger(port)) {
    throw new Error(`Invalid LIFEUP_HOST: ${input}`)
  }
  return { host: url.hostname, port, name: "manual" }
}

export async function loadConfig(): Promise<PersistedConfig> {
  try {
    return JSON.parse(await readFile(configPath(), "utf8")) as PersistedConfig
  } catch {
    return {}
  }
}

export async function saveConfig(config: PersistedConfig): Promise<void> {
  const path = configPath()
  await mkdir(dirname(path), { recursive: true })
  await writeFile(path, `${JSON.stringify(config, null, 2)}\n`, { encoding: "utf8", mode: 0o600 })
  await chmod(path, 0o600)
}

export function envHost(): CloudEndpoint | undefined {
  const raw = process.env.LIFEUP_HOST
  return raw ? parseHostInput(raw) : undefined
}

export function envToken(): string | undefined {
  return process.env.LIFEUP_TOKEN || undefined
}

export function boundToken(config: PersistedConfig, endpoint: CloudEndpoint): string | undefined {
  if (!config.token) return undefined
  if (config.host !== endpoint.host || config.port !== endpoint.port) return undefined
  return config.token
}

export function resolveToken(
  input: { token?: string },
  saved: PersistedConfig,
  endpoint: CloudEndpoint,
): string | undefined {
  if (input.token !== undefined) return input.token || undefined
  return envToken() ?? boundToken(saved, endpoint)
}

export function tokenToPersist(
  input: { token?: string },
  saved: PersistedConfig,
  endpoint: CloudEndpoint,
): string | undefined {
  if (input.token !== undefined) return input.token || undefined
  return boundToken(saved, endpoint)
}


