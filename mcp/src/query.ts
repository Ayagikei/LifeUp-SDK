export const LIST_RESOURCES = [
  "tasks",
  "task_categories",
  "history",
  "items",
  "item_categories",
  "skills",
  "coin",
  "info",
  "achievements",
  "achievement_categories",
  "feelings",
  "synthesis",
  "synthesis_categories",
  "pomodoro_records",
  "skill_groups",
  "achievement_conditions",
  "coin_records",
  "inventory_records",
  "exp_records",
  "step_records",
  "level_defines",
  "statistics",
] as const

export type ListResource = (typeof LIST_RESOURCES)[number]

const SERVER_PAGED = new Set<ListResource>(["history", "feelings", "pomodoro_records", "coin_records", "inventory_records", "exp_records", "step_records"])
const SINGULAR = new Set<ListResource>(["coin", "info", "level_defines", "statistics"])

const COMPACT: Record<string, string[]> = {
  tasks: ["id", "gid", "name", "status", "categoryId", "frequency", "coin", "exp", "deadline", "countProgress", "repeatEndCondition"],
  history: ["id", "gid", "name", "status", "endTime", "coin", "exp", "countProgress"],
  items: ["id", "name", "categoryId", "price", "ownNumber", "stockNumber", "disablePurchase", "maxPurchaseNumber"],
  skills: ["id", "name", "level", "exp", "untilNextLevelExp"],
  achievements: ["id", "name", "categoryId", "status", "progress", "exp", "coin"],
  feelings: ["id", "content", "time", "isFav"],
  synthesis: ["id", "name", "categoryId", "canSynthesisTimes"],

  pomodoro_records: ["id", "startTime", "endTime", "duration", "reward"],
  task_categories: ["id", "name", "type", "order", "status"],
  item_categories: ["id", "name", "order", "hidden", "inventoryHidden"],
  achievement_categories: ["id", "name", "order"],
  synthesis_categories: ["id", "name", "order", "hidden"],
  skill_groups: ["id", "content", "order", "collapsed"],
  achievement_conditions: ["id", "type", "relatedId", "target", "current", "progress"],
  coin_records: ["id", "time", "value", "isDecrease", "totalValue", "resCode", "content"],
  inventory_records: ["id", "time", "itemId", "itemName", "changeNumber", "isDecrease", "resCode"],
  exp_records: ["id", "time", "value", "isDecrease", "resCode", "skillIds", "relatedAttributes", "content"],
  step_records: ["id", "date", "dailyStepCount", "totalStepCount", "isGotReward"],
}

export type ListArgs = {
  resource: ListResource
  categoryId?: number
  ids?: number[]
  gid?: number
  offset?: number
  limit?: number
  detail?: boolean
  timeRangeStart?: number
  timeRangeEnd?: number
  includeHidden?: boolean
}

