import assert from "node:assert/strict"
import { readFileSync } from "node:fs"
import { mkdtemp, readFile, rm, writeFile } from "node:fs/promises"
import { createServer, type IncomingMessage, type ServerResponse } from "node:http"
import { tmpdir } from "node:os"
import { join } from "node:path"
import { describe, test } from "node:test"
import { InMemoryTransport, McpServer, type JSONRPCMessage } from "@modelcontextprotocol/server"
import { acceptCloudDiagnostics, type DiagnosticsReport, type LocalDiagnostics } from "../src/diagnostics.ts"
import {
  DIAGNOSTICS_GUIDANCE,
  HELP_TOOL_DESCRIPTION,
  HELP_TOPIC_DESCRIPTION,
  HELP_TOPICS,
  readHelp,
  SERVER_INSTRUCTIONS,
} from "../src/help.ts"
import { createServer as createLifeUpServer } from "../src/index.ts"
import { Session } from "../src/session.ts"
import { registerTools } from "../src/tools.ts"

const { version: packageVersion } = JSON.parse(readFileSync(new URL("../package.json", import.meta.url), "utf8")) as {
  version: string
}

const ENV_KEYS = ["LIFEUP_MCP_CONFIG", "LIFEUP_HOST", "LIFEUP_TOKEN", "LIFEUP_TIMEOUT_MS"] as const

const cloudPayload = {
  schemaVersion: 1,
  cloudVersion: 12,
  cloudVersionName: "3.2.0",
  androidApi: 34,
  executionMode: "dispatchers_io",
  slowThresholdMs: 10000,
  activeCount: 1,
  activeTruncatedCount: 0,
  active: [{
    id: 1,
    operation: "read",
    api: "tasks.list",
    route: "GET /tasks",
    batchSize: 1,
    phase: "executing",
    elapsedMs: 12001,
    dispatchMs: 4,
    executionMs: 11000,
    slow: true,
    errorType: null,
  }],
  recent: [{
    id: 2,
    operation: "command",
    api: "api.command",
    route: "POST /api/contentprovider",
    batchSize: 2,
    phase: "failed",
    elapsedMs: 40,
    dispatchMs: 3,
    executionMs: 30,
    slow: false,
    errorType: "net.lifeupapp.lifeup.http.service.ProviderExecutionTest$ProbeError",
  }],
}

type Hit = { method: string; url: string; authorization?: string }

function envelope(data: unknown) {
  return { code: 200, message: "success", data }
}

function sendJson(res: ServerResponse, status: number, body: unknown) {
  res.writeHead(status, { "content-type": "application/json" })
  res.end(JSON.stringify(body))
}

async function listen(onRequest: (req: IncomingMessage, res: ServerResponse) => void) {
  const hits: Hit[] = []
  const server = createServer((req, res) => {
    hits.push({
      method: req.method ?? "",
      url: req.url ?? "",
      authorization: typeof req.headers.authorization === "string" ? req.headers.authorization : undefined,
    })
    onRequest(req, res)
  })
  server.on("clientError", () => {})
  await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", () => resolve()))
  const address = server.address()
  if (!address || typeof address === "string") throw new Error("expected a tcp port")
  return {
    hits,
    port: address.port,
    host: `127.0.0.1:${address.port}`,
    stop: async () => {
      server.closeAllConnections()
      if (!server.listening) return
      await new Promise<void>((resolve, reject) => server.close((error) => (error ? reject(error) : resolve())))
    },
  }
}

async function closedPort(): Promise<number> {
  const server = createServer()
  await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", () => resolve()))
  const address = server.address()
  if (!address || typeof address === "string") throw new Error("expected a tcp port")
  await new Promise<void>((resolve, reject) => server.close((error) => (error ? reject(error) : resolve())))
  return address.port
}

async function withIsolatedEnv(run: (configPath: string) => Promise<void>) {
  const previous = new Map(ENV_KEYS.map((key) => [key, process.env[key]]))
  const dir = await mkdtemp(join(tmpdir(), "lifeup-mcp-diag-"))
  const configPath = join(dir, "config.json")
  process.env.LIFEUP_MCP_CONFIG = configPath
  delete process.env.LIFEUP_HOST
  delete process.env.LIFEUP_TOKEN
  delete process.env.LIFEUP_TIMEOUT_MS
  try {
    await run(configPath)
  } finally {
    for (const key of ENV_KEYS) {
      const value = previous.get(key)
      if (value === undefined) delete process.env[key]
      else process.env[key] = value
    }
    await rm(dir, { recursive: true, force: true })
  }
}

