import { WebSocket } from "undici"
import { assertRequestTimeoutMs, resolveRequestTimeoutMs } from "./config.js"
import { buildLifeUpUrl, type LifeUpParamValue } from "./lifeup-url.js"
import type { CallVia } from "./results.js"
export const SUCCESS = 200
export const LIFEUP_NOT_RUNNING = 10001
export const CONTENT_PROVIDER_ERROR = 10002
export const CLOUD_BUSY = 10003

/** Opt-in Cloud admission header. One value, attached to every Cloud HTTP request. */
export const LIFEUP_CONCURRENCY_LIMITED = {
  name: "X-LifeUp-Concurrency",
  value: "limited",
} as const

const CLOUD_BUSY_LEAD = "LifeUp Cloud is busy; request not started. Retry later"
const CLOUD_BUSY_MESSAGE = `${CLOUD_BUSY_LEAD}.`
const CLOUD_BUSY_COMMAND_MESSAGE = `${CLOUD_BUSY_LEAD}; the command was not executed.`

export type Envelope<T> = {
  code: number
  message: string
  data: T | null
}

export type CloudEvent = {
  id: number
  time: number
  action: string
  extras: Record<string, string>
}

export type EventsPage = {
  latestId: number
  eventWs: boolean
  events: CloudEvent[]
  broadcasts?: boolean
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

export function decodeEnvelope<T>(
  status: number,
  body: string,
  method: "GET" | "POST" = "GET",
): Envelope<T> {
  if (status === 401) {
    throw new LifeUpHttpError("token required or invalid", 401)
  }
  if (status === 503) {
    const busy = cloudBusyError(body, method)
    if (busy) throw busy
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

function cloudBusyError(body: string, method: "GET" | "POST"): LifeUpHttpError | undefined {
  let parsed: unknown
  try {
    parsed = JSON.parse(body)
  } catch {
    return undefined
  }
  if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) return undefined
  if ((parsed as { code?: unknown }).code !== CLOUD_BUSY) return undefined
  const message = method === "POST" ? CLOUD_BUSY_COMMAND_MESSAGE : CLOUD_BUSY_MESSAGE
  return new LifeUpHttpError(message, 503, CLOUD_BUSY)
}

export type FetchLike = (
  input: string,
  init?: { method?: string; headers?: Record<string, string>; body?: string; signal?: AbortSignal },
) => Promise<{
  status: number
  text(): Promise<string>
  headers?: { get(name: string): string | null }
}>

function formatTimeoutBudget(ms: number): string {
  const whole = Math.trunc(ms / 1000)
  const fraction = ms % 1000
  if (fraction === 0) return `${whole}s`
  return `${whole}.${String(fraction).padStart(3, "0").replace(/0+$/, "")}s`
}

function timeoutMessage(method: string, path: string, timeoutMs: number): string {
  const base = `${method} ${path} timed out after ${formatTimeoutBudget(timeoutMs)}`
  return method === "GET" ? base : `${base}; outcome unknown, check state before retry`
}

function isTimeoutFailure(error: unknown): boolean {
  if (!(error instanceof Error)) return false
  if (error.name === "TimeoutError" || error.name === "AbortError") return true
  const cause = (error as { cause?: unknown }).cause
  return cause instanceof Error && (cause.name === "TimeoutError" || cause.name === "AbortError")
}

function raceAbort<T>(work: Promise<T>, signal: AbortSignal): Promise<T> {
  return new Promise((resolve, reject) => {
    const onAbort = () => reject(abortReason(signal))
    if (signal.aborted) onAbort()
    else signal.addEventListener("abort", onAbort, { once: true })
    work.then(
      (value) => {
        signal.removeEventListener("abort", onAbort)
        resolve(value)
      },
      (error: unknown) => {
        signal.removeEventListener("abort", onAbort)
        reject(error)
      },
    )
  })
}

function abortReason(signal: AbortSignal): Error {
  if (signal.reason instanceof Error) return signal.reason
  const error = new Error("The operation timed out")
  error.name = "TimeoutError"
  return error
}

type Deadline = {
  signal: AbortSignal
  guard<T>(work: Promise<T>): Promise<T>
  close(): void
}

function startDeadline(method: string, url: string, timeoutMs: number): Deadline {
  const pathname = new URL(url).pathname
  const controller = new AbortController()
  const timer = setTimeout(() => {
    const reason = new Error("The operation timed out")
    reason.name = "TimeoutError"
    controller.abort(reason)
  }, timeoutMs)
  let closed = false
  const close = () => {
    if (closed) return
    closed = true
    clearTimeout(timer)
  }
  const message = timeoutMessage(method, pathname, timeoutMs)
  return {
    signal: controller.signal,
    close,
    async guard<T>(work: Promise<T>): Promise<T> {
      try {
        return await raceAbort(work, controller.signal)
      } catch (error) {
        close()
        if (isTimeoutFailure(error)) throw new LifeUpHttpError(message)
        throw error
      }
    },
  }
}

type GuardedResponse = {
  status: number
  headers?: { get(name: string): string | null }
  text: () => Promise<string>
  finish: () => void
}
// ponytail: FIFO cap 32; key by full URL and raise the cap if one session outgrows it
const RESOURCE_CACHE_MAX = 32

type CachedGet = { etag: string; data: unknown }

export class LifeUpClient {
  private readonly resourceCache = new Map<string, CachedGet>()
  // Key is this client's token plus the full href (path and query). Joiners share the
  // first caller's promise, so they also share that caller's deadline and do not start
  // a second timer or fetch. The entry is removed when the promise settles. ETag and
  // 304 handling stay inside the shared call.
  private readonly inflightGets = new Map<string, Promise<unknown>>()
  private readonly timeoutMs: number
  constructor(
    readonly host: string,
    readonly port: number,
    readonly token: string | undefined,
    private readonly fetchImpl: FetchLike = fetch,
    timeoutMs: number = resolveRequestTimeoutMs(),
  ) {
    this.timeoutMs = assertRequestTimeoutMs(timeoutMs)
  }

  get requestTimeoutMs(): number {
    return this.timeoutMs
  }

  get baseUrl(): string {
    return `http://${this.host}:${this.port}`
  }

  private headers(): Record<string, string> {
    const headers: Record<string, string> = { Accept: "application/json" }
    if (this.token) headers.Authorization = this.token
    return headers
  }

  private async send(
    url: string,
    init: { method?: string; headers?: Record<string, string>; body?: string },
  ): Promise<GuardedResponse> {
    const headers = {
      ...(init.headers ?? {}),
      [LIFEUP_CONCURRENCY_LIMITED.name]: LIFEUP_CONCURRENCY_LIMITED.value,
    }
    const deadline = startDeadline(init.method ?? "GET", url, this.timeoutMs)
    try {
      const response = await deadline.guard(this.fetchImpl(url, { ...init, headers, signal: deadline.signal }))
      return {
        status: response.status,
        headers: response.headers,
        text: () => deadline.guard(response.text()),
        finish: () => deadline.close(),
      }
    } catch (error) {
      deadline.close()
      throw error
    }
  }

  private remember(url: string, etag: string, data: unknown) {
    if (!this.resourceCache.has(url) && this.resourceCache.size >= RESOURCE_CACHE_MAX) {
      const oldest = this.resourceCache.keys().next().value
      if (oldest !== undefined) this.resourceCache.delete(oldest)
    }
    this.resourceCache.set(url, { etag, data })
  }

  private async readGet<T>(url: string, response: GuardedResponse): Promise<T> {
    const body = await response.text()
    const envelope = decodeEnvelope<T>(response.status, body, "GET")
    const data = envelope.data as T
    const etag = response.headers?.get("etag")
    if (etag) this.remember(url, etag, data)
    else this.resourceCache.delete(url)
    return data
  }

  private hrefFor(path: string, query: Record<string, string | number | number[] | undefined>): string {
    const url = new URL(path, this.baseUrl)
    for (const [key, value] of Object.entries(query)) {
      if (value === undefined) continue
      if (Array.isArray(value)) {
        for (const item of value) url.searchParams.append(key, String(item))
      } else {
        url.searchParams.append(key, String(value))
      }
    }
    return url.toString()
  }

  private inflightGetKey(href: string): string {
    return `${this.token ?? ""}\0${href}`
  }

  async get<T>(path: string, query: Record<string, string | number | number[] | undefined> = {}): Promise<T> {
    const href = this.hrefFor(path, query)
    const key = this.inflightGetKey(href)
    const existing = this.inflightGets.get(key)
    if (existing) return existing as Promise<T>

    const pending = this.fetchGet<T>(href)
    this.inflightGets.set(key, pending)
    const forget = () => {
      if (this.inflightGets.get(key) === pending) this.inflightGets.delete(key)
    }
    pending.then(forget, forget)
    return pending
  }

  private async fetchGet<T>(href: string): Promise<T> {
    const cached = this.resourceCache.get(href)
    const headers = this.headers()
    if (cached) headers["If-None-Match"] = cached.etag
    const response = await this.send(href, { headers })
    try {
      if (response.status === 304) {
        if (cached) return cached.data as T
        const fresh = await this.send(href, { headers: this.headers() })
        try {
          return await this.readGet(href, fresh)
        } finally {
          fresh.finish()
        }
      }
      return await this.readGet(href, response)
    } finally {
      response.finish()
    }
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
    const response = await this.send(`${this.baseUrl}${path}`, {
      method: "POST",
      headers: { ...this.headers(), "Content-Type": "application/json" },
      body: JSON.stringify({ urls }),
    })
    try {
      const body = await response.text()
      if (via === "launch") {
        decodeEnvelope(response.status, body, "POST")
        return urls.map((url) => ({ url, result: null }))
      }
      const envelope = decodeEnvelope<CallUrlResult[]>(response.status, body, "POST")
      return envelope.data ?? []
    } finally {
      response.finish()
    }
  }


  async listEvents(after = 0, limit = 50): Promise<EventsPage> {
    return this.get<EventsPage>("/events", { after, limit })
  }

  subscribeEvents(after: number, onEvent: (event: CloudEvent) => void): { close: () => void } {
    const url = new URL("/events", this.baseUrl.replace(/^http/, "ws"))
    url.searchParams.set("after", String(after))
    if (this.token) url.searchParams.set("token", this.token)
    const socket = new WebSocket(url)
    socket.addEventListener("message", (message) => {
      if (typeof message.data !== "string" || !message.data.startsWith("{")) return
      onEvent(JSON.parse(message.data) as CloudEvent)
    })
    return { close: () => socket.close() }
  }
}
