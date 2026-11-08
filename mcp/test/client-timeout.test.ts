import assert from "node:assert/strict"
import { spawn } from "node:child_process"
import { mkdtemp, readFile, rm, writeFile } from "node:fs/promises"
import { createServer, type Server } from "node:http"
import type { AddressInfo } from "node:net"
import { tmpdir } from "node:os"
import { join } from "node:path"
import { mock, test } from "node:test"
import { fileURLToPath } from "node:url"
import { LIFEUP_CONCURRENCY_LIMITED, LifeUpClient, LifeUpHttpError } from "../src/client.ts"

const mcpRoot = fileURLToPath(new URL("..", import.meta.url))

function envelope(data: unknown): string {
  return JSON.stringify({ code: 200, message: "success", data })
}

function namedError(name: "TimeoutError" | "AbortError", message: string): Error {
  return Object.assign(new Error(message), { name })
}

function hang(_url: string, init?: { signal?: AbortSignal }): Promise<never> {
  return new Promise((_resolve, reject) => {
    const signal = init?.signal
    if (!signal) return
    const fail = () => reject(namedError("AbortError", "aborted"))
    if (signal.aborted) fail()
    else signal.addEventListener("abort", fail, { once: true })
  })
}

async function withTimeoutEnv(value: string | undefined, run: () => Promise<void>) {
  const previous = process.env.LIFEUP_TIMEOUT_MS
  if (value === undefined) delete process.env.LIFEUP_TIMEOUT_MS
  else process.env.LIFEUP_TIMEOUT_MS = value
  try {
    await run()
  } finally {
    if (previous === undefined) delete process.env.LIFEUP_TIMEOUT_MS
    else process.env.LIFEUP_TIMEOUT_MS = previous
  }
}

async function rejection(work: Promise<unknown>): Promise<unknown> {
  try {
    await work
  } catch (error) {
    return error
  }
  throw new Error("expected the request to fail")
}

function assertTimeout(error: unknown, pattern: RegExp, forbidden: RegExp) {
  assert.ok(error instanceof LifeUpHttpError)
  assert.equal(error.status, undefined)
  assert.match(error.message, pattern)
  assert.doesNotMatch(error.message, forbidden)
  assert.doesNotMatch(error.message, /cancel|abort/i)
}

test("stalled GET aborts at the default 30s budget and names method and path", async () => {
  await withTimeoutEnv(undefined, async () => {
    mock.timers.enable({ apis: ["setTimeout"] })
    try {
      let aborted = false
      const client = new LifeUpClient("127.0.0.1", 13276, "secret-token", (_url, init) => {
        init?.signal?.addEventListener("abort", () => {
          aborted = true
        })
        return hang(_url, init)
      })
      const pending = rejection(client.get("/info", { access_token: "query-secret" }))
      mock.timers.tick(29_999)
      assert.equal(aborted, false)
      mock.timers.tick(1)
      assert.equal(aborted, true)
      assertTimeout(await pending, /^GET \/info timed out after 30s$/, /query-secret|secret-token|\?/)
    } finally {
      mock.timers.reset()
    }
  })
})

test("LIFEUP_TIMEOUT_MS replaces the default budget", async () => {
  await withTimeoutEnv("45000", async () => {
    mock.timers.enable({ apis: ["setTimeout"] })
    try {
      let aborted = false
      const client = new LifeUpClient("127.0.0.1", 13276, undefined, (_url, init) => {
        init?.signal?.addEventListener("abort", () => {
          aborted = true
        })
        return hang(_url, init)
      })
      const pending = rejection(client.get("/coin"))
      mock.timers.tick(30_000)
      assert.equal(aborted, false)
      mock.timers.tick(15_000)
      assert.equal(aborted, true)
      assertTimeout(await pending, /^GET \/coin timed out after 45s$/, /\?/)
    } finally {
      mock.timers.reset()
    }
  })
})

test("a supplied timeout is the configured budget, not the measured wait", async () => {
  const client = new LifeUpClient("127.0.0.1", 13276, "secret-token", async () => {
    throw namedError("TimeoutError", "The operation was aborted due to timeout")
  }, 80)
  assertTimeout(
    await rejection(client.get("/tasks", { token: "query-secret" })),
    /^GET \/tasks timed out after 0\.08s$/,
    /query-secret|secret-token|80ms|0s$/,
  )
})

