#!/usr/bin/env node
import { McpServer } from "@modelcontextprotocol/server"
import { serveStdio } from "@modelcontextprotocol/server/stdio"
import { SERVER_INSTRUCTIONS, skillsDir } from "./help.js"
import { Session } from "./session.js"
import { registerTools } from "./tools.js"

function createServer(): McpServer {
  const server = new McpServer({
    name: "lifeup",
    version: "0.1.0",
    title: "LifeUp Cloud",
    description: SERVER_INSTRUCTIONS,
  })

  const session = new Session()
  registerTools(server, session)
  return server
}


export { createServer, skillsDir }

if (process.argv[1] && (import.meta.url === `file://${process.argv[1]}` || process.argv[1].endsWith("index.js"))) {
  serveStdio(() => createServer())
}

