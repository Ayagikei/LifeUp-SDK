import { readFile } from "node:fs/promises"
import { dirname, join, resolve, sep } from "node:path"
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
  "query-index",
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
  "query-index": "references/query-index.md",
}

// Keep aligned with ANCHOR_RE in scripts/extract-wiki-queries.py.
const QUERY_SECTION_TOPIC = /^query\/([a-z0-9]+(?:-[a-z0-9]+)*)$/

export const HELP_TOOL_DESCRIPTION =
  "Read bundled LifeUp docs. Omit topic for the workflow. api-index then a method name for params. HTTP query parameters and response models: query-index, then query/<section> (e.g. query/task-history-models, query/task-queries). Also basics|discovery|query|tasks|economy|sample_icons|item_structures|qr_scanning|gaps|broadcasts."

export const HELP_TOPIC_DESCRIPTION =
  "overview (default), discovery, basics, query, tasks, economy, api-index, a method name from api-index, sample_icons, item_structures, qr_scanning, gaps, broadcasts, query-index, or query/<section> such as query/task-history-models or query/task-queries."

export function skillsDir(): string {
  return join(dirname(fileURLToPath(import.meta.url)), "..", "skills", "lifeup-cloud")
}

export function methodDocSlug(method: string): string {
  return method.replaceAll("/", "-")
}

function unknownHelpTopic(topic: string): Error {
  return new Error(
    `Unknown help topic "${topic}". Use ${HELP_TOPICS.join("|")}, query/<section> from query-index (e.g. query/task-history-models), or a method from api-index.`,
  )
}

function querySectionPath(id: string): string {
  const root = resolve(skillsDir(), "references", "queries")
  const file = resolve(root, `${id}.md`)
  if (!file.startsWith(root + sep)) throw unknownHelpTopic(`query/${id}`)
  return file
}

export async function readHelp(topic: string): Promise<string> {
  if (
    topic.includes("..") ||
    topic.includes("\\") ||
    topic.includes("\0") ||
    topic.includes("%") ||
    topic.startsWith("/")
  ) {
    throw unknownHelpTopic(topic)
  }
  if ((HELP_TOPICS as readonly string[]).includes(topic)) {
    return readFile(join(skillsDir(), FILES[topic as (typeof HELP_TOPICS)[number]]), "utf8")
  }
  const querySection = QUERY_SECTION_TOPIC.exec(topic)
  if (querySection) {
    try {
      return await readFile(querySectionPath(querySection[1]), "utf8")
    } catch (error) {
      if ((error as NodeJS.ErrnoException).code === "ENOENT") throw unknownHelpTopic(topic)
      throw error
    }
  }
  if (KNOWN_METHODS.includes(topic as KnownMethod)) {
    return readFile(join(skillsDir(), "references", "methods", `${methodDocSlug(topic)}.md`), "utf8")
  }
  throw unknownHelpTopic(topic)
}

export const SERVER_INSTRUCTIONS = `LifeUp Cloud MCP. Phone must run LifeUp + LifeUp Cloud on the same LAN.

Workflow:
1. discover (auto-connects when one Cloud is on LAN) or connect { host }. Token only if Cloud set one; Authorization is the raw token, not Bearer.
2. Query with list_data / list_* (GET ContentProvider). Compact, prefer categoryId, page with hasMore. detail=true only when needed. Response fields: help query-index, then query/<section>. Feelings/achievements/synthesis/pomodoro: list_data resource=...
3. Mutate with complete_task / add_task / reward / purchase_item. Read calls[].ok and calls[].data.
4. Long-tail: call_api or call_api_batch. Default via=contentprovider (has results). via=launch is UI-only. Destructive methods need confirm=true.
5. Docs: help (no topic) then help api-index / method name / basics / sample_icons / item_structures / qr_scanning. HTTP parameters and response models: help query-index, then query/<section> (e.g. query/task-history-models, query/task-queries). Pass raw param values — MCP encodes.

Do not dump every task into context. id changes for repeating tasks; gid stays. Wiki may lag; Cloud HTTP is source of truth.`
