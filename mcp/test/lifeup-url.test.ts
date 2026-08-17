import assert from "node:assert/strict"
import { test } from "node:test"
import { buildLifeUpUrl } from "../src/lifeup-url.ts"

test("encodes query values", () => {
  assert.equal(
    buildLifeUpUrl("complete", { name: "早起 & 喝水", ui: false }),
    "lifeup://api/complete?name=%E6%97%A9%E8%B5%B7+%26+%E5%96%9D%E6%B0%B4&ui=false",
  )
})

test("repeats array params", () => {
  const url = buildLifeUpUrl("reward", { type: "exp", content: "x", number: 1, skills: [2, 6] })
  assert.match(url, /skills=2/)
  assert.match(url, /skills=6/)
})

test("keeps slashed method names", () => {
  assert.equal(buildLifeUpUrl("loot_box/v2"), "lifeup://api/loot_box/v2")
})
