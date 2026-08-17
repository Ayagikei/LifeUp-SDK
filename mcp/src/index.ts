#!/usr/bin/env node
import { McpServer } from "@modelcontextprotocol/server"
import { serveStdio } from "@modelcontextprotocol/server/stdio"
import { SERVER_INSTRUCTIONS, readHelp, skillsDir, type HelpTopic } from "./help.js"
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
  registerSkillResources(server)
  return server
}

function registerSkillResources(server: McpServer): void {
  const resources: Array<{ name: string; uri: string; topic: HelpTopic }> = [
    { name: "skill-overview", uri: "lifeup://skill/SKILL.md", topic: "overview" },
    { name: "skill-discovery", uri: "lifeup://skill/discovery.md", topic: "discovery" },
    { name: "skill-basics", uri: "lifeup://skill/basics.md", topic: "basics" },
    { name: "skill-query", uri: "lifeup://skill/query.md", topic: "query" },

    { name: "skill-tasks", uri: "lifeup://skill/tasks.md", topic: "tasks" },
    { name: "skill-economy", uri: "lifeup://skill/economy.md", topic: "economy" },
    { name: "skill-api-index", uri: "lifeup://skill/api-index.md", topic: "api-index" },
  ]
  for (const resource of resources) {
    server.registerResource(
      resource.name,
      resource.uri,
      { title: resource.name, mimeType: "text/markdown" },
      async (uri) => ({
        contents: [{ uri: uri.href, mimeType: "text/markdown", text: await readHelp(resource.topic) }],
      }),
    )
  }
}

export { createServer, skillsDir }

if (process.argv[1] && (import.meta.url === `file://${process.argv[1]}` || process.argv[1].endsWith("index.js"))) {
  serveStdio(() => createServer())
}

