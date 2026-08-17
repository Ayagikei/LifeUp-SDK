export type LifeUpParamValue = string | number | boolean | Array<string | number>

export function buildLifeUpUrl(
  method: string,
  params: Record<string, LifeUpParamValue | undefined> = {},
): string {
  const path = method.replace(/^\/+/, "")
  const url = new URL(`lifeup://api/${path}`)
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined) continue
    if (Array.isArray(value)) {
      for (const item of value) {
        url.searchParams.append(key, String(item))
      }
    } else {
      url.searchParams.append(key, String(value))
    }
  }
  return url.toString()
}
