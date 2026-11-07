#!/usr/bin/env node
import { readFileSync } from "node:fs"
import { McpServer } from "@modelcontextprotocol/server"
import { serveStdio } from "@modelcontextprotocol/server/stdio"
import { resolveRequestTimeoutMs } from "./config.js"
import { SERVER_INSTRUCTIONS, skillsDir } from "./help.js"
import { Session } from "./session.js"
import { registerTools } from "./tools.js"

const { version } = JSON.parse(readFileSync(new URL("../package.json", import.meta.url), "utf8"))

function createServer(): McpServer {
  const server = new McpServer({
    name: "lifeup",
    version,
    title: "LifeUp Cloud",
    description: SERVER_INSTRUCTIONS,
  })

  const session = new Session()
  registerTools(server, session)
  return server
}


export { createServer, skillsDir }

if (process.argv[1] && (import.meta.url === `file://${process.argv[1]}` || process.argv[1].endsWith("index.js"))) {
  try {
    resolveRequestTimeoutMs()
  } catch (error) {
    console.error(error instanceof Error ? error.message : String(error))
    process.exit(1)
  }
  serveStdio(() => createServer())
}

