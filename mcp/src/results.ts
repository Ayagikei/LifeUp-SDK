import type { CallUrlResult } from "./client.js"

export type CallVia = "contentprovider" | "launch"

type ReturnKind = "void" | "payload" | "code" | "bool" | "flag"

const CODE_OK: Partial<Record<string, Set<number>>> = {
  purchase_item: new Set([0, 4]),
  use_item: new Set([0]),
  synthesize: new Set([0]),
  synthesis_formula: new Set([0]),
  app_settings: new Set([0]),
}

export const KIND: Record<string, ReturnKind> = {

  toast: "void",
  reward: "void",
  penalty: "void",
  edit_coin: "void",
  complete: "void",
  give_up: "void",
  freeze: "void",
  unfreeze: "void",
  delete_task: "void",
  shop_settings: "void",
  goto: "void",
  loot_box: "void",
  "loot_box/v2": "void",
  unlock_condition: "void",
  step: "void",
  edit_exp: "void",
  feeling: "void",
  confirm_dialog: "void",
  random: "void",
  placeholder: "void",
  item: "void",
  add_task: "payload",
  edit_task: "payload",
  add_item: "payload",
  task_template: "payload",
  history_operation: "payload",
  subtask: "payload",
  subtask_operation: "payload",
  category: "payload",
  achievement: "payload",
  skill: "payload",
  skill_group: "payload",
  export_backup: "payload",
  query: "payload",
  query_skill: "payload",
  query_skill_group: "payload",
  tomato: "payload",
  add_pomodoro: "payload",
  edit_pomodoro: "payload",
  purchase_item: "code",
  use_item: "code",
  synthesize: "code",
  synthesis_formula: "code",
  app_settings: "code",
  deposit: "bool",
  withdraw: "bool",
  pomodoro_timer: "flag",
}

export function methodFromUrl(url: string): string {
  return url.replace(/^lifeup:\/\/api\//, "").split("?")[0] ?? url
}

export function presentCalls(via: CallVia, rows: CallUrlResult[]) {
  return {
    via,
    note:
      via === "launch"
        ? "Launch has no return payload. Use via=contentprovider to get data."
        : "Read each call.ok and call.data (full API return). data is not just ids.",
    calls: rows.map((row) => presentOne(via, row)),
  }
}

function presentOne(via: CallVia, row: CallUrlResult) {
  const method = methodFromUrl(row.url)
  if (via === "launch") {
    return { url: row.url, method, ok: true, data: null }
  }
  return interpret(method, row.url, hydrate(row.result))
}

function interpret(method: string, url: string, result: unknown) {
  const kind = KIND[method] ?? inferKind(result)
  const rec = asRecord(result)

  if (kind === "void") {
    return { url, method, ok: true, data: rec ?? {} }
  }

  if (result == null) {
    return { url, method, ok: false, data: null, error: "empty result" }
  }

  if (kind === "bool") {
    const ok = rec?.result === true
    return { url, method, ok, data: rec, error: ok ? undefined : "result=false" }
  }

  if (kind === "flag") {
    const ok = rec?.api_result !== false
    const error = ok ? undefined : String(rec?.error_message ?? rec?.error_code ?? "api_result=false")
    return { url, method, ok, data: rec, error }
  }

  if (kind === "code") {
    const code = typeof rec?.result === "number" ? rec.result : undefined
    const okCodes = CODE_OK[method] ?? new Set([0])
    const ok = code != null && okCodes.has(code)
    return {
      url,
      method,
      ok,
      data: rec,
      error: ok ? undefined : String(rec?.desc ?? rec?.error_message ?? `code ${code}`),
    }
  }

  if (rec?.success === false) {
    return { url, method, ok: false, data: rec, error: String(rec.desc ?? rec.error ?? "success=false") }
  }
  return { url, method, ok: true, data: rec ?? result }
}


function inferKind(result: unknown): ReturnKind {
  const rec = asRecord(result)
  if (result == null) return "void"
  if (typeof rec?.api_result === "boolean") return "flag"
  if (typeof rec?.result === "boolean") return "bool"
  if (typeof rec?.result === "number") return "code"
  return "payload"
}

function asRecord(value: unknown): Record<string, unknown> | null {
  if (value && typeof value === "object" && !Array.isArray(value)) {
    return value as Record<string, unknown>
  }
  return null
}

function hydrate(value: unknown): unknown {
  if (typeof value === "string") {
    const text = value.trim()
    if (
      (text.startsWith("{") && text.endsWith("}")) ||
      (text.startsWith("[") && text.endsWith("]"))
    ) {
      try {
        return JSON.parse(text)
      } catch {
        return value
      }
    }
    return value
  }
  const rec = asRecord(value)
  if (!rec) return value
  return Object.fromEntries(Object.entries(rec).map(([key, nested]) => [key, hydrate(nested)]))
}
