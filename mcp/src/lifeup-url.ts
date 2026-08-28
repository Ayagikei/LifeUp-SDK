export type LifeUpParamValue = string | number | boolean | Array<string | number>

export function buildLifeUpUrl(
  method: string,
  params: Record<string, LifeUpParamValue | undefined> = {},
): string {
  const path = method.replace(/^\/+/, "")
  const pairs: string[] = []
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined) continue
    const items = Array.isArray(value) ? value : [value]
    for (const item of items) {
      pairs.push(`${encodeURIComponent(key)}=${encodeURIComponent(String(item))}`)
    }
  }
  return pairs.length ? `lifeup://api/${path}?${pairs.join("&")}` : `lifeup://api/${path}`
}
