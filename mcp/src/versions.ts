export const MIN_LIFEUP = "1.106.0"
export const MIN_CLOUD = "3.0.0"

export function cmpVersion(a: string, b: string): number {
  const left = a.split(".").map((part) => Number.parseInt(part, 10) || 0)
  const right = b.split(".").map((part) => Number.parseInt(part, 10) || 0)
  const n = Math.max(left.length, right.length)
  for (let i = 0; i < n; i++) {
    const d = (left[i] ?? 0) - (right[i] ?? 0)
    if (d !== 0) return d
  }
  return 0
}

export function versionAdvice(info?: { appVersionName?: string; cloudVersionName?: string }): string[] {
  const notes: string[] = []
  const lifeup = info?.appVersionName
  if (!lifeup || cmpVersion(lifeup, MIN_LIFEUP) < 0) {
    notes.push(
      `LifeUp ${lifeup ?? "unknown"} < ${MIN_LIFEUP}. Update LifeUp — journals, stats, level curve, and related APIs need 1.106.0+.`,
    )
  }
  const cloud = info?.cloudVersionName
  if (!cloud || cmpVersion(cloud, MIN_CLOUD) < 0) {
    notes.push(
      `LifeUp Cloud ${cloud ?? "unknown"} < ${MIN_CLOUD}. Update Cloud — GET routes and version fields need 3.0.0+.`,
    )
  }
  return notes
}
