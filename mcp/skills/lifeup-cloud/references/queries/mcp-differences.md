# MCP differences

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=mcp-differences

Help topic: `query/mcp-differences`

## MCP differences :id=mcp-differences

MCP list tools default to `limit=20`, clamp it to `1..100`, and clamp `offset` to at least `0`. They send pagination to server-paged resources such as history/journals, but fetch full HTTP arrays and slice locally for resources such as tasks. Their `items`, `count`, `hasMore` and optional `total` belong to the MCP envelope. `detail=false` also removes fields from each record; use `detail=true` when the full HTTP model is needed.
