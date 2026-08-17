import assert from "node:assert/strict"
import { test } from "node:test"
import { readHelp } from "../src/help.ts"

test("bundled skill files load", async () => {
  const overview = await readHelp("overview")
  assert.match(overview, /lifeup_connect/)
  const basics = await readHelp("basics")
  assert.match(basics, /MCP encodes/)
  const index = await readHelp("api-index")
  assert.match(index, /delete_task/)

})
