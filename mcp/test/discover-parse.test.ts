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

test("uses TXT HTTP port, not the dummy NSD listen port", () => {
  // Live capture 2026-08-18: dns-sd showed Android_S74K12OK.local.:38017 + TXT port=13276
  assert.deepEqual(
    parseCloudService({
      name: "lifeup_cloud",
      addresses: ["fe80::b484:9ff:fec4:8634", "192.168.31.20"],
      txt: { port: "13276" },
    }),
    { host: "192.168.31.20", port: 13276, name: "lifeup_cloud" },
  )
})

test("rejects missing name, port, or address", () => {
  assert.equal(parseCloudService({ name: "other", txt: { port: "13276" }, addresses: ["1.1.1.1"] }), null)

  assert.equal(parseCloudService({ name: "lifeup_cloud", txt: {}, addresses: ["1.1.1.1"] }), null)
  assert.equal(parseCloudService({ name: "lifeup_cloud", txt: { port: "13276" }, addresses: [] }), null)
})

test("falls back to TXT ipv4 when addresses are empty", () => {
  assert.deepEqual(
    parseCloudService({
      name: "lifeup_cloud",
      addresses: [],
      txt: { port: "13276", ipv4: "192.168.14.166" },
    }),
    { host: "192.168.14.166", port: 13276, name: "lifeup_cloud" },
  )
})