test("invalid LIFEUP_TIMEOUT_MS fails instead of falling back", async () => {
  const invalid = ["", "0", "-1", "1.5", "abc", "120001", "Infinity", " 30", "30 ", "30_000", "+30000", "00", "1e3", "NaN", "30000.0"]
  for (const raw of invalid) {
    await withTimeoutEnv(raw, async () => {
      assert.throws(() => new LifeUpClient("127.0.0.1", 13276, "secret-token"), (error: unknown) => {
        assert.ok(error instanceof Error)
        assert.match(error.message, /Invalid LIFEUP_TIMEOUT_MS/)
        assert.match(error.message, /1 to 120000/)
        assert.ok(error.message.includes(raw), error.message)
        assert.doesNotMatch(error.message, /secret-token/)
        return true
      })
    })
  }
})

test("boundary timeout values are accepted and an explicit timeout overrides the environment", async () => {
  await withTimeoutEnv("45000", async () => {
    const overridden = new LifeUpClient("127.0.0.1", 13276, undefined, async () => {
      throw namedError("AbortError", "aborted http://127.0.0.1/info?token=leak")
    }, 1)
    assertTimeout(await rejection(overridden.get("/info")), /^GET \/info timed out after 0\.001s$/, /leak|45000|45s/)
  })
  await withTimeoutEnv(undefined, async () => {
    const max = new LifeUpClient("127.0.0.1", 13276, undefined, async () => {
      throw namedError("TimeoutError", "timeout")
    }, 120_000)
    assertTimeout(await rejection(max.get("/events")), /^GET \/events timed out after 120s$/, /\?/)
    const second = new LifeUpClient("127.0.0.1", 13276, undefined, async () => {
      throw namedError("TimeoutError", "timeout")
    }, 1500)
    assertTimeout(await rejection(second.get("/history")), /^GET \/history timed out after 1\.5s$/, /\?/)
    const odd = new LifeUpClient("127.0.0.1", 13276, undefined, async () => {
      throw namedError("TimeoutError", "timeout")
    }, 1001)
    assertTimeout(await rejection(odd.get("/skills")), /^GET \/skills timed out after 1\.001s$/, /\?/)
  })
  for (const value of [0, -5, 1.5, 120_001, Number.NaN, Number.POSITIVE_INFINITY]) {
    assert.throws(() => new LifeUpClient("127.0.0.1", 13276, undefined, fetch, value), /Invalid timeoutMs/)
  }
})

test("TimeoutError and AbortError from headers or body use the same timeout failure", async () => {
  for (const name of ["TimeoutError", "AbortError"] as const) {
    const header = new LifeUpClient("127.0.0.1", 13276, undefined, async () => {
      throw namedError(name, `http://phone/tasks?title=secret ${name}`)
    }, 1500)
    assertTimeout(await rejection(header.get("/tasks", { title: "secret" })), /^GET \/tasks timed out after 1\.5s$/, /secret/)

    const body = new LifeUpClient("127.0.0.1", 13276, undefined, async () => ({
      status: 200,
      async text() {
        throw namedError(name, "body failed with title=secret")
      },
    }), 1000)
    assertTimeout(await rejection(body.get("/tasks")), /^GET \/tasks timed out after 1s$/, /secret/)
  }
})

test("header time and body time share one deadline", { timeout: 3000 }, async () => {
  let calls = 0
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, async (_url, init) => {
    calls += 1
    await delay(70, init?.signal)
    return {
      status: 200,
      headers: { get: () => "\"e\"" },
      text: () => delay(70, init?.signal).then(() => envelope({ ok: true })),
    }
  }, 100)
  assertTimeout(await rejection(client.get("/tasks")), /^GET \/tasks timed out after 0\.1s$/, /\?/)
  assert.equal(calls, 1)

  const fast = new LifeUpClient("127.0.0.1", 13276, undefined, async (_url, init) => {
    await delay(20, init?.signal)
    return { status: 200, text: () => delay(20, init?.signal).then(() => envelope({ ok: true })) }
  }, 200)
  assert.deepEqual(await fast.get("/tasks"), { ok: true })
})

