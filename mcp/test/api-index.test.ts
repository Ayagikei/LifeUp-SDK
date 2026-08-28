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
  assert.match(addTask, /weekdays/)
  assert.match(addTask, /repeat_end_behavior/)
  const v2 = await readHelp("loot_box/v2")
  assert.match(v2, /loot_box\/v2/)
})

test("sample_icons help returns the built-in icon catalog", async () => {
  const icons = await readHelp("sample_icons")
  assert.match(icons, /lifeup_sample_\*/)
  assert.match(icons, /lifeup_sample_1\.png/)
  assert.match(icons, /lifeup_sample_208\.png/)
})

test("item_structures help returns purchase limits and effect types", async () => {
  const structures = await readHelp("item_structures")
  assert.match(structures, /Item Reward Structure/)
  assert.match(structures, /Purchase Limit Structure/)
  assert.match(structures, /Item Effects Structure/)
  assert.match(structures, /lifeup:\/\/api/)
  assert.match(structures, /\| 9 \| Link/)
  assert.match(structures, /item_id/)
})

test("qr_scanning help documents Cloud scan and non-lifeup schemes", async () => {
  const qr = await readHelp("qr_scanning")
  assert.match(qr, /not limited to LifeUp/)
  assert.match(qr, /lifeup:\/\/api/)
  assert.match(qr, /weixin:\/\//)
  assert.match(qr, /Not QR scanning/)
})

test("unknown help topic is rejected", async () => {
  await assert.rejects(() => readHelp("explode"), /Unknown help topic/)
})
