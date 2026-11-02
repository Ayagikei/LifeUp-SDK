import assert from "node:assert/strict"
import { test } from "node:test"
import { listRequest, presentList } from "../src/query.ts"
import { listDataInputSchema, listTasksInputSchema } from "../src/tools.ts"
import { z } from "zod"

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

test("coin_records is server-paged", () => {
  const request = listRequest({ resource: "coin_records", offset: 10, limit: 5 })
  assert.equal(request.path, "/coin_records")
  assert.equal(request.serverPaged, true)
  assert.equal(request.query.offset, 10)
  assert.equal(request.query.limit, 5)
})

test("statistics and level_defines are singular", () => {
  assert.equal(listRequest({ resource: "statistics" }).singular, true)
  assert.equal(listRequest({ resource: "level_defines" }).path, "/level_defines")
  assert.equal(listRequest({ resource: "step_records" }).serverPaged, true)
})

test("achievement_conditions needs a positive categoryId", () => {
  assert.throws(() => listRequest({ resource: "achievement_conditions" }), /achievement id/)
  assert.equal(listRequest({ resource: "achievement_conditions", categoryId: 4 }).path, "/achievement_conditions/4")
})
test("feelings compact keeps Cloud isFav", () => {
  const page = presentList(
    "feelings",
    [{ id: 1, content: "hi", time: 9, isFav: true, title: "t" }],
    {},
  )
  assert.deepEqual(page.items, [{ id: 1, content: "hi", time: 9, isFav: true }])
})

test("smartListType routes only task lists and keeps client paging", () => {
  assert.equal(listRequest({ resource: "tasks", smartListType: "daily" }).path, "/smart_lists/daily/tasks")
  assert.equal(listRequest({ resource: "tasks", smartListType: "weekly" }).path, "/smart_lists/weekly/tasks")
  assert.equal(listRequest({ resource: "tasks", smartListType: "monthly" }).path, "/smart_lists/monthly/tasks")
  assert.deepEqual(listRequest({ resource: "tasks", smartListType: "daily", offset: 1, limit: 1 }).query, {})
  assert.equal(listRequest({ resource: "tasks", smartListType: "daily" }).serverPaged, false)
  assert.equal(listRequest({ resource: "tasks" }).path, "/tasks")
  assert.equal(listRequest({ resource: "tasks", categoryId: 0 }).path, "/tasks/0")

  assert.throws(
    () => listRequest({ resource: "tasks", categoryId: 0, smartListType: "daily" }),
    /mutually exclusive/,
  )
  assert.throws(
    () => listRequest({ resource: "tasks", smartListType: "Daily" as "daily" }),
    /daily, weekly, or monthly/,
  )
  for (const resource of ["items", "history", "statistics", "task_categories"] as const) {
    assert.throws(() => listRequest({ resource, smartListType: "monthly" }), /only valid for tasks/)
  }

  const raw = [
    {
      id: 1,
      gid: 9,
      name: "a",
      status: 0,
      categoryId: 2,
      frequency: 1,
      weekdays: "1",
      coin: 3,
      exp: 4,
      deadline: 5,
      countProgress: { currentCount: 1, targetCount: 2 },
      repeatEndCondition: { mode: "COUNT" },
      notes: "hide",
      subTasks: [{ todo: "x" }],
    },
    { id: 2, name: "b", status: 1, notes: "hide" },
  ]
  const page = presentList("tasks", raw, { offset: 1, limit: 1 })
  assert.equal(page.total, 2)
  assert.equal(page.count, 1)
  assert.equal(page.hasMore, false)
  assert.equal(page.detail, false)
  assert.deepEqual(page.items, [{ id: 2, name: "b", status: 1 }])
  const first = presentList("tasks", raw, { offset: 0, limit: 1 }).items[0] as Record<string, unknown>
  assert.deepEqual(first, {
    id: 1,
    gid: 9,
    name: "a",
    status: 0,
    categoryId: 2,
    frequency: 1,
    weekdays: "1",
    coin: 3,
    exp: 4,
    deadline: 5,
    countProgress: { currentCount: 1, targetCount: 2 },
    repeatEndCondition: { mode: "COUNT" },
  })
  assert.equal(first.notes, undefined)
  assert.equal(first.subTasks, undefined)
})

