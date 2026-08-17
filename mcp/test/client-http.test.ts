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