async function readMaybe(path: string): Promise<string | null> {
  try {
    return await readFile(path, "utf8")
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code === "ENOENT") return null
    throw error
  }
}

function snapshot(session: Session) {
  return {
    status: session.status(),
    host: session.client?.host,
    port: session.client?.port,
    token: session.client?.token,
  }
}

function assertRedacted(value: unknown, forbidden: string[]) {
  const raw = JSON.stringify(value)
  forbidden.forEach((secret, index) => {
    assert.equal(raw.includes(secret), false, `forbidden value #${index} leaked`)
  })
}

function assertLocal(
  report: { local: LocalDiagnostics },
  expected: { connected: boolean; eventsSubscribed: boolean; tokenSet: boolean; requestTimeoutMs?: number },
) {
  assert.equal(report.local.mcpVersion, packageVersion)
  assert.equal(report.local.nodeVersion, process.version)
  assert.equal(report.local.requestTimeoutMs, expected.requestTimeoutMs ?? 30_000)
  assert.equal(report.local.connected, expected.connected)
  assert.equal(report.local.eventsSubscribed, expected.eventsSubscribed)
  assert.equal(report.local.tokenSet, expected.tokenSet)
}

async function registeredDiagnostics(session: Session, args: { host?: string; token?: string }): Promise<DiagnosticsReport> {
  const server = new McpServer({ name: "lifeup", version: packageVersion })
  registerTools(server, session)
  const [client, serverTransport] = InMemoryTransport.createLinkedPair()
  const waiting = new Map<number, (message: JSONRPCMessage) => void>()
  client.onmessage = (message) => {
    if ("id" in message && typeof message.id === "number" && waiting.has(message.id)) {
      waiting.get(message.id)?.(message)
      waiting.delete(message.id)
    }
  }
  await client.start()
  await server.connect(serverTransport)
  let next = 1
  const request = async (method: string, params?: Record<string, unknown>) => {
    const id = next++
    const done = new Promise<JSONRPCMessage>((resolve) => waiting.set(id, resolve))
    await client.send({ jsonrpc: "2.0", id, method, params })
    return done
  }
  try {
    const init = await request("initialize", {
      protocolVersion: "2025-11-25",
      capabilities: {},
      clientInfo: { name: "diagnostics-test", version: "1.0.0" },
    })
    assert.ok("result" in init, JSON.stringify(init))
    await client.send({ jsonrpc: "2.0", method: "notifications/initialized" })
    const listed = await request("tools/list", {})
    assert.ok("result" in listed, JSON.stringify(listed))
    const tools = (listed.result as { tools?: Array<{ name: string; description?: string }> }).tools ?? []
    const tool = tools.find((item) => item.name === "get_diagnostics")
    assert.ok(tool, "get_diagnostics was not registered")
    assert.match(tool.description ?? "", /get_diagnostics once/)
    assert.match(tool.description ?? "", /Never upload/)
    const called = await request("tools/call", { name: "get_diagnostics", arguments: args })
    assert.ok("result" in called, JSON.stringify(called))
    const result = called.result as { isError?: boolean; content?: Array<{ text?: string }> }
    assert.notEqual(result.isError, true, JSON.stringify(called))
    const text = result.content?.[0]?.text
    assert.equal(typeof text, "string")
    return JSON.parse(text ?? "") as DiagnosticsReport
  } finally {
    await server.close()
    await client.close()
  }
}