test("registered task schemas reject smartListType conflicts", () => {
  assert.equal(listTasksInputSchema.safeParse({ smartListType: "daily", offset: 0, limit: 20, detail: false }).success, true)
  assert.equal(listTasksInputSchema.safeParse({ categoryId: 0 }).success, true)
  assert.equal(listTasksInputSchema.safeParse({}).success, true)
  const both = listTasksInputSchema.safeParse({ categoryId: 0, smartListType: "weekly" })
  assert.equal(both.success, false)
  assert.match(JSON.stringify(both), /mutually exclusive/)
  assert.equal(listTasksInputSchema.safeParse({ smartListType: "Daily" }).success, false)

  assert.equal(listDataInputSchema.safeParse({ resource: "tasks", smartListType: "monthly" }).success, true)
  const items = listDataInputSchema.safeParse({ resource: "items", smartListType: "daily" })
  assert.equal(items.success, false)
  assert.match(JSON.stringify(items), /only valid for tasks/)
  assert.equal(listDataInputSchema.safeParse({ resource: "history", smartListType: "daily" }).success, false)
  assert.equal(listDataInputSchema.safeParse({ resource: "history", categoryId: 1 }).success, true)

  const json = JSON.stringify(z.toJSONSchema(listTasksInputSchema))
  assert.match(json, /"daily"/)
  assert.match(json, /"weekly"/)
  assert.match(json, /"monthly"/)
  assert.match(json, /categoryId/)
})

test("compact tasks and history keep isFrozen false, true, and null", () => {
  const tasks = [
    { id: 1, name: "open", status: 0, isFrozen: false, notes: "hide" },
    { id: 2, name: "frozen", status: 0, isFrozen: true, notes: "hide" },
    { id: 3, name: "unknown", status: 0, isFrozen: null, notes: "hide" },
    { id: 4, name: "legacy", status: 0, notes: "hide" },
  ]
  const page = presentList("tasks", tasks, { offset: 1, limit: 1 })
  assert.equal(page.total, 4)
  assert.equal(page.count, 1)
  assert.equal(page.hasMore, true)
  assert.equal(page.detail, false)
  assert.deepEqual(page.items, [{ id: 2, name: "frozen", status: 0, isFrozen: true }])

  const open = presentList("tasks", tasks, { offset: 0, limit: 1 })
  assert.equal(open.total, 4)
  assert.equal(open.count, 1)
  assert.equal(open.hasMore, true)
  assert.deepEqual(open.items, [{ id: 1, name: "open", status: 0, isFrozen: false }])

  const unknown = presentList("tasks", tasks, { offset: 2, limit: 2 })
  assert.equal(unknown.total, 4)
  assert.equal(unknown.count, 2)
  assert.equal(unknown.hasMore, false)
  assert.deepEqual(unknown.items, [
    { id: 3, name: "unknown", status: 0, isFrozen: null },
    { id: 4, name: "legacy", status: 0 },
  ])
  assert.equal(Object.hasOwn(unknown.items[1] as object, "isFrozen"), false)

  const detailed = presentList("tasks", tasks, { detail: true, offset: 0, limit: 1 })
  assert.equal(detailed.detail, true)
  assert.equal(detailed.count, 1)
  assert.equal(detailed.hasMore, true)
  assert.deepEqual(detailed.items[0], tasks[0])

  const history = [
    { id: 5, gid: 1, name: "done-open", status: 0, endTime: 9, coin: 1, exp: 2, countProgress: null, isFrozen: false, notes: "full" },
    { id: 6, gid: 1, name: "done-frozen", status: 0, endTime: 8, coin: 1, exp: 2, isFrozen: true, notes: "full" },
    { id: 7, gid: 1, name: "done-unknown", status: 0, endTime: 7, coin: 1, exp: 2, isFrozen: null, notes: "full" },
  ]
  const historyPage = presentList("history", history, { offset: 1, limit: 1 })
  assert.equal(historyPage.total, undefined)
  assert.equal(historyPage.count, 3)
  assert.equal(historyPage.hasMore, true)
  assert.equal(historyPage.offset, 1)
  assert.equal(historyPage.limit, 1)
  assert.deepEqual(historyPage.items, [
    { id: 5, gid: 1, name: "done-open", status: 0, endTime: 9, coin: 1, exp: 2, countProgress: null, isFrozen: false },
    { id: 6, gid: 1, name: "done-frozen", status: 0, endTime: 8, coin: 1, exp: 2, isFrozen: true },
    { id: 7, gid: 1, name: "done-unknown", status: 0, endTime: 7, coin: 1, exp: 2, isFrozen: null },
  ])

  const historyDetail = presentList("history", history, { detail: true, offset: 0, limit: 20 })
  assert.equal(historyDetail.detail, true)
  assert.equal(historyDetail.count, 3)
  assert.equal(historyDetail.hasMore, false)
  assert.deepEqual(historyDetail.items, history)
})
