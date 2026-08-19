import assert from "node:assert/strict"
import { test } from "node:test"
import { readHelp } from "../src/help.ts"
import { KNOWN_METHODS } from "../src/methods.ts"

test("api-index catalog lists every known method once", async () => {
  const index = await readHelp("api-index")
  assert.match(index, /Catalog only/)
  const listed = [...index.matchAll(/\| `([^`]+)` \|/g)].map((match) => match[1])
  assert.deepEqual([...listed].sort(), [...KNOWN_METHODS].sort())
})

test("method help returns the wiki param table on demand", async () => {
  const toast = await readHelp("toast")
  assert.match(toast, /\*\*Method name:\*\* toast/)
  assert.match(toast, /\| text\s+\|/)
  const addTask = await readHelp("add_task")
  assert.match(addTask, /todo/)
  assert.match(addTask, /repeat_end_behavior/)
  const v2 = await readHelp("loot_box/v2")
  assert.match(v2, /loot_box\/v2/)
})

test("unknown help topic is rejected", async () => {
  await assert.rejects(() => readHelp("explode"), /Unknown help topic/)
})
