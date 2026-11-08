# MCP differences

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=mcp-differences

Help topic: `query/mcp-differences`

## MCP differences :id=mcp-differences

MCP list tools default to `limit=20`, clamp it to `1..100`, and clamp `offset` to at least `0`. A requested limit outside `1..100` still returns the clamped page and adds `limitCapped: true` plus `requestedLimit` on the MCP envelope. They send pagination to server-paged resources such as history/journals, but fetch full HTTP arrays and slice locally for resources such as tasks. Their `items`, `count`, `hasMore` and optional `total` belong to the MCP envelope. `detail=false` also removes fields from each record; use `detail=true` when the full HTTP model is needed. For tasks, optional `smartListType` (`daily`, `weekly`, `monthly`) calls `GET /smart_lists/{type}/tasks` and is mutually exclusive with `categoryId`. Compact task reads (`detail` not true) add `withSubTasks=false` on `/tasks`, `/tasks/{id}`, and `/smart_lists/{type}/tasks` so Cloud can skip subtask fetches; `detail=true` omits that parameter. Full subtask text is `todo`, not `name`. MCP sends `X-LifeUp-Concurrency: limited` on Cloud HTTP requests. HTTP 503 with body code 10003 means the request was not started; back off and retry later instead of hammering.