test("a header stall that ignores the signal still fails once", { timeout: 3000 }, async () => {
  let calls = 0
  const started = Date.now()
  const client = new LifeUpClient("127.0.0.1", 13276, "secret-token", () => {
    calls += 1
    return new Promise(() => {})
  }, 80)
  assertTimeout(
    await rejection(client.get("/info", { access_token: "query-secret" })),
    /^GET \/info timed out after 0\.08s$/,
    /query-secret|secret-token/,
  )
  assert.equal(calls, 1)
  const elapsed = Date.now() - started
  assert.ok(elapsed >= 40 && elapsed < 1500, `elapsed ${elapsed}`)
})

test("a body stall does not reset the budget or send the command again", { timeout: 3000 }, async () => {
  let calls = 0
  let body = ""
  const client = new LifeUpClient("127.0.0.1", 13276, "secret-token", async (_url, init) => {
    calls += 1
    body = init?.body ?? ""
    await delay(40, init?.signal)
    return { status: 200, text: () => new Promise(() => {}) }
  }, 70)
  assertTimeout(
    await rejection(client.callApi("complete", { id: 1, note: "body-secret" })),
    /^POST \/api\/contentprovider timed out after 0\.07s; outcome unknown, check state before retry$/,
    /body-secret|secret-token|lifeup:/,
  )
  assert.equal(calls, 1)
  assert.match(body, /body-secret/)
})

test("launch submission timeout is not sent again", { timeout: 3000 }, async () => {
  let calls = 0
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, (_url, init) => {
    calls += 1
    assert.equal(init?.method, "POST")
    return hang(_url, init)
  }, 50)
  const started = Date.now()
  assertTimeout(
    await rejection(client.callApi("goto", { page: "task", title: "launch-secret" }, "launch")),
    /^POST \/api timed out after 0\.05s; outcome unknown, check state before retry$/,
    /launch-secret|lifeup:|\/api\/contentprovider/,
  )
  assert.equal(calls, 1)
  const elapsed = Date.now() - started
  assert.ok(elapsed < 1500, `elapsed ${elapsed}`)
})

test("GET, mutation, and 304 succeed inside the deadline", async () => {
  const calls: string[] = []
  let modified = true
  const client = new LifeUpClient("127.0.0.1", 13276, "secret-token", async (url, init) => {
    calls.push(`${init?.method ?? "GET"} ${url}`)
    if (init?.method === "POST") {
      return { status: 200, text: async () => envelope([{ url: "lifeup://api/complete?id=1", result: { ok: true } }]) }
    }
    if (!modified) {
      return { status: 304, headers: { get: () => "\"e\"" }, text: async () => { throw new Error("304 body") } }
    }
    modified = false
    return {
      status: 200,
      headers: { get: (name: string) => (name.toLowerCase() === "etag" ? "\"e\"" : null) },
      text: async () => envelope({ value: 4 }),
    }
  }, 200)
  assert.deepEqual(await client.get<{ value: number }>("/tasks", { gid: 7 }), { value: 4 })
  assert.deepEqual(await client.get("/tasks", { gid: 7 }), { value: 4 })
  const results = await client.callApi("complete", { id: 1 })
  assert.equal(calls.length, 3)
  assert.match(calls[2], /POST http:\/\/127\.0\.0\.1:13276\/api\/contentprovider$/)
  const result = results[0]?.result
  assert.equal(result !== null && typeof result === "object" && "ok" in result && result.ok, true)
})

test("HTTP 503 is a failure and is not retried", async () => {
  let calls = 0
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, async () => {
    calls += 1
    return { status: 503, text: async () => "busy" }
  }, 200)
  const error = await rejection(client.callApi("complete", { id: 1 }))
  assert.ok(error instanceof LifeUpHttpError)
  assert.equal(error.status, 503)
  assert.equal(error.message, "HTTP 503")
  assert.equal(calls, 1)
})

test("network errors stay network errors", async () => {
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, async () => {
    throw new TypeError("fetch failed")
  }, 200)
  const error = await rejection(client.get("/info"))
  assert.ok(error instanceof TypeError)
  assert.equal(error.message, "fetch failed")
})

