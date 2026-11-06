import assert from "node:assert/strict"
import { readFileSync } from "node:fs"
import { test } from "node:test"
import { InMemoryTransport, type JSONRPCMessage } from "@modelcontextprotocol/server"
import { createServer } from "../src/index.ts"

test("initialize reports the published package version", { timeout: 5000 }, async (t) => {
  const { version } = JSON.parse(readFileSync(new URL("../package.json", import.meta.url), "utf8"))
  const server = createServer()
  const [client, transport] = InMemoryTransport.createLinkedPair()
  t.after(async () => {
    await server.close()
    await client.close()
  })
  const response = new Promise<JSONRPCMessage>((resolve) => {
    client.onmessage = resolve
  })
  await client.start()
  await server.connect(transport)
  await client.send({
    jsonrpc: "2.0",
    id: 1,
    method: "initialize",
    params: {
      protocolVersion: "2025-11-25",
      capabilities: {},
      clientInfo: { name: "version-test", version: "1.0.0" },
    },
  })
  const message = await response
  assert.ok("result" in message)
  assert.equal(message.result.serverInfo.version, version)
})