describe("get_diagnostics", { concurrency: false }, () => {
  test("failed /info still diagnoses without saving config or session state", { timeout: 5000 }, async () => {
    await withIsolatedEnv(async (configPath) => {
      const cloud = await listen((req, res) => {
        const path = req.url?.split("?")[0]
        if (path === "/info") {
          res.writeHead(500, { "content-type": "text/plain" })
          res.end("INFO_BODY_SECRET connect-secret stack")
          return
        }
        sendJson(res, 200, envelope(cloudPayload))
      })
      try {
        const session = new Session()
        await assert.rejects(() => session.connect({ host: cloud.host, token: "connect-secret" }))
        const state = snapshot(session)
        assert.equal(state.host, undefined)
        assert.equal(state.token, undefined)
        assert.equal(state.status.connected, false)
        assert.equal(state.status.lastError, "HTTP 500")
        assert.equal(await readMaybe(configPath), null)

        const direct = await session.getDiagnostics({ host: cloud.host, token: "diag-secret" })
        assert.deepEqual(snapshot(session), state)
        assert.equal(await readMaybe(configPath), null)
        const viaTool = await registeredDiagnostics(session, { host: cloud.host, token: "diag-secret" })
        assert.deepEqual(snapshot(session), state)
        assert.equal(await readMaybe(configPath), null)

        for (const report of [direct, viaTool]) {
          assert.deepEqual(report.cloud, cloudPayload)
          assert.equal(report.cloudError, undefined)
          assert.equal(report.local.cachedInfo, undefined)
          assertLocal(report, { connected: false, eventsSubscribed: false, tokenSet: false })
          assertRedacted(report, ["connect-secret", "diag-secret", "INFO_BODY_SECRET", "HTTP 500", "127.0.0.1", cloud.host])
        }
        assert.deepEqual(cloud.hits.map((hit) => `${hit.method} ${hit.url}`), [
          "GET /info",
          "GET /diagnostics",
          "GET /diagnostics",
        ])
        assert.deepEqual(cloud.hits.slice(1).map((hit) => hit.authorization), ["diag-secret", "diag-secret"])
      } finally {
        await cloud.stop()
      }
    })
  })

  test("reuses the connected client unless host or token overrides it", { timeout: 5000 }, async () => {
    await withIsolatedEnv(async (configPath) => {
      const info = {
        appVersion: 10704,
        appVersionName: "1.107.4",
        apiVersion: 53,
        cloudVersion: 12,
        cloudVersionName: "3.2.0",
        token: "info-token-secret",
        host: "10.9.8.7",
        note: "secret-note",
      }
      const cloud = await listen((req, res) => {
        const path = req.url?.split("?")[0]
        if (path === "/info") {
          sendJson(res, 200, envelope(info))
          return
        }
        sendJson(res, 200, envelope(cloudPayload))
      })
      try {
        process.env.LIFEUP_TIMEOUT_MS = "30000"
        const session = new Session()
        await session.connect({ host: cloud.host, token: "session-token" })
        const tampered = `${JSON.stringify({ host: "10.1.1.1", port: 9, token: "tampered-token" }, null, 2)}\n`
        await writeFile(configPath, tampered)
        process.env.LIFEUP_TIMEOUT_MS = "45000"
        const client = session.client

        const current = await session.getDiagnostics()
        assert.equal(session.client, client)
        assert.equal(session.client?.token, "session-token")
        assert.equal(await readMaybe(configPath), tampered)
        assert.deepEqual(current.cloud, cloudPayload)
        assert.deepEqual(current.local.cachedInfo, {
          appVersion: 10704,
          appVersionName: "1.107.4",
          apiVersion: 53,
          cloudVersion: 12,
          cloudVersionName: "3.2.0",
        })
        assertLocal(current, { connected: true, eventsSubscribed: false, tokenSet: true, requestTimeoutMs: 30_000 })
        assert.equal(cloud.hits.at(-1)?.authorization, "session-token")
        assert.equal(cloud.hits.at(-1)?.url, "/diagnostics")
        assertRedacted(current, ["session-token", "tampered-token", "info-token-secret", "secret-note", "10.9.8.7", "10.1.1.1", cloud.host])

        const overridden = await session.getDiagnostics({ token: "override-token" })
        assert.equal(session.client, client)
        assert.equal(session.client?.token, "session-token")
        assert.equal(await readMaybe(configPath), tampered)
        assert.equal(cloud.hits.at(-1)?.authorization, "override-token")
        assert.equal(cloud.hits.at(-1)?.url, "/diagnostics")
        assert.equal(overridden.local.requestTimeoutMs, 30_000)
        assert.equal(overridden.local.tokenSet, true)
        assert.deepEqual(overridden.local.cachedInfo, current.local.cachedInfo)
        assertRedacted(overridden, ["override-token", "session-token", "tampered-token", cloud.host])
        assert.deepEqual(cloud.hits.map((hit) => hit.url), ["/info", "/diagnostics", "/diagnostics"])
      } finally {
        await cloud.stop()
      }
    })
  })

  test("explicit target does not replace a subscribed session", { timeout: 5000 }, async () => {
    await withIsolatedEnv(async (configPath) => {
      const cloud = await listen((_req, res) => sendJson(res, 200, envelope(cloudPayload)))
      try {
        let reused = false
        const session = new Session()
        const fake = {
          host: "10.8.8.8",
          port: 13276,
          token: "session-live-token",
          requestTimeoutMs: 30_000,
          listEvents: async () => ({ latestId: 1, eventWs: true, events: [] }),
          subscribeEvents: () => ({ close() {} }),
          get: async () => {
            reused = true
            throw new Error("reused session client")
          },
        }
        session.client = fake as never
        await session.setEventSubscription(true, 0)
        const report = await session.getDiagnostics({ host: cloud.host, token: "probe-token" })
        assert.equal(reused, false)
        assert.equal(session.client, fake)
        assert.equal(session.status().eventsSubscribed, true)
        assert.equal(session.status().host, "10.8.8.8")
        assert.equal(session.client?.token, "session-live-token")
        assert.equal(await readMaybe(configPath), null)
        assert.deepEqual(report.cloud, cloudPayload)
        assert.equal(report.local.cachedInfo, undefined)
        assertLocal(report, { connected: true, eventsSubscribed: true, tokenSet: true })
        assert.equal(cloud.hits.at(-1)?.authorization, "probe-token")
        assert.equal(cloud.hits.at(-1)?.url, "/diagnostics")
        assertRedacted(report, ["probe-token", "session-live-token", "10.8.8.8", cloud.host])
      } finally {
        await cloud.stop()
      }
    })
  })

  test("resolves saved and env credentials without persisting a foreign token", { timeout: 5000 }, async () => {
    await withIsolatedEnv(async (configPath) => {
      const cloud = await listen((req, res) => {
        if (req.url?.split("?")[0] !== "/diagnostics") {
          res.writeHead(404)
          res.end("MISSING_ROUTE")
          return
        }
        if (!req.headers.authorization) {
          res.writeHead(401, { "content-type": "text/plain" })
          res.end("BODY_401 foreign-token")
          return
        }
        sendJson(res, 200, envelope(cloudPayload))
      })
      try {
        const saved = `${JSON.stringify({ host: "127.0.0.1", port: cloud.port, token: "saved-token" })}\n`
        await writeFile(configPath, saved)
        const savedSession = new Session()
        const savedReport = await savedSession.getDiagnostics()
        assert.equal(savedSession.client, undefined)
        assert.equal(await readMaybe(configPath), saved)
        assert.deepEqual(savedReport.cloud, cloudPayload)
        assertLocal(savedReport, { connected: false, eventsSubscribed: false, tokenSet: false })
        assert.equal(cloud.hits.at(-1)?.authorization, "saved-token")
        assertRedacted(savedReport, ["saved-token", "127.0.0.1", cloud.host])

        await rm(configPath)
        process.env.LIFEUP_HOST = cloud.host
        process.env.LIFEUP_TOKEN = "env-token"
        const envSession = new Session()
        const envReport = await envSession.getDiagnostics()
        assert.equal(envSession.client, undefined)
        assert.equal(await readMaybe(configPath), null)
        assert.equal(cloud.hits.at(-1)?.authorization, "env-token")
        assertRedacted(envReport, ["env-token", cloud.host])

        const explicit = await envSession.getDiagnostics({ token: "explicit-token" })
        assert.equal(envSession.client, undefined)
        assert.equal(await readMaybe(configPath), null)
        assert.equal(cloud.hits.at(-1)?.authorization, "explicit-token")
        assert.deepEqual(explicit.cloud, cloudPayload)
        assertRedacted(explicit, ["explicit-token", "env-token", cloud.host])

        const foreignBody = `${JSON.stringify({ host: "10.4.4.4", port: 13276, token: "foreign-token" })}\n`
        await writeFile(configPath, foreignBody)
        delete process.env.LIFEUP_HOST
        delete process.env.LIFEUP_TOKEN
        const foreign = new Session()
        const foreignReport = await foreign.getDiagnostics({ host: cloud.host })
        assert.equal(foreign.client, undefined)
        assert.equal(await readMaybe(configPath), foreignBody)
        assert.equal(cloud.hits.at(-1)?.authorization, undefined)
        assert.equal(foreignReport.cloud, undefined)
        assert.deepEqual(foreignReport.cloudError, { category: "unauthorized", status: 401 })
        assertLocal(foreignReport, { connected: false, eventsSubscribed: false, tokenSet: false })
        assertRedacted(foreignReport, ["foreign-token", "BODY_401", "token required", "10.4.4.4", cloud.host])
      } finally {
        await cloud.stop()
      }
    })
  })

  test("sanitizes old, rejected, and malformed diagnostics responses", { timeout: 5000 }, async () => {
    await withIsolatedEnv(async () => {
      type Mode = "401" | "404" | "500" | "html" | "provider" | "array" | "text" | "null" | "hang"
      let mode: Mode = "401"
      const cloud = await listen((_req, res) => {
        if (mode === "hang") return
        if (mode === "401") {
          res.writeHead(401)
          res.end("BODY_401 rejected-token")
          return
        }
        if (mode === "404") {
          res.writeHead(404, { "content-type": "text/html" })
          res.end("<html>OLD_CLOUD_SECRET stack</html>")
          return
        }
        if (mode === "500") {
          res.writeHead(500)
          res.end("BODY_500 stack")
          return
        }
        if (mode === "html") {
          res.writeHead(200, { "content-type": "text/html" })
          res.end("<html>PORTAL_SECRET</html>")
          return
        }
        if (mode === "provider") {
          sendJson(res, 200, { code: 10002, message: "task content TASK_SECRET", data: null })
          return
        }
        if (mode === "array") {
          sendJson(res, 200, envelope(["TASK_ARRAY_SECRET"]))
          return
        }
        if (mode === "text") {
          sendJson(res, 200, envelope("TASK_TEXT_SECRET"))
          return
        }
        sendJson(res, 200, envelope(null))
      })
      try {
        const probe = async (next: Mode, token = "probe-token") => {
          mode = next
          const before = cloud.hits.length
          const report = await new Session().getDiagnostics({ host: cloud.host, token })
          assert.equal(cloud.hits.length, before + 1)
          assert.equal(cloud.hits.at(-1)?.method, "GET")
          assert.equal(cloud.hits.at(-1)?.url, "/diagnostics")
          assert.equal(report.cloud, undefined)
          return report
        }

        const denied = await probe("401", "rejected-token")
        assert.deepEqual(denied.cloudError, { category: "unauthorized", status: 401 })
        assertLocal(denied, { connected: false, eventsSubscribed: false, tokenSet: false })
        assertRedacted(denied, ["rejected-token", "BODY_401", "token required", cloud.host])

        const old = await probe("404")
        assert.deepEqual(old.cloudError, { category: "unsupported", status: 404 })
        assertRedacted(old, ["OLD_CLOUD_SECRET", "HTTP 404", "<html>", "probe-token", cloud.host])

        const failed = await probe("500")
        assert.deepEqual(failed.cloudError, { category: "unavailable", status: 500 })
        assertRedacted(failed, ["BODY_500", "HTTP 500", "probe-token"])

        const portal = await probe("html")
        assert.deepEqual(portal.cloudError, { category: "unavailable", status: 200 })
        assertRedacted(portal, ["PORTAL_SECRET", "Non-JSON", "captive", "probe-token"])

        const provider = await probe("provider")
        assert.deepEqual(provider.cloudError, { category: "unavailable", status: 200 })
        assertRedacted(provider, ["TASK_SECRET", "ContentProvider", "probe-token"])

        for (const next of ["array", "text", "null"] as const) {
          const report = await probe(next)
          assert.deepEqual(report.cloudError, { category: "unavailable" })
          assertRedacted(report, ["TASK_ARRAY_SECRET", "TASK_TEXT_SECRET", "probe-token"])
        }

        process.env.LIFEUP_TIMEOUT_MS = "200"
        mode = "hang"
        const before = cloud.hits.length
        const started = Date.now()
        const timedOut = await new Session().getDiagnostics({ host: cloud.host, token: "timeout-token" })
        assert.ok(Date.now() - started < 2000)
        assert.equal(cloud.hits.length, before + 1)
        assert.deepEqual(timedOut.cloudError, { category: "timeout" })
        assertLocal(timedOut, { connected: false, eventsSubscribed: false, tokenSet: false, requestTimeoutMs: 200 })
        assertRedacted(timedOut, ["timeout-token", "timed out after", "GET /diagnostics", cloud.host])
      } finally {
        await cloud.stop()
      }
    })
  })

  test("network failure returns local context without the destination", { timeout: 5000 }, async () => {
    await withIsolatedEnv(async () => {
      const port = await closedPort()
      const host = `127.0.0.1:${port}`
      const report = await new Session().getDiagnostics({ host, token: "network-token" })
      assert.equal(report.cloud, undefined)
      assert.deepEqual(report.cloudError, { category: "unavailable" })
      assertLocal(report, { connected: false, eventsSubscribed: false, tokenSet: false })
      assertRedacted(report, ["network-token", host, "127.0.0.1", "ECONNREFUSED", "fetch failed"])
    })
  })

  test("missing and invalid targets do not discover or echo input", { timeout: 2000 }, async () => {
    await withIsolatedEnv(async (configPath) => {
      const started = Date.now()
      const missing = await new Session().getDiagnostics()
      assert.ok(Date.now() - started < 1000)
      assert.equal(missing.cloud, undefined)
      assert.deepEqual(missing.cloudError, { category: "unavailable" })
      assertLocal(missing, { connected: false, eventsSubscribed: false, tokenSet: false })
      assert.equal(await readMaybe(configPath), null)

      const invalid = await new Session().getDiagnostics({ host: "bad host", token: "invalid-host-token" })
      assert.deepEqual(invalid.cloudError, { category: "unavailable" })
      assertRedacted(invalid, ["bad host", "invalid-host-token"])

      process.env.LIFEUP_HOST = "bad host marker-env-host"
      const brokenEnv = await new Session().getDiagnostics()
      assert.deepEqual(brokenEnv.cloudError, { category: "unavailable" })
      assertRedacted(brokenEnv, ["marker-env-host"])

      const orphan = await new Session().getDiagnostics({ token: "orphan-token" })
      assert.deepEqual(orphan.cloudError, { category: "unavailable" })
      assertRedacted(orphan, ["orphan-token", "marker-env-host"])
    })
  })

  test("whitelists diagnostics snapshots and drops external canaries", { timeout: 5000 }, async () => {
    await withIsolatedEnv(async (configPath) => {
      const canaries = [
        "CANARY_TOKEN",
        "http://10.9.9.9/raw?token=CANARY_URL",
        "CANARY_BODY",
        "CANARY_MESSAGE",
        "CANARY_ROUTE",
        "CANARY_ERR",
        "CANARY_VER",
        "CANARY_OP",
        "CANARY_INFO",
        "CANARY_OVERFLOW",
        "CANARY_OLD",
      ]
      const extra = {
        token: "CANARY_TOKEN",
        url: "http://10.9.9.9/raw?token=CANARY_URL",
        body: "CANARY_BODY",
        message: "CANARY_MESSAGE",
      }
      const fillers = Array.from({ length: 63 }, (_, index) => ({
        id: index + 3,
        operation: "read" as const,
        api: "tasks.list_category",
        route: "GET /tasks/{id}",
        phase: "completed" as const,
        elapsedMs: 0,
        slow: false,
      }))
      const omitted = {
        id: 8,
        operation: "command" as const,
        api: "api.command",
        route: "GET /history",
        phase: "dispatching" as const,
        elapsedMs: 2,
        slow: false,
      }
      const nullable = {
        id: 7,
        operation: "redacted" as const,
        api: "redacted",
        route: "redacted",
        batchSize: null,
        phase: "cancelled_before_start" as const,
        elapsedMs: 0,
        dispatchMs: null,
        executionMs: null,
        slow: false,
        errorType: null,
      }
      let mode = "canary"
      const cloud = await listen((req, res) => {
        const path = req.url?.split("?")[0]
        if (path === "/info") {
          sendJson(res, 200, envelope({
            appVersion: 10704,
            appVersionName: "1.107.4 CANARY_INFO",
            apiVersion: 53.5,
            cloudVersion: 12,
            cloudVersionName: "3.2.0",
            token: "CANARY_INFO",
            host: "http://10.9.9.9/raw?token=CANARY_URL",
          }))
          return
        }
        if (mode === "canary") {
          sendJson(res, 200, envelope({
            ...cloudPayload,
            ...extra,
            active: [{ ...cloudPayload.active[0], ...extra }],
            recent: [
              { ...cloudPayload.recent[0], ...extra },
              ...fillers,
              { ...fillers[0], id: 99, body: "CANARY_OVERFLOW" },
            ],
          }))
          return
        }
        if (mode === "nullable") {
          sendJson(res, 200, envelope({
            ...cloudPayload,
            activeCount: 0,
            active: [nullable],
            recent: [omitted],
          }))
          return
        }
        if (mode === "unsafe") {
          sendJson(res, 200, envelope({
            ...cloudPayload,
            active: [{
              ...cloudPayload.active[0],
              api: "CANARY_API",
              route: "GET /tasks?token=CANARY_ROUTE",
              errorType: "java.lang.IllegalStateException: CANARY_ERR",
            }],
          }))
          return
        }
        if (mode === "version") {
          sendJson(res, 200, envelope({ ...cloudPayload, cloudVersionName: "3.2.0 CANARY_VER" }))
          return
        }
        if (mode === "operation") {
          sendJson(res, 200, envelope({
            ...cloudPayload,
            active: [{ ...cloudPayload.active[0], operation: "command CANARY_OP" }],
          }))
          return
        }
        sendJson(res, 200, envelope({ ...cloudPayload, ...extra, schemaVersion: 2, token: "CANARY_OLD" }))
      })
      try {
        const session = new Session()
        await session.connect({ host: cloud.host, token: "session-canary-token" })
        const saved = await readMaybe(configPath)
        const state = snapshot(session)
        const direct = await session.getDiagnostics()
        const viaTool = await registeredDiagnostics(session, {})
        const expected = {
          ...cloudPayload,
          recent: [cloudPayload.recent[0], ...fillers],
        }
        for (const report of [direct, viaTool]) {
          assert.deepEqual(report.cloud, expected)
          assert.equal(report.cloudError, undefined)
          assert.deepEqual(report.local.cachedInfo, {
            appVersion: 10704,
            cloudVersion: 12,
            cloudVersionName: "3.2.0",
          })
          assertRedacted(report, [...canaries, "session-canary-token", cloud.host])
        }
        assert.equal(direct.cloud?.recent.length, 64)
        assert.equal(await readMaybe(configPath), saved)
        assert.deepEqual(snapshot(session), state)

        mode = "nullable"
        const flexible = await session.getDiagnostics({ host: cloud.host, token: "probe-canary-token" })
        assert.deepEqual(flexible.cloud, {
          ...cloudPayload,
          activeCount: 0,
          active: [nullable],
          recent: [omitted],
        })
        assert.equal(session.client?.token, "session-canary-token")
        assertRedacted(flexible, [...canaries, "probe-canary-token", "session-canary-token"])

        mode = "unsafe"
        const sanitized = await session.getDiagnostics({ host: cloud.host })
        assert.equal(sanitized.cloud?.active[0]?.api, "redacted")
        assert.equal(sanitized.cloud?.active[0]?.route, "redacted")
        assert.equal(sanitized.cloud?.active[0]?.errorType, null)
        assert.equal(sanitized.cloudError, undefined)
        assertRedacted(sanitized, canaries)

        for (const [next, category] of [
          ["version", "unavailable"],
          ["operation", "unavailable"],
          ["old", "unsupported"],
        ] as const) {
          mode = next
          const rejected = await session.getDiagnostics({ host: cloud.host, token: "probe-canary-token" })
          assert.equal(rejected.cloud, undefined)
          assert.deepEqual(rejected.cloudError, { category })
          assertRedacted(rejected, [...canaries, "probe-canary-token", "session-canary-token", "ZodError", "Invalid", "expected"])
        }
        assert.deepEqual(snapshot(session), state)
      } finally {
        await cloud.stop()
      }
    })
  })

  test("help and server instructions point at voluntary diagnostics", async () => {
    assert.equal(HELP_TOPICS.includes("diagnostics"), true)
    assert.match(HELP_TOOL_DESCRIPTION, /diagnostics/)
    assert.match(HELP_TOPIC_DESCRIPTION, /diagnostics/)
    assert.match(DIAGNOSTICS_GUIDANCE, /get_diagnostics once/)
    assert.match(DIAGNOSTICS_GUIDANCE, /do not retry writes blindly/)
    assert.match(SERVER_INSTRUCTIONS, /get_diagnostics once/)
    assert.match(SERVER_INSTRUCTIONS, /Never upload, email, or open an issue automatically/)
    assert.doesNotMatch(SERVER_INSTRUCTIONS, /mailto:/)
    assert.doesNotMatch(DIAGNOSTICS_GUIDANCE, /https?:\/\//)
    await assert.rejects(() => readHelp("not-a-diagnostics-topic"), /diagnostics/)
    const text = await readHelp("diagnostics")
    assert.match(text, /get_diagnostics/)
    assert.match(text, /schemaVersion/)
    assert.match(text, /Copyable report template/)
  })

  test("the production server registers get_diagnostics", { timeout: 5000 }, async () => {
    const server = createLifeUpServer()
    const [client, serverTransport] = InMemoryTransport.createLinkedPair()
    const waiting = new Map<number, (message: JSONRPCMessage) => void>()
    client.onmessage = (message) => {
      if ("id" in message && typeof message.id === "number" && waiting.has(message.id)) {
        waiting.get(message.id)?.(message)
        waiting.delete(message.id)
      }
    }
    await client.start()
    await server.connect(serverTransport)
    const done = new Promise<JSONRPCMessage>((resolve) => waiting.set(1, resolve))
    try {
      await client.send({
        jsonrpc: "2.0",
        id: 1,
        method: "initialize",
        params: {
          protocolVersion: "2025-11-25",
          capabilities: {},
          clientInfo: { name: "diagnostics-test", version: "1.0.0" },
        },
      })
      assert.ok("result" in await done)
      await client.send({ jsonrpc: "2.0", method: "notifications/initialized" })
      const listed = new Promise<JSONRPCMessage>((resolve) => waiting.set(2, resolve))
      await client.send({ jsonrpc: "2.0", id: 2, method: "tools/list", params: {} })
      const message = await listed
      assert.ok("result" in message)
      const tools = (message.result as { tools?: Array<{ name: string; description?: string }> }).tools ?? []
      const tool = tools.find((item) => item.name === "get_diagnostics")
      assert.ok(tool)
      assert.match(tool?.description ?? "", /get_diagnostics once/)
      assert.match(tool?.description ?? "", /existing feedback channel/)
    } finally {
      await server.close()
      await client.close()
    }
  })
})

test("diagnostics records accept limited, queueMs, and shared; old snapshots still pass", () => {
  const old = acceptCloudDiagnostics(cloudPayload)
  assert.equal(old.ok, true)
  if (!old.ok) return
  assert.equal(old.cloud.schemaVersion, 1)
  assert.equal(old.cloud.executionMode, "dispatchers_io")
  assert.equal("limited" in old.cloud.active[0], false)
  assert.equal("queueMs" in old.cloud.active[0], false)
  assert.equal("shared" in old.cloud.active[0], false)

  const next = acceptCloudDiagnostics({
    ...cloudPayload,
    active: [{ ...cloudPayload.active[0], limited: true, queueMs: 20, shared: false }],
    recent: [{ ...cloudPayload.recent[0], limited: false, queueMs: null, shared: true }],
  })
  assert.equal(next.ok, true)
  if (!next.ok) return
  assert.equal(next.cloud.schemaVersion, 1)
  assert.equal(next.cloud.executionMode, "dispatchers_io")
  assert.equal(next.cloud.active[0]?.limited, true)
  assert.equal(next.cloud.active[0]?.queueMs, 20)
  assert.equal(next.cloud.active[0]?.shared, false)
  assert.equal(next.cloud.recent[0]?.limited, false)
  assert.equal(next.cloud.recent[0]?.queueMs, null)
  assert.equal(next.cloud.recent[0]?.shared, true)

  const negative = acceptCloudDiagnostics({
    ...cloudPayload,
    active: [{ ...cloudPayload.active[0], queueMs: -1 }],
  })
  assert.deepEqual(negative, { ok: false, cloudError: { category: "unavailable" } })
  const fractional = acceptCloudDiagnostics({
    ...cloudPayload,
    recent: [{ ...cloudPayload.recent[0], queueMs: 1.5 }],
  })
  assert.deepEqual(fractional, { ok: false, cloudError: { category: "unavailable" } })
  const wrongMode = acceptCloudDiagnostics({ ...cloudPayload, executionMode: "binder" })
  assert.deepEqual(wrongMode, { ok: false, cloudError: { category: "unavailable" } })
  const wrongVersion = acceptCloudDiagnostics({ ...cloudPayload, schemaVersion: 2 })
  assert.deepEqual(wrongVersion, { ok: false, cloudError: { category: "unsupported" } })
})