test("loopback stalls share one real fetch deadline and do not retry", { timeout: 3000 }, async () => {
  const header = await listen((req, res) => {
    header.paths.push(req.url ?? "")
    req.on("close", () => {
      header.closed += 1
    })
    void res
  })
  try {
    const client = new LifeUpClient("127.0.0.1", header.port, "secret-token", undefined, 150)
    assertTimeout(
      await rejection(client.get("/info", { access_token: "query-secret" })),
      /^GET \/info timed out after 0\.15s$/,
      /query-secret|secret-token/,
    )
    assert.deepEqual(header.paths, ["/info?access_token=query-secret"])
    assert.equal(header.hits, 1)
    assert.equal(await settled(header), 1)
  } finally {
    await closeServer(header.server)
  }

  const body = await listen((req, res) => {
    req.on("close", () => {
      body.closed += 1
    })
    res.writeHead(200, { "content-type": "application/json" })
  })
  try {
    const client = new LifeUpClient("127.0.0.1", body.port, undefined, undefined, 150)
    assertTimeout(
      await rejection(client.callApis(["lifeup://api/complete?id=1&note=body-secret"])),
      /^POST \/api\/contentprovider timed out after 0\.15s; outcome unknown, check state before retry$/,
      /body-secret|lifeup:/,
    )
    assert.equal(body.hits, 1)
    assert.equal(await settled(body), 1)
  } finally {
    await closeServer(body.server)
  }
})

test("loopback GET and 304 succeed inside the deadline", { timeout: 3000 }, async () => {
  const seen: Array<string | undefined> = []
  const server = await listen((req, res) => {
    seen.push(req.headers["if-none-match"])
    if (req.headers["if-none-match"] === "\"e1\"") {
      res.writeHead(304, { ETag: "\"e1\"" })
      res.end()
      return
    }
    res.writeHead(200, { "content-type": "application/json", ETag: "\"e1\"" })
    res.end(envelope({ value: 7 }))
  })
  try {
    const client = new LifeUpClient("127.0.0.1", server.port, undefined, undefined, 1000)
    assert.deepEqual(await client.get("/tasks"), { value: 7 })
    assert.deepEqual(await client.get("/tasks"), { value: 7 })
    assert.deepEqual(seen, [undefined, "\"e1\""])
  } finally {
    await closeServer(server.server)
  }
})

test("invalid LIFEUP_TIMEOUT_MS exits at startup and leaves saved config untouched", { timeout: 3000 }, async () => {
  const dir = await mkdtemp(join(tmpdir(), "lifeup-timeout-"))
  const configPath = join(dir, "saved.json")
  const saved = `${JSON.stringify({ host: "10.0.0.8", port: 13276, token: "saved-token" }, null, 2)}\n`
  await writeFile(configPath, saved)
  const child = spawn(process.execPath, ["--import", "tsx", "src/index.ts"], {
    cwd: mcpRoot,
    env: { ...process.env, LIFEUP_TIMEOUT_MS: "nope", LIFEUP_TOKEN: "env-token", LIFEUP_MCP_CONFIG: configPath },
    stdio: ["ignore", "pipe", "pipe"],
  })
  let stderr = ""
  child.stderr?.on("data", (chunk: Buffer) => {
    stderr += chunk.toString("utf8")
  })
  try {
    const code = await new Promise<number | null>((resolve) => child.once("close", resolve))
    assert.equal(code, 1)
    assert.match(stderr, /Invalid LIFEUP_TIMEOUT_MS/)
    assert.match(stderr, /nope/)
    assert.doesNotMatch(stderr, /saved-token|env-token/)
    assert.equal(await readFile(configPath, "utf8"), saved)
  } finally {
    child.kill("SIGTERM")
    await rm(dir, { recursive: true, force: true })
  }
})

test("a valid LIFEUP_TIMEOUT_MS does not exit at startup", { timeout: 3000 }, async () => {
  const child = spawn(process.execPath, ["--import", "tsx", "src/index.ts"], {
    cwd: mcpRoot,
    env: { ...process.env, LIFEUP_TIMEOUT_MS: "30000" },
    stdio: ["pipe", "pipe", "pipe"],
  })
  let stderr = ""
  child.stderr?.on("data", (chunk: Buffer) => {
    stderr += chunk.toString("utf8")
  })
  try {
    const early = await Promise.race([
      new Promise<number | null>((resolve) => child.once("exit", resolve)),
      new Promise<"alive">((resolve) => setTimeout(() => resolve("alive"), 300)),
    ])
    assert.equal(early, "alive")
    assert.equal(stderr, "")
  } finally {
    child.kill("SIGTERM")
  }
})

