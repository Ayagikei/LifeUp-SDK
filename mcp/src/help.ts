import { readFile } from "node:fs/promises"
import { dirname, join } from "node:path"
import { fileURLToPath } from "node:url"
import { KNOWN_METHODS, type KnownMethod } from "./methods.js"

export const HELP_TOPICS = [
  "overview",
  "discovery",
  "basics",
  "query",
  "tasks",
  "economy",
  "api-index",
  "sample_icons",
  "item_structures",
  "qr_scanning",
  "gaps",
  "broadcasts",
] as const

export type HelpTopic = (typeof HELP_TOPICS)[number] | KnownMethod

const FILES: Record<(typeof HELP_TOPICS)[number], string> = {
  overview: "SKILL.md",
  discovery: "references/discovery.md",
  basics: "references/basics.md",
  query: "references/query.md",
  tasks: "references/tasks.md",
  economy: "references/economy.md",
  "api-index": "references/api-index.md",
  sample_icons: "references/sample_icons.md",
  item_structures: "references/item_structures.md",
  qr_scanning: "references/qr_scanning.md",
  gaps: "references/gaps.md",
  broadcasts: "references/broadcasts.md",
}

export function skillsDir(): string {
  return join(dirname(fileURLToPath(import.meta.url)), "..", "skills", "lifeup-cloud")
}

export function methodDocSlug(method: string): string {
  return method.replaceAll("/", "-")
}

export async function readHelp(topic: string): Promise<string> {
  if ((HELP_TOPICS as readonly string[]).includes(topic)) {
    return readFile(join(skillsDir(), FILES[topic as (typeof HELP_TOPICS)[number]]), "utf8")
  }
  if (KNOWN_METHODS.includes(topic as KnownMethod)) {
    return readFile(join(skillsDir(), "references", "methods", `${methodDocSlug(topic)}.md`), "utf8")
  }
  throw new Error(
    `Unknown help topic "${topic}". Use overview|discovery|basics|query|tasks|economy|api-index|sample_icons|item_structures|qr_scanning|gaps|broadcasts, or a method from api-index.`,
  )
}

export const SERVER_INSTRUCTIONS = `LifeUp Cloud MCP. Phone must run LifeUp + LifeUp Cloud on the same LAN.

Workflow:
1. discover (auto-connects when one Cloud is on LAN) or connect { host }. Token only if Cloud set one; Authorization is the raw token, not Bearer.
2. Query with list_data / list_* (GET ContentProvider). Compact, prefer categoryId, page with hasMore. detail=true only when needed. Feelings/achievements/synthesis/pomodoro: list_data resource=...
3. Mutate with complete_task / add_task / reward / purchase_item. Read calls[].ok and calls[].data.
4. Long-tail: call_api or call_api_batch. Default via=contentprovider (has results). via=launch is UI-only. Destructive methods need confirm=true.
5. Docs: help (no topic) then help api-index / method name / basics / sample_icons / item_structures / qr_scanning. Pass raw param values — MCP encodes.

Do not dump every task into context. id changes for repeating tasks; gid stays. Wiki may lag; Cloud HTTP is source of truth.`
