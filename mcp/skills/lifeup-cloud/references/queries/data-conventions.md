# Shared data conventions

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=data-conventions

Help topic: `query/data-conventions`

## Shared data conventions :id=data-conventions

| Convention | Rule |
| --- | --- |
| Time | Timestamp fields use Unix milliseconds. Calendar-day boundaries depend on the intended time zone. Individual fields still have resource-specific meanings. |
| Identity | Task `id` identifies one task/cycle; task `gid` identifies its group across repeating cycles. A category/list ID identifies a separate resource. |
| Empty values | Missing, `null`, `0`, `""` and `[]` are distinct. Each model documents its actual fallback or omission behavior. |
| Numeric IDs | IDs can be signed 64-bit integers; a JavaScript Number cannot exactly represent every value. Preserve large IDs with a suitable JSON parser. |
| Arrays and objects | HTTP nested values such as `items` and `subTasks` are arrays, not JSON-encoded strings. |
| Status and reason codes | Use the enum documented for that resource. Task status, achievement status and journal reason codes are different domains. |

See the [task timestamp definitions](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=task-times "help topic query/task-history-models") for `startTime`, `deadline` and `endTime`.
