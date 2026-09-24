import assert from "node:assert/strict"
import { test } from "node:test"
import { LifeUpClient } from "../src/client.ts"

test("sends raw Authorization and decodes GET", async () => {
  const calls: Array<{ url: string; init?: { headers?: Record<string, string> } }> = []
  const client = new LifeUpClient("192.168.1.8", 13276, "secret", async (url, init) => {
    calls.push({ url, init })
    return { status: 200, text: async () => JSON.stringify({ code: 200, message: "success", data: { value: 9 } }) }
  })
  assert.equal((await client.get<{ value: number }>("/coin")).value, 9)
  assert.equal(calls[0].url, "http://192.168.1.8:13276/coin")
  assert.equal(calls[0].init?.headers?.Authorization, "secret")
})

test("posts contentprovider body and returns nested results", async () => {
  let body = ""
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, async (_url, init) => {
    body = init?.body ?? ""
    return {
      status: 200,
      text: async () => JSON.stringify({
        code: 200,
        message: "success",
        data: [{ url: "lifeup://api/complete?id=1&ui=false", result: { ok: true } }],
      }),
    }
  })
  const results = await client.callApi("complete", { id: 1, ui: false })
  assert.deepEqual(JSON.parse(body), { urls: ["lifeup://api/complete?id=1&ui=false"] })
  assert.equal(results[0].result?.ok, true)
})

test("launch posts /api and ignores payload", async () => {
  let url = ""
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, async (input) => {
    url = input
    return { status: 200, text: async () => JSON.stringify({ code: 200, message: "success", data: "success" }) }
  })
  const results = await client.callApi("goto", { page: "task" }, "launch")
  assert.equal(url, "http://127.0.0.1:13276/api")
  assert.equal(results[0].result, null)
})

function envelope(data: unknown) {
  return JSON.stringify({ code: 200, message: "success", data })
}

test("revalidates a GET and reuses the cached payload on 304", async () => {
  const calls: Array<Record<string, string> | undefined> = []
  let etag = "\"aaa\""
  let data: { value: number } = { value: 1 }
  let modified = true
  const client = new LifeUpClient("127.0.0.1", 13276, "secret", async (_url, init) => {
    calls.push(init?.headers)
    if (!modified) {
      return { status: 304, headers: { get: () => etag }, text: async () => { throw new Error("304 body") } }
    }
    modified = false
    return {
      status: 200,
      headers: { get: (name: string) => (name.toLowerCase() === "etag" ? etag : null) },
      text: async () => envelope(data),
    }
  })
  assert.deepEqual(await client.get<{ value: number }>("/tasks"), { value: 1 })
  assert.deepEqual(await client.get("/tasks"), { value: 1 })
  etag = "\"bbb\""
  data = { value: 2 }
  modified = true
  assert.deepEqual(await client.get("/tasks"), { value: 2 })
  assert.equal(calls[0]?.["If-None-Match"], undefined)
  assert.equal(calls[1]?.["If-None-Match"], "\"aaa\"")
  assert.equal(calls[2]?.["If-None-Match"], "\"aaa\"")
  assert.deepEqual(await client.get("/tasks"), { value: 2 })
  assert.equal(calls[3]?.["If-None-Match"], "\"bbb\"")
})

test("GET without an ETag always downloads the body", async () => {
  const sent: Array<string | undefined> = []
  let n = 0
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, async (_url, init) => {
    sent.push(init?.headers?.["If-None-Match"])
    n += 1
    return { status: 200, text: async () => envelope({ n }) }
  })
  assert.deepEqual(await client.get("/info"), { n: 1 })
  assert.deepEqual(await client.get("/info"), { n: 2 })
  assert.deepEqual(sent, [undefined, undefined])
})

test("a later success without an ETag drops the cached validator", async () => {
  const sent: Array<string | undefined> = []
  let tagged = true
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, async (_url, init) => {
    sent.push(init?.headers?.["If-None-Match"])
    if (tagged) {
      tagged = false
      return { status: 200, headers: { get: () => "\"aaa\"" }, text: async () => envelope({ n: 1 }) }
    }
    return { status: 200, text: async () => envelope({ n: 2 }) }
  })
  assert.deepEqual(await client.get("/tasks"), { n: 1 })
  assert.deepEqual(await client.get("/tasks"), { n: 2 })
  assert.deepEqual(await client.get("/tasks"), { n: 2 })
  assert.deepEqual(sent, [undefined, "\"aaa\"", undefined])
})

test("POST does not send If-None-Match", async () => {
  const headers: Array<Record<string, string>> = []
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, async (_url, init) => {
    headers.push({ ...(init?.headers ?? {}) })
    if (init?.method === "POST") return { status: 200, text: async () => envelope([]) }
    return { status: 200, headers: { get: () => "\"t\"" }, text: async () => envelope({ value: 1 }) }
  })
  await client.get("/tasks")
  await client.callApi("complete", { id: 1 })
  assert.equal(headers[1]["If-None-Match"], undefined)
})

test("304 without a cached body retries once without If-None-Match", async () => {
  let n = 0
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, async (_url, init) => {
    n += 1
    assert.equal(init?.headers?.["If-None-Match"], undefined)
    if (n === 1) return { status: 304, text: async () => "" }
    return { status: 200, headers: { get: () => "\"z\"" }, text: async () => envelope({ ok: true }) }
  })
  assert.deepEqual(await client.get("/tasks"), { ok: true })
  assert.equal(n, 2)
})