function delay(ms: number, signal?: AbortSignal): Promise<void> {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(resolve, ms)
    if (!signal) return
    const onAbort = () => {
      clearTimeout(timer)
      reject(namedError("AbortError", "aborted"))
    }
    if (signal.aborted) onAbort()
    else signal.addEventListener("abort", onAbort, { once: true })
  })
}

type Probe = {
  server: Server
  port: number
  hits: number
  closed: number
  paths: string[]
}

async function listen(onRequest: (req: import("node:http").IncomingMessage, res: import("node:http").ServerResponse) => void): Promise<Probe> {
  const probe: Probe = { server: undefined as unknown as Server, port: 0, hits: 0, closed: 0, paths: [] }
  const server = createServer((req, res) => {
    probe.hits += 1
    onRequest(req, res)
  })
  probe.server = server
  await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", resolve))
  probe.port = (server.address() as AddressInfo).port
  return probe
}

async function settled(probe: Probe): Promise<number> {
  const started = Date.now()
  while (probe.closed < 1 && Date.now() - started < 1000) await delay(10)
  return probe.closed
}

async function closeServer(server: Server): Promise<void> {
  server.closeAllConnections()
  await new Promise<void>((resolve, reject) => server.close((error) => (error ? reject(error) : resolve())))
}

test("GET and POST Cloud requests send X-LifeUp-Concurrency: limited once", async () => {
  const seen: string[] = []
  const client = new LifeUpClient("127.0.0.1", 13276, "secret", async (_url, init) => {
    seen.push(`${init?.method ?? "GET"} ${init?.headers?.[LIFEUP_CONCURRENCY_LIMITED.name] ?? ""}`)
    if (init?.method === "POST") return { status: 200, text: async () => envelope([]) }
    return { status: 200, text: async () => envelope({ ok: true }) }
  })
  await client.get("/info")
  await client.get("/diagnostics")
  await client.callApi("complete", { id: 1 })
  await client.callApi("goto", { page: "task" }, "launch")
  assert.deepEqual(seen, ["GET limited", "GET limited", "POST limited", "POST limited"])
  assert.equal(LIFEUP_CONCURRENCY_LIMITED.name, "X-LifeUp-Concurrency")
  assert.equal(LIFEUP_CONCURRENCY_LIMITED.value, "limited")
})

test("HTTP 503 code 10003 is busy for GET and POST and is not retried", async () => {
  const body = JSON.stringify({
    code: 10003,
    message: "LifeUp Cloud is busy; the request was not started. Retry later.",
    data: null,
  })
  let calls = 0
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, async () => {
    calls += 1
    return { status: 503, text: async () => body }
  }, 200)
  const getError = await rejection(client.get("/tasks/3"))
  assert.ok(getError instanceof LifeUpHttpError)
  assert.equal(getError.status, 503)
  assert.equal(getError.code, 10003)
  assert.equal(getError.message, "LifeUp Cloud is busy; request not started. Retry later.")
  const postError = await rejection(client.callApi("complete", { id: 1 }))
  assert.ok(postError instanceof LifeUpHttpError)
  assert.equal(postError.status, 503)
  assert.equal(postError.code, 10003)
  assert.equal(postError.message, "LifeUp Cloud is busy; request not started. Retry later; the command was not executed.")
  assert.equal(calls, 2)
})

test("HTTP 503 without code 10003 stays HTTP 503", async () => {
  const bodies = [
    "busy",
    "",
    "<html>",
    "10003",
    JSON.stringify({ code: 10002, message: "cp down", data: null }),
    JSON.stringify({ code: "10003", message: "nope", data: null }),
  ]
  for (const body of bodies) {
    let calls = 0
    const client = new LifeUpClient("127.0.0.1", 13276, undefined, async () => {
      calls += 1
      return { status: 503, text: async () => body }
    }, 200)
    for (const run of [() => client.get("/info"), () => client.callApi("complete", { id: 1 })]) {
      const error = await rejection(run())
      assert.ok(error instanceof LifeUpHttpError)
      assert.equal(error.status, 503)
      assert.equal(error.code, undefined)
      assert.equal(error.message, "HTTP 503")
    }
    assert.equal(calls, 2)
  }
})