export function listRequest(args: ListArgs): {
  path: string
  query: Record<string, string | number | number[] | undefined>
  serverPaged: boolean
  singular: boolean
} {
  const offset = Math.max(args.offset ?? 0, 0)
  const limit = Math.min(Math.max(args.limit ?? 20, 1), 100)
  const serverPaged = SERVER_PAGED.has(args.resource)
  const singular = SINGULAR.has(args.resource)
  const page = serverPaged ? { offset, limit } : {}

  switch (args.resource) {
    case "tasks":
      return {
        path: args.categoryId == null ? "/tasks" : `/tasks/${args.categoryId}`,
        query: {},
        serverPaged,
        singular,
      }
    case "task_categories":
      return { path: "/tasks_categories", query: {}, serverPaged, singular }
    case "history":
      return { path: "/history", query: { ...page, gid: args.gid }, serverPaged, singular }
    case "items":
      if (args.ids?.length) {
        return { path: "/items", query: { id: args.ids }, serverPaged, singular }
      }
      return {
        path: args.categoryId == null ? "/items" : `/items/${args.categoryId}`,
        query: {},
        serverPaged,
        singular,
      }
    case "item_categories":
      return {
        path: "/items_categories",
        query: { include_hidden: args.includeHidden ? "true" : undefined },
        serverPaged,
        singular,
      }
    case "skills":
      return { path: "/skills", query: {}, serverPaged, singular }
    case "skill_groups":
      return {
        path: "/skill_groups",
        query: { include_hidden: args.includeHidden ? "true" : undefined },
        serverPaged,
        singular,
      }
    case "coin":
      return { path: "/coin", query: {}, serverPaged, singular }
    case "info":
      return { path: "/info", query: {}, serverPaged, singular }
    case "achievements":
      return {
        path: args.categoryId == null ? "/achievements" : `/achievements/${args.categoryId}`,
        query: {},
        serverPaged,
        singular,
      }
    case "achievement_categories":
      return { path: "/achievement_categories", query: {}, serverPaged, singular }
    case "achievement_conditions": {
      const achievementId = Number(args.categoryId)
      if (!Number.isInteger(achievementId) || achievementId <= 0) {
        throw new Error("achievement_conditions needs categoryId = achievement id")
      }
      return {
        path: `/achievement_conditions/${achievementId}`,
        query: {},
        serverPaged,
        singular,
      }
    }
    case "feelings":
      return { path: "/feelings", query: page, serverPaged, singular }
    case "synthesis":
      return {
        path: args.categoryId == null ? "/synthesis" : `/synthesis/${args.categoryId}`,
        query: {},
        serverPaged,
        singular,
      }
    case "synthesis_categories":
      return {
        path: args.categoryId == null ? "/synthesis_categories" : `/synthesis_categories/${args.categoryId}`,
        query: { include_hidden: args.includeHidden ? "true" : undefined },
        serverPaged,
        singular,
      }
    case "pomodoro_records":
      return {
        path: "/pomodoro_records",
        query: { ...page, time_range_start: args.timeRangeStart, time_range_end: args.timeRangeEnd },
        serverPaged,
        singular,
      }
    case "coin_records":
    case "inventory_records":
    case "exp_records":
    case "step_records":
      return {
        path: `/${args.resource}`,
        query: { ...page, time_range_start: args.timeRangeStart, time_range_end: args.timeRangeEnd },
        serverPaged,
        singular,
      }
    case "level_defines":
      return { path: "/level_defines", query: {}, serverPaged, singular }
    case "statistics":
      return {
        path: "/statistics",
        query: { time_range_start: args.timeRangeStart, time_range_end: args.timeRangeEnd },
        serverPaged,
        singular,
      }
  }
}

export function presentList(
  resource: ListResource,
  raw: unknown,
  args: Pick<ListArgs, "offset" | "limit" | "detail">,
) {
  const offset = Math.max(args.offset ?? 0, 0)
  const limit = Math.min(Math.max(args.limit ?? 20, 1), 100)
  const detail = args.detail === true

  if (SINGULAR.has(resource)) {
    return { ok: true, code: 200, source: "contentprovider", resource, data: raw }
  }

  const all = Array.isArray(raw) ? raw : []
  const serverPaged = SERVER_PAGED.has(resource)
  const page = serverPaged ? all : all.slice(offset, offset + limit)
  const items = detail ? page : page.map((item) => compact(resource, item))
  const total = serverPaged ? undefined : all.length
  const hasMore = serverPaged ? page.length >= limit : offset + limit < all.length

  return {
    ok: true,
    code: 200,
    source: "contentprovider",
    resource,
    offset: serverPaged ? offset : offset,
    limit,
    total,
    count: items.length,
    hasMore,
    detail,
    items,
    note: detail
      ? "Full objects. Prefer detail=false and a categoryId."
      : "Compact rows. Pass detail=true only for the rows you need.",
  }
}

function compact(resource: ListResource, item: unknown): unknown {
  const keys = COMPACT[resource]
  if (!keys || !item || typeof item !== "object") return item
  const rec = item as Record<string, unknown>
  const out: Record<string, unknown> = {}
  for (const key of keys) {
    if (rec[key] !== undefined) out[key] = rec[key]
  }
  return out
}
