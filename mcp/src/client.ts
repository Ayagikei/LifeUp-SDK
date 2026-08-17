import { buildLifeUpUrl, type LifeUpParamValue } from "./lifeup-url.js"
import type { CallVia } from "./results.js"


export const SUCCESS = 200
export const LIFEUP_NOT_RUNNING = 10001
export const CONTENT_PROVIDER_ERROR = 10002


export type Envelope<T> = {
  code: number
  message: string
  data: T | null
}

export type CallUrlResult = {
  url: string
  result: unknown
}

export class LifeUpHttpError extends Error {
  constructor(
    message: string,
    readonly status?: number,
    readonly code?: number,
  ) {
    super(message)
  }
}

export function decodeEnvelope<T>(status: number, body: string): Envelope<T> {
  if (status === 401) {
    throw new LifeUpHttpError("token required or invalid", 401)
  }
  if (status < 200 || status >= 300) {
    throw new LifeUpHttpError(`HTTP ${status}`, status)
  }
  let parsed: Envelope<T>
  try {
    parsed = JSON.parse(body) as Envelope<T>
  } catch {
    throw new LifeUpHttpError("Non-JSON response from Cloud (wrong host or captive portal)", status)
  }
  if (parsed.code === LIFEUP_NOT_RUNNING) {
    throw new LifeUpHttpError(
      "LifeUp is not running, or Read LifeUp Data is not granted",
      status,
      parsed.code,
    )
  }
  if (parsed.code === CONTENT_PROVIDER_ERROR) {
    throw new LifeUpHttpError(parsed.message || "ContentProvider query failed", status, parsed.code)
  }
  if (parsed.code !== SUCCESS) {
    throw new LifeUpHttpError(parsed.message || `LifeUp error ${parsed.code}`, status, parsed.code)
  }
  return parsed
}

export type FetchLike = (
  input: string,
  init?: { method?: string; headers?: Record<string, string>; body?: string; signal?: AbortSignal },
) => Promise<{
  status: number
  text(): Promise<string>
}>

const REQUEST_TIMEOUT_MS = 10_000

export class LifeUpClient {
  constructor(
    readonly host: string,
    readonly port: number,
    readonly token: string | undefined,
    private readonly fetchImpl: FetchLike = fetch,
  ) {}

  get baseUrl(): string {
    return `http://${this.host}:${this.port}`
  }

  private headers(): Record<string, string> {
    const headers: Record<string, string> = { Accept: "application/json" }
    if (this.token) headers.Authorization = this.token
    return headers
  }

  private async request(url: string, init: { method?: string; headers?: Record<string, string>; body?: string }) {
    const signal = AbortSignal.timeout(REQUEST_TIMEOUT_MS)
    try {
      return await this.fetchImpl(url, { ...init, signal })
    } catch (error) {
      if (error instanceof Error && (error.name === "TimeoutError" || error.name === "AbortError")) {
        throw new LifeUpHttpError("Cloud timed out (10s). Check phone/Wi-Fi, then lifeup_connect again.")
      }
      throw error
    }
  }

  async get<T>(path: string, query: Record<string, string | number | number[] | undefined> = {}): Promise<T> {
    const url = new URL(path, this.baseUrl)
    for (const [key, value] of Object.entries(query)) {
      if (value === undefined) continue
      if (Array.isArray(value)) {
        for (const item of value) url.searchParams.append(key, String(item))
      } else {
        url.searchParams.append(key, String(value))
      }
    }

    const response = await this.request(url.toString(), { headers: this.headers() })
    const body = await response.text()
    const envelope = decodeEnvelope<T>(response.status, body)
    return envelope.data as T
  }

  async callApi(
    method: string,
    params: Record<string, LifeUpParamValue | undefined> = {},
    via: CallVia = "contentprovider",
  ): Promise<CallUrlResult[]> {
    return this.callApis([buildLifeUpUrl(method, params)], via)
  }

  async callApis(urls: string[], via: CallVia = "contentprovider"): Promise<CallUrlResult[]> {

    const path = via === "launch" ? "/api" : "/api/contentprovider"
    const response = await this.request(`${this.baseUrl}${path}`, {
      method: "POST",
      headers: { ...this.headers(), "Content-Type": "application/json" },
      body: JSON.stringify({ urls }),
    })
    const body = await response.text()
    if (via === "launch") {
      decodeEnvelope(response.status, body)
      return urls.map((url) => ({ url, result: null }))
    }
    const envelope = decodeEnvelope<CallUrlResult[]>(response.status, body)
    return envelope.data ?? []
  }


}
