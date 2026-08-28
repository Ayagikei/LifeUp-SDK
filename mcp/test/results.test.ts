import assert from "node:assert/strict"
import { test } from "node:test"
import { KNOWN_METHODS } from "../src/methods.ts"
import { KIND, methodFromUrl, presentCalls } from "../src/results.ts"

test("void APIs treat a null bundle as success", () => {
  const presented = presentCalls("contentprovider", [
    { url: "lifeup://api/complete?id=1&ui=false", result: null },
    { url: "lifeup://api/toast?text=hi", result: null },
  ])
  assert.equal(presented.calls[0].ok, true)
  assert.equal(presented.calls[0].method, "complete")
  assert.deepEqual(presented.calls[1].data, {})
})

test("payload APIs keep the full return, not just ids", () => {
  const presented = presentCalls("contentprovider", [
    { url: "lifeup://api/add_task?todo=x", result: { task_id: 9, task_gid: 9 } },
    { url: "lifeup://api/query?key=coin", result: { value: 42 } },
    { url: "lifeup://api/tomato?number=1", result: { tomatoes: 10 } },
    { url: "lifeup://api/query?key=item", result: { item_id: 1, skillIds: "[1,2]", own_number: 3 } },
  ])
  assert.deepEqual(presented.calls[0].data, { task_id: 9, task_gid: 9 })
  assert.equal((presented.calls[1].data as { value: number }).value, 42)
  assert.equal((presented.calls[2].data as { tomatoes: number }).tomatoes, 10)
  assert.deepEqual((presented.calls[3].data as { skillIds: number[] }).skillIds, [1, 2])
})

test("status codes are per-method", () => {
  const presented = presentCalls("contentprovider", [
    { url: "lifeup://api/purchase_item?id=1", result: { result: 4, desc: "PurchaseAndUseSuccess", itemId: 1 } },
    { url: "lifeup://api/use_item?id=1", result: { result: 4, desc: "RunningCountDown" } },
    { url: "lifeup://api/purchase_item?id=1", result: { result: 2, desc: "NotEnoughCoin" } },
    { url: "lifeup://api/deposit?amount=1", result: { result: false } },
    {
      url: "lifeup://api/pomodoro_timer?action=status",
      result: { api_result: false, error_code: "background_start_not_allowed" },
    },
  ])
  assert.equal(presented.calls[0].ok, true)
  assert.equal((presented.calls[0].data as { itemId: number }).itemId, 1)
  assert.equal(presented.calls[1].ok, false)
  assert.equal(presented.calls[1].error, "RunningCountDown")
  assert.equal(presented.calls[2].ok, false)
  assert.equal(presented.calls[3].ok, false)
  assert.equal(presented.calls[4].ok, false)
  assert.match(String(presented.calls[4].error), /background_start_not_allowed/)
})

test("empty payload result is a failure", () => {
  const presented = presentCalls("contentprovider", [
    { url: "lifeup://api/add_task?todo=x", result: null },
  ])
  assert.equal(presented.calls[0].ok, false)
})

test("task_template success=false is not ok", () => {
  const presented = presentCalls("contentprovider", [
    { url: "lifeup://api/task_template?method=delete&id=1", result: { success: false, id: 1 } },
  ])
  assert.equal(presented.calls[0].ok, false)
})


test("launch has no data", () => {
  const presented = presentCalls("launch", [{ url: "lifeup://api/goto?page=task", result: null }])
  assert.equal(presented.calls[0].ok, true)
  assert.equal(presented.calls[0].data, null)
})

test("every known method has a return kind", () => {
  for (const method of KNOWN_METHODS) {
    assert.ok(KIND[method], method)
    assert.equal(methodFromUrl(`lifeup://api/${method}?x=1`), method)
  }
})
