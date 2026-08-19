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
