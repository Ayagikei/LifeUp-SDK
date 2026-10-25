import assert from "node:assert/strict"
import { readFileSync } from "node:fs"
import { test } from "node:test"
import { z } from "zod"
import {
  HELP_TOOL_DESCRIPTION,
  HELP_TOPIC_DESCRIPTION,
  SERVER_INSTRUCTIONS,
  readHelp,
} from "../src/help.ts"

test("query-index is the HTTP response catalog", async () => {
  const index = await readHelp("query-index")
  assert.match(index, /`query\/task-history-models`/)
  assert.match(index, /`query\/task-queries`/)
  assert.match(index, /`task-fields`/)
  assert.doesNotMatch(index, /`query\/task-fields`/)
  assert.match(index, /1\.106\.0\+/)
  assert.match(index, /https:\/\/docs\.lifeupapp\.fun\/en\/#\/guide\/api-reference/)
  assert.doesNotMatch(index, /\]\(\/guide/)
  assert.doesNotMatch(index, /\]\(#/)
})

test("query sections preserve nested models and rewrite wiki links", async () => {
  const models = await readHelp("query/task-history-models")
  assert.match(models, /Help topic: `query\/task-history-models`/)
  assert.match(models, /countProgress/)
  assert.match(models, /repeatEndCondition/)
  assert.match(models, /### Task \/ HistoryRecord fields :id=task-fields/)
  assert.match(models, /https:\/\/docs\.lifeupapp\.fun\/en\/#\/guide\/api-reference\?id=task-times/)
  assert.match(models, /help topic query\/task-queries/)
  assert.doesNotMatch(models, /\]\(\/guide/)
  assert.doesNotMatch(models, /\]\(#/)

  const queries = await readHelp("query/task-queries")
  assert.match(queries, /GET \/history/)
  assert.match(queries, /GET \/tasks/)
  assert.match(queries, /### Build a daily report/)
  assert.doesNotMatch(queries, /### ShopItem/)
})

test("query topics do not collide with workflow docs or API methods", async () => {
  const workflow = await readHelp("query")
  assert.match(workflow, /Progressive disclosure/)
  assert.match(workflow, /list_data/)
  assert.doesNotMatch(workflow, /Help topic: `query\//)

  const tasks = await readHelp("tasks")
  assert.match(tasks, /complete_task/)

  const addTask = await readHelp("add_task")
  assert.match(addTask, /\*\*Method name:\*\* add_task/)

  const loot = await readHelp("loot_box/v2")
  assert.match(loot, /loot_box\/v2/)

  const overview = await readHelp("overview")
  assert.match(overview, /`discover`/)
})

test("traversal and unknown query topics are rejected", async () => {
  const topics = [
    "query/../tasks",
    "query/../../skills/lifeup-cloud/SKILL.md",
    "query/task-history-models/extra",
    "query/no-such-section",
    "query/task-fields",
    "query/Task-History-Models",
    "query/task-history-models.md",
    "../SKILL.md",
    "query/%2e%2e/tasks",
    "/etc/passwd",
  ]
  for (const topic of topics) {
    await assert.rejects(() => readHelp(topic), /Unknown help topic/)
  }
})

test("help schema and instructions name callable query topics", () => {
  assert.match(HELP_TOOL_DESCRIPTION, /query-index/)
  assert.match(HELP_TOOL_DESCRIPTION, /query\/<section>/)
  assert.match(HELP_TOOL_DESCRIPTION, /query\/task-history-models/)
  assert.match(HELP_TOOL_DESCRIPTION, /query\/task-queries/)
  assert.match(HELP_TOPIC_DESCRIPTION, /query-index/)
  assert.match(HELP_TOPIC_DESCRIPTION, /query\/task-queries/)
  assert.match(SERVER_INSTRUCTIONS, /query-index/)
  assert.match(SERVER_INSTRUCTIONS, /query\/<section>/)
  assert.match(SERVER_INSTRUCTIONS, /query\/task-history-models/)

  const tools = readFileSync(new URL("../src/tools.ts", import.meta.url), "utf8")
  assert.match(tools, /description: HELP_TOOL_DESCRIPTION/)
  assert.match(tools, /describe\(HELP_TOPIC_DESCRIPTION\)/)

  const json = JSON.stringify(z.toJSONSchema(z.string().optional().describe(HELP_TOPIC_DESCRIPTION)))
  assert.match(json, /query-index/)
  assert.match(json, /query\/task-history-models/)
})

test("skill docs ship inside the npm package", () => {
  const pkg = JSON.parse(readFileSync(new URL("../package.json", import.meta.url), "utf8")) as { files: string[] }
  assert.ok(pkg.files.includes("skills"))
  const section = readFileSync(
    new URL("../skills/lifeup-cloud/references/queries/task-queries.md", import.meta.url),
    "utf8",
  )
  assert.match(section, /GET \/history/)
})