test("identical in-flight GETs share one fetch and a settled call refetches", async () => {
  let calls = 0
  let release: () => void = () => {}
  const gate = new Promise<void>((resolve) => {
    release = resolve
  })
  const conditional: Array<string | undefined> = []
  const client = new LifeUpClient("127.0.0.1", 13276, "tok-a", async (url, init) => {
    calls += 1
    conditional.push(init?.headers?.["If-None-Match"])
    await gate
    return {
      status: 200,
      headers: { get: (name: string) => (name.toLowerCase() === "etag" ? "\"e1\"" : null) },
      text: async () => envelope({ url }),
    }
  })
  const sameA = client.get<{ url: string }>("/tasks", { category: 1 })
  const sameB = client.get<{ url: string }>("/tasks", { category: 1 })
  const different = client.get<{ url: string }>("/tasks", { category: 2 })
  assert.equal(calls, 2)
  release()
  const [a, b, c] = await Promise.all([sameA, sameB, different])
  assert.equal(a, b)
  assert.notEqual(a.url, c.url)
  assert.equal(calls, 2)
  assert.deepEqual(conditional, [undefined, undefined])
  const again = await client.get<{ url: string }>("/tasks", { category: 1 })
  assert.equal(calls, 3)
  assert.equal(conditional[2], "\"e1\"")
  assert.equal(again.url, a.url)
})

test("a shared GET failure is delivered once and the next call refetches", async () => {
  let calls = 0
  let fail = true
  let release: () => void = () => {}
  let gate = new Promise<void>((resolve) => {
    release = resolve
  })
  const client = new LifeUpClient("127.0.0.1", 13276, "tok-a", async () => {
    calls += 1
    await gate
    if (fail) return { status: 500, text: async () => "nope" }
    return { status: 200, text: async () => envelope({ ok: true }) }
  })
  const first = client.get("/info")
  const second = client.get("/info")
  assert.equal(calls, 1)
  release()
  const errors = await Promise.all([rejection(first), rejection(second)])
  assert.equal(errors[0], errors[1])
  assert.ok(errors[0] instanceof LifeUpHttpError)
  assert.equal((errors[0] as LifeUpHttpError).message, "HTTP 500")
  fail = false
  gate = new Promise<void>((resolve) => {
    release = resolve
  })
  const next = client.get("/info")
  assert.equal(calls, 2)
  release()
  assert.deepEqual(await next, { ok: true })
})

test("coalesced conditional GETs send one If-None-Match request", async () => {
  let calls = 0
  let release: () => void = () => {}
  const gate = new Promise<void>((resolve) => {
    release = resolve
  })
  let primed = false
  const client = new LifeUpClient("127.0.0.1", 13276, "tok-a", async (_url, init) => {
    calls += 1
    if (!primed) {
      primed = true
      return { status: 200, headers: { get: () => "\"e\"" }, text: async () => envelope({ n: 1 }) }
    }
    assert.equal(init?.headers?.["If-None-Match"], "\"e\"")
    await gate
    return { status: 304, headers: { get: () => "\"e\"" }, text: async () => { throw new Error("304 body") } }
  })
  assert.deepEqual(await client.get("/tasks"), { n: 1 })
  const left = client.get("/tasks")
  const right = client.get("/tasks")
  assert.equal(calls, 2)
  release()
  assert.deepEqual(await left, { n: 1 })
  assert.deepEqual(await right, { n: 1 })
  assert.equal(calls, 2)
})

test("coalesced GETs share the first caller deadline", async () => {
  mock.timers.enable({ apis: ["setTimeout"] })
  try {
    let fetches = 0
    let aborted = 0
    const client = new LifeUpClient("127.0.0.1", 13276, undefined, (_url, init) => {
      fetches += 1
      init?.signal?.addEventListener("abort", () => {
        aborted += 1
      })
      return hang(_url, init)
    }, 1000)
    const pending = Promise.all([rejection(client.get("/info")), rejection(client.get("/info"))])
    mock.timers.tick(999)
    assert.equal(aborted, 0)
    mock.timers.tick(1)
    const errors = await pending
    assert.equal(fetches, 1)
    assert.equal(aborted, 1)
    assert.equal(errors[0], errors[1])
    assert.ok(errors[0] instanceof LifeUpHttpError)
    assert.match((errors[0] as LifeUpHttpError).message, /^GET \/info timed out after 1s$/)
    const again = rejection(client.get("/info"))
    assert.equal(fetches, 2)
    mock.timers.tick(1000)
    const retried = await again
    assert.ok(retried instanceof LifeUpHttpError)
    assert.equal(aborted, 2)
  } finally {
    mock.timers.reset()
  }
})
