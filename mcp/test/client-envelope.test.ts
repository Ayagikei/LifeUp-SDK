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

test("maps HTTP 503 code 10003 for GET and POST without using the body message", () => {
  const body = JSON.stringify({
    code: 10003,
    message: "LifeUp Cloud is busy; the request was not started. Retry later.",
    data: null,
  })
  assert.throws(() => decodeEnvelope(503, body), (error: unknown) => {
    assert.ok(error instanceof LifeUpHttpError)
    assert.equal(error.status, 503)
    assert.equal(error.code, 10003)
    assert.equal(error.message, "LifeUp Cloud is busy; request not started. Retry later.")
    return true
  })
  assert.throws(() => decodeEnvelope(503, body, "GET"), (error: unknown) => {
    assert.ok(error instanceof LifeUpHttpError)
    assert.equal(error.status, 503)
    assert.equal(error.code, 10003)
    assert.equal(error.message, "LifeUp Cloud is busy; request not started. Retry later.")
    return true
  })
  assert.throws(() => decodeEnvelope(503, body, "POST"), (error: unknown) => {
    assert.ok(error instanceof LifeUpHttpError)
    assert.equal(error.status, 503)
    assert.equal(error.code, 10003)
    assert.equal(error.message, "LifeUp Cloud is busy; request not started. Retry later; the command was not executed.")
    return true
  })
})

test("non-10003 HTTP 503 keeps the generic HTTP error", () => {
  const bodies = [
    "busy",
    "",
    "<html>",
    "10003",
    JSON.stringify({ code: 10002, message: "cp down", data: null }),
    JSON.stringify({ code: "10003", message: "nope", data: null }),
  ]
  for (const body of bodies) {
    for (const method of ["GET", "POST"] as const) {
      assert.throws(() => decodeEnvelope(503, body, method), (error: unknown) => {
        assert.ok(error instanceof LifeUpHttpError)
        assert.equal(error.status, 503)
        assert.equal(error.code, undefined)
        assert.equal(error.message, "HTTP 503")
        return true
      })
    }
  }
})
