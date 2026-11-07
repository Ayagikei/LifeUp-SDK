import { readFileSync } from "node:fs"
import { z } from "zod"
import { LifeUpHttpError } from "./client.js"

const { version: MCP_VERSION } = JSON.parse(readFileSync(new URL("../package.json", import.meta.url), "utf8")) as {
  version: string
}

export type DiagnosticsCloudErrorCategory = "timeout" | "unauthorized" | "unsupported" | "unavailable"

export type DiagnosticsCloudError = {
  category: DiagnosticsCloudErrorCategory
  status?: number
}

export type CachedCloudInfo = {
  appVersion?: number
  appVersionName?: string
  apiVersion?: number
  cloudVersion?: number
  cloudVersionName?: string
}

export type LocalDiagnostics = {
  mcpVersion: string
  nodeVersion: string
  requestTimeoutMs: number
  connected: boolean
  eventsSubscribed: boolean
  tokenSet: boolean
  cachedInfo?: CachedCloudInfo
}

const DIAGNOSTIC_PHASES = ["dispatching", "executing", "completed", "failed", "cancelled_before_start"] as const
const DIAGNOSTIC_OPERATIONS = ["read", "command", "redacted"] as const

export type CloudDiagnosticRecord = {
  id: number
  operation: (typeof DIAGNOSTIC_OPERATIONS)[number]
  route: string
  batchSize?: number | null
  phase: (typeof DIAGNOSTIC_PHASES)[number]
  elapsedMs: number
  dispatchMs?: number | null
  executionMs?: number | null
  slow: boolean
  errorType?: string | null
}

export type CloudDiagnostics = {
  schemaVersion: 1
  cloudVersion: number
  cloudVersionName: string
  androidApi: number
  executionMode: "dispatchers_io"
  slowThresholdMs: 10000
  activeCount: number
  activeTruncatedCount: number
  active: CloudDiagnosticRecord[]
  recent: CloudDiagnosticRecord[]
}

export type DiagnosticsReport = {
  local: LocalDiagnostics
  cloud?: CloudDiagnostics
  cloudError?: DiagnosticsCloudError
}

export type AcceptedCloudDiagnostics =
  | { ok: true; cloud: CloudDiagnostics }
  | { ok: false; cloudError: DiagnosticsCloudError }

type CachedInfoSource = {
  appVersion?: unknown
  appVersionName?: unknown
  apiVersion?: unknown
  cloudVersion?: unknown
  cloudVersionName?: unknown
}

const VERSION_NAME = /^[0-9]{1,6}(?:\.[0-9]{1,6}){1,3}$/
const SAFE_ROUTE = /^(?:redacted|GET \/[A-Za-z0-9_./{}-]{1,120}|POST \/api\/contentprovider)$/
const CLASS_NAME = /^[A-Za-z_$][A-Za-z0-9_$]{0,80}(?:\.[A-Za-z_$][A-Za-z0-9_$]{0,80}){0,12}$/
const REDACTED_ROUTE = "redacted"

function safeInteger(value: unknown): number | undefined {
  return typeof value === "number" && Number.isSafeInteger(value) && value >= 0 ? value : undefined
}

function versionName(value: unknown): string | undefined {
  return typeof value === "string" && value.length <= 32 && VERSION_NAME.test(value) ? value : undefined
}

export function whitelistCachedInfo(info: CachedInfoSource | undefined): CachedCloudInfo | undefined {
  if (!info) return undefined
  const cached: CachedCloudInfo = {}
  const appVersion = safeInteger(info.appVersion)
  const appVersionName = versionName(info.appVersionName)
  const apiVersion = safeInteger(info.apiVersion)
  const cloudVersion = safeInteger(info.cloudVersion)
  const cloudVersionName = versionName(info.cloudVersionName)
  if (appVersion !== undefined) cached.appVersion = appVersion
  if (appVersionName !== undefined) cached.appVersionName = appVersionName
  if (apiVersion !== undefined) cached.apiVersion = apiVersion
  if (cloudVersion !== undefined) cached.cloudVersion = cloudVersion
  if (cloudVersionName !== undefined) cached.cloudVersionName = cloudVersionName
  return Object.keys(cached).length ? cached : undefined
}

export function localDiagnostics(input: {
  requestTimeoutMs: number
  connected: boolean
  eventsSubscribed: boolean
  tokenSet: boolean
  info?: CachedInfoSource
}): LocalDiagnostics {
  const cachedInfo = whitelistCachedInfo(input.info)
  return {
    mcpVersion: MCP_VERSION,
    nodeVersion: process.version,
    requestTimeoutMs: input.requestTimeoutMs,
    connected: input.connected,
    eventsSubscribed: input.eventsSubscribed,
    tokenSet: input.tokenSet,
    ...(cachedInfo ? { cachedInfo } : {}),
  }
}

function httpStatus(status: number | undefined): number | undefined {
  return typeof status === "number" && Number.isSafeInteger(status) ? status : undefined
}

export function classifyCloudError(error: unknown): DiagnosticsCloudError {
  if (error instanceof LifeUpHttpError) {
    if (error.status === 401) return { category: "unauthorized", status: 401 }
    if (error.status === 404) return { category: "unsupported", status: 404 }
    if (error.status === undefined && error.message.includes(" timed out after ")) return { category: "timeout" }
    const status = httpStatus(error.status)
    return status === undefined ? { category: "unavailable" } : { category: "unavailable", status }
  }
  return { category: "unavailable" }
}

const safeInt = z.number().int().nonnegative().max(Number.MAX_SAFE_INTEGER)
const nullableInt = safeInt.nullable().optional()

const recordSchema = z.object({
  id: z.number().int().positive().max(Number.MAX_SAFE_INTEGER),
  operation: z.enum(DIAGNOSTIC_OPERATIONS),
  route: z.string().transform((value) => (SAFE_ROUTE.test(value) ? value : REDACTED_ROUTE)),
  batchSize: nullableInt,
  phase: z.enum(DIAGNOSTIC_PHASES),
  elapsedMs: safeInt,
  dispatchMs: nullableInt,
  executionMs: nullableInt,
  slow: z.boolean(),
  errorType: z.union([z.string(), z.null()]).optional().transform((value) => {
    if (value == null) return value
    return CLASS_NAME.test(value) ? value : null
  }),
})

const recordsSchema = z.preprocess(
  (value) => (Array.isArray(value) ? value.slice(0, 64) : value),
  z.array(recordSchema).max(64),
)

const snapshotSchema = z.object({
  schemaVersion: z.literal(1),
  cloudVersion: safeInt,
  cloudVersionName: z.string().regex(VERSION_NAME).max(32),
  androidApi: safeInt,
  executionMode: z.literal("dispatchers_io"),
  slowThresholdMs: z.literal(10000),
  activeCount: safeInt,
  activeTruncatedCount: safeInt,
  active: recordsSchema,
  recent: recordsSchema,
})

function unknownSchemaVersion(data: unknown): boolean {
  if (!data || typeof data !== "object" || Array.isArray(data)) return false
  const version = (data as { schemaVersion?: unknown }).schemaVersion
  return typeof version === "number" && Number.isSafeInteger(version) && version !== 1
}

export function acceptCloudDiagnostics(data: unknown): AcceptedCloudDiagnostics {
  if (unknownSchemaVersion(data)) return { ok: false, cloudError: { category: "unsupported" } }
  const parsed = snapshotSchema.safeParse(data)
  if (!parsed.success) return { ok: false, cloudError: { category: "unavailable" } }
  return { ok: true, cloud: parsed.data }
}
