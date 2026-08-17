import assert from "node:assert/strict"
import { test } from "node:test"
import { assertCallableMethod } from "../src/methods.ts"
import { boundToken, parseHostInput, resolveToken, tokenToPersist } from "../src/config.ts"

test("unknown methods fail", () => {
  assert.throws(() => assertCallableMethod("explode"), /Unknown LifeUp API method/)
})

test("destructive methods need confirm", () => {
  assert.throws(() => assertCallableMethod("delete_task"), /confirm=true/)
  assert.doesNotThrow(() => assertCallableMethod("delete_task", true))
  assert.doesNotThrow(() => assertCallableMethod("complete"))
})

test("param-aware deletes need confirm", () => {
  assert.throws(() => assertCallableMethod("skill", false, { delete: true }), /confirm=true/)
  assert.throws(() => assertCallableMethod("task_template", false, { method: "delete", id: 1 }), /confirm=true/)
  assert.doesNotThrow(() => assertCallableMethod("skill", true, { delete: true }))
  assert.doesNotThrow(() => assertCallableMethod("skill", false, { id: 1, name: "x" }))
})

test("does not reuse token on a different host", () => {
  const endpoint = parseHostInput("10.0.0.2:13276")
  assert.equal(boundToken({ host: "10.0.0.1", port: 13276, token: "abc" }, endpoint), undefined)
  assert.equal(boundToken({ host: "10.0.0.2", port: 13276, token: "abc" }, endpoint), "abc")
})

test("empty token argument clears persist and does not send the old token", () => {
  const endpoint = parseHostInput("10.0.0.2:13276")
  const saved = { host: "10.0.0.2", port: 13276, token: "old" }
  assert.equal(resolveToken({ token: "" }, saved, endpoint), undefined)
  assert.equal(tokenToPersist({ token: "" }, saved, endpoint), undefined)
  assert.equal(resolveToken({}, saved, endpoint), "old")
  assert.equal(resolveToken({ token: undefined }, saved, endpoint), "old")
  assert.equal(tokenToPersist({ token: undefined }, saved, endpoint), "old")
})

