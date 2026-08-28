import assert from "node:assert/strict"
import { test } from "node:test"
import { LifeUpClient, type FetchLike } from "../src/client.ts"

function fakeFetch(body: unknown): FetchLike {
  return async () => ({
    status: 200,
    async text() {
      return JSON.stringify({ code: 200, message: "success", data: body })
    },
  })
}

test("listEvents decodes GET /events page", async () => {
  const page = {
    latestId: 2,
    eventWs: false,
    events: [{ id: 2, time: 1, action: "app.lifeup.task.complete", extras: { task_id: "1" } }],
  }
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, fakeFetch(page))
  assert.deepEqual(await client.listEvents(1, 50), page)
})

test("listEvents keeps optional broadcasts", async () => {
  const page = { latestId: 0, eventWs: true, events: [], broadcasts: false }
  const client = new LifeUpClient("127.0.0.1", 13276, undefined, fakeFetch(page))
  assert.deepEqual(await client.listEvents(0, 50), page)
})

test("setEventSubscription copies broadcasts on all branches", async () => {
  const { Session } = await import("../src/session.ts")
  const page = { latestId: 3, eventWs: false, events: [], broadcasts: true }
  const session = new Session()
  session.client = {
    listEvents: async () => page,
    subscribeEvents: () => ({ close() {} }),
  } as never
  assert.equal((await session.setEventSubscription(false, 0)).broadcasts, true)
  assert.equal((await session.setEventSubscription(true, 0)).broadcasts, true)
  page.eventWs = true
  assert.equal((await session.setEventSubscription(true, 0)).broadcasts, true)
})
