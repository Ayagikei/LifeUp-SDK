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
  tasks: ["id", "gid", "name", "status", "categoryId", "frequency", "weekdays", "coin", "exp", "deadline", "countProgress", "repeatEndCondition", "isFrozen"],
  history: ["id", "gid", "name", "status", "endTime", "coin", "exp", "countProgress", "isFrozen"],
  items: ["id", "name", "categoryId", "price", "ownNumber", "stockNumber", "disablePurchase", "maxPurchaseNumber", "titleColorString", "actionText", "unlist", "disableUse", "purchaseLimit", "limitScope"],
  skills: ["id", "name", "level", "exp", "untilNextLevelExp"],
  achievements: ["id", "name", "categoryId", "status", "progress", "exp", "coin", "color"],
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

export const SMART_LIST_TYPES = ["daily", "weekly", "monthly"] as const
export type SmartListTypeName = (typeof SMART_LIST_TYPES)[number]

const SMART_LIST_TYPE_SET: ReadonlySet<string> = new Set(SMART_LIST_TYPES)

export type ListArgs = {
  resource: ListResource
  categoryId?: number
  smartListType?: SmartListTypeName
  ids?: number[]
  gid?: number
  offset?: number
  limit?: number
  detail?: boolean
  timeRangeStart?: number
  timeRangeEnd?: number
  includeHidden?: boolean
}

export function taskQueryConflict(args: {
  resource?: string
  categoryId?: number
  smartListType?: string
}): string | undefined {
  if (args.smartListType == null) return undefined
  if (!SMART_LIST_TYPE_SET.has(args.smartListType)) {
    return "smartListType must be daily, weekly, or monthly"
  }
  if (args.resource != null && args.resource !== "tasks") {
    return "smartListType is only valid for tasks"
  }
  if (args.categoryId != null) {
    return "smartListType and categoryId are mutually exclusive"
  }
  return undefined
}

export function listRequest(args: ListArgs): {
  path: string
  query: Record<string, string | number | number[] | undefined>
  serverPaged: boolean
  singular: boolean
} {
  const conflict = taskQueryConflict(args)
  if (conflict) throw new Error(conflict)
  const offset = Math.max(args.offset ?? 0, 0)
  const limit = resolveListLimit(args.limit).limit
  const serverPaged = SERVER_PAGED.has(args.resource)
  const singular = SINGULAR.has(args.resource)
  const page = serverPaged ? { offset, limit } : {}

  switch (args.resource) {
    case "tasks":
      return {
        path: args.smartListType
          ? `/smart_lists/${args.smartListType}/tasks`
          : args.categoryId == null
            ? "/tasks"
            : `/tasks/${args.categoryId}`,
        query: taskListQuery(args.detail),
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

function resolveListLimit(requested: number | undefined): {
  limit: number
  limitCapped?: true
  requestedLimit?: number
} {
  const specified = typeof requested === "number" ? requested : undefined
  const limit = Math.min(Math.max(specified ?? 20, 1), 100)
  if (specified !== undefined && (specified > 100 || specified < 1)) {
    return { limit, limitCapped: true, requestedLimit: specified }
  }
  return { limit }
}

function taskListQuery(detail: boolean | undefined): Record<string, string | number | number[] | undefined> {
  return detail === true ? {} : { withSubTasks: "false" }
}

export function presentList(
  resource: ListResource,
  raw: unknown,
  args: Pick<ListArgs, "offset" | "limit" | "detail">,
) {
  const offset = Math.max(args.offset ?? 0, 0)
  const { limit, ...limitCap } = resolveListLimit(args.limit)
  const detail = args.detail === true

  if (SINGULAR.has(resource)) {
    return { ok: true, code: 200, source: "contentprovider", resource, data: raw, ...limitCap }
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
    ...limitCap,
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
