import assert from "node:assert/strict"
import { test } from "node:test"
import { cmpVersion, versionAdvice } from "../src/versions.ts"

test("1.105.1 and Cloud 2.1.2 are below the floor", () => {
  assert.ok(cmpVersion("1.105.1", "1.106.0") < 0)
  assert.ok(cmpVersion("1.106.0", "1.106.0") === 0)
  assert.ok(cmpVersion("3.0.0", "3.0.0") === 0)
  const notes = versionAdvice({ appVersionName: "1.105.1", cloudVersionName: "2.1.2" })
  assert.equal(notes.length, 2)
  assert.match(notes[0], /1\.106\.0/)
  assert.match(notes[1], /3\.0\.0/)
})

test("missing Cloud version is treated as outdated", () => {
  const notes = versionAdvice({ appVersionName: "1.106.0" })
  assert.equal(notes.length, 1)
  assert.match(notes[0], /Cloud/)
})

test("current floor produces no advice", () => {
  assert.deepEqual(versionAdvice({ appVersionName: "1.106.0", cloudVersionName: "3.0.0" }), [])
  assert.deepEqual(versionAdvice({ appVersionName: "1.107.0", cloudVersionName: "3.1.0" }), [])
})
