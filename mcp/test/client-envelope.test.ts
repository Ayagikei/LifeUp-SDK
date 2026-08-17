import assert from "node:assert/strict"
import { test } from "node:test"
import { decodeEnvelope, LifeUpHttpError } from "../src/client.ts"

test("accepts code 200", () => {
  const envelope = decodeEnvelope(200, JSON.stringify({ code: 200, message: "success", data: { value: 3 } }))
  assert.equal(envelope.data?.value, 3)
})

test("maps HTTP 401 to token required or invalid", () => {
  assert.throws(() => decodeEnvelope(401, "{}"), (error: unknown) => {
    assert.ok(error instanceof LifeUpHttpError)
    assert.equal(error.message, "token required or invalid")
    return true
  })
})

test("maps envelope 10001 to LifeUp not running or no permission", () => {
  assert.throws(
    () => decodeEnvelope(200, JSON.stringify({ code: 10001, message: "down", data: null })),
    /LifeUp is not running, or Read LifeUp Data/,
  )
})

test("rejects non-JSON bodies", () => {
  assert.throws(() => decodeEnvelope(200, "<html>"), /Non-JSON response/)
})

test("maps envelope 10002 to ContentProvider failure", () => {
  assert.throws(
    () => decodeEnvelope(200, JSON.stringify({ code: 10002, message: "cp down", data: null })),
    /ContentProvider query failed|cp down/,
  )
})

