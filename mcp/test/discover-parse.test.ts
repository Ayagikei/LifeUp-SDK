import assert from "node:assert/strict"
import { test } from "node:test"
import { parseCloudService } from "../src/discover.ts"

test("reads TXT port and prefers IPv4", () => {
  assert.deepEqual(
    parseCloudService({
      name: "lifeup_cloud",
      addresses: ["fe80::1", "192.168.1.8"],
      txt: { port: "13276" },
    }),
    { host: "192.168.1.8", port: 13276, name: "lifeup_cloud" },
  )
})

test("rejects missing name, port, or address", () => {
  assert.equal(parseCloudService({ name: "other", txt: { port: "13276" }, addresses: ["1.1.1.1"] }), null)
  assert.equal(parseCloudService({ name: "lifeup_cloud", txt: {}, addresses: ["1.1.1.1"] }), null)
  assert.equal(parseCloudService({ name: "lifeup_cloud", txt: { port: "13276" }, addresses: [] }), null)
})
