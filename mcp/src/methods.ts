export const KNOWN_METHODS = [
  "toast",
  "reward",
  "penalty",
  "edit_coin",
  "add_task",
  "complete",
  "give_up",
  "freeze",
  "unfreeze",
  "delete_task",
  "edit_task",
  "task_template",
  "history_operation",
  "shop_settings",
  "goto",
  "add_item",
  "item",
  "loot_box",
  "loot_box/v2",
  "use_item",
  "deposit",
  "withdraw",
  "pomodoro_timer",
  "add_pomodoro",
  "edit_pomodoro",
  "unlock_condition",
  "step",
  "edit_exp",
  "feeling",
  "tomato",
  "purchase_item",
  "synthesize",
  "synthesis_formula",
  "subtask",
  "subtask_operation",
  "category",
  "achievement",
  "skill",
  "skill_group",
  "export_backup",
  "app_settings",
  "query",
  "query_skill",
  "query_skill_group",
  "random",
  "confirm_dialog",
  "placeholder",
] as const

export type KnownMethod = (typeof KNOWN_METHODS)[number]

export const DESTRUCTIVE_METHODS = new Set<string>([
  "delete_task",
  "edit_coin",
  "export_backup",
  "history_operation",
])

function flagTrue(value: unknown): boolean {
  return value === true || value === "true"
}

export function isDestructiveCall(
  method: string,
  params: Record<string, unknown> = {},
): boolean {
  if (DESTRUCTIVE_METHODS.has(method)) return true
  if (method === "task_template" && params.method === "delete") return true
  if (method === "subtask_operation" && params.operation === "delete") return true
  if (
    (method === "synthesis_formula" ||
      method === "skill" ||
      method === "skill_group" ||
      method === "achievement") &&
    flagTrue(params.delete)
  ) {
    return true
  }
  return false
}

export function assertCallableMethod(
  method: string,
  confirm?: boolean,
  params: Record<string, unknown> = {},
): void {
  if (!KNOWN_METHODS.includes(method as KnownMethod)) {
    throw new Error(`Unknown LifeUp API method: ${method}. Use help topic=api-index.`)
  }
  if (isDestructiveCall(method, params) && confirm !== true) {
    throw new Error(`${method} is destructive. Pass confirm=true to proceed.`)
  }

}
