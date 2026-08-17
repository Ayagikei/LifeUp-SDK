import assert from "node:assert/strict"
import { test } from "node:test"
import { listRequest, presentList } from "../src/query.ts"

test("tasks path uses categoryId; history is server-paged", () => {
  assert.equal(listRequest({ resource: "tasks", categoryId: 3 }).path, "/tasks/3")
  const history = listRequest({ resource: "history", offset: 20, limit: 10, gid: 8 })
  assert.equal(history.path, "/history")
  assert.equal(history.serverPaged, true)
  assert.deepEqual(history.query, { offset: 20, limit: 10, gid: 8 })
})

test("compacts and client-pages full lists", () => {
  const raw = [
    { id: 1, name: "a", notes: "long", icon: "x", status: 0 },
    { id: 2, name: "b", notes: "long", icon: "y", status: 1 },
    { id: 3, name: "c", notes: "long", icon: "z", status: 0 },
  ]
  const page = presentList("tasks", raw, { offset: 1, limit: 1 })
  assert.equal(page.total, 3)
  assert.equal(page.count, 1)
  assert.equal(page.hasMore, true)
  assert.deepEqual(page.items, [{ id: 2, name: "b", status: 1 }])
})

test("detail keeps extra fields; server page has no total", () => {
  const raw = [{ id: 1, name: "h", notes: "keep", endTime: 9 }]
  const detailed = presentList("history", raw, { detail: true, limit: 20 })
  assert.equal(detailed.total, undefined)
  assert.deepEqual(detailed.items[0], raw[0])
})

test("items?id= repeats keys", () => {
  const request = listRequest({ resource: "items", ids: [1, 4] })
  assert.deepEqual(request.query, { id: [1, 4] })
})

test("feelings compact keeps Cloud isFav", () => {
  const page = presentList(
    "feelings",
    [{ id: 1, content: "hi", time: 9, isFav: true, title: "t" }],
    {},
  )
  assert.deepEqual(page.items, [{ id: 1, content: "hi", time: 9, isFav: true }])
})

