import assert from "node:assert/strict"
import { test } from "node:test"
import { withDiscoveredName } from "../src/session.ts"

test("connect host keeps mDNS name when it matches discover", () => {
  const parsed = { host: "192.168.31.20", port: 13276, name: "direct" }
  const found = [{ host: "192.168.31.20", port: 13276, name: "lifeup_cloud" }]
  assert.deepEqual(withDiscoveredName(parsed, found), found[0])
})

test("unknown host stays direct", () => {
  const parsed = { host: "10.0.0.2", port: 13276, name: "direct" }
  assert.equal(withDiscoveredName(parsed, []).name, "direct")
})
