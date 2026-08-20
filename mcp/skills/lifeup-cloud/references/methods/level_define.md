# level_define

**Method name:** level_define

**Description:** Read the current XP curve, toggle custom mode, or replace the custom curve.

**Example:**

- Query: `lifeup://api/level_define?query=true`
- Enable custom mode: `lifeup://api/level_define?custom=true`
- Replace curve: `lifeup://api/level_define?levels=[{"levelStart":1,"levelEnd":10,"perLevelExp":300}]`

| Parameter | Meaning | Type | Required | Notes |
| --------- | ------- | ---- | -------- | ----- |
| query | Return current curve | boolean | no | Does not write |
| custom | Use custom curve | boolean | no | false = built-in table |
| levels | Replace custom rows | JSON array of `{levelStart,levelEnd,perLevelExp}` | no | Replaces the whole custom table; turns custom on. MCP needs `confirm=true` |

**Return value:** `{ custom, levels }`
