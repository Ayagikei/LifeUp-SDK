# Interface index

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=interface-index

Help topic: `query/interface-index`

## Interface index :id=interface-index

| Resource | Methods and paths | Parameters and model |
| --- | --- | --- |
| Common rules | JSON responses, authorization, cache conditions, pagination | [Conventions](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=representations "help topic query/representations"), [cache and auth](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=auth-cache "help topic query/auth-cache"), [pagination](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=pagination "help topic query/pagination") |
| Tasks | `GET /tasks`, `GET /tasks/{categoryId}`, `GET /tasks_categories` | [Queries](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-tasks "help topic query/task-queries"), [task model](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=task-fields "help topic query/task-history-models"), [categories](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-tasks-categories "help topic query/get-items") |
| Task history | `GET /history` | [Parameters](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-history "help topic query/task-queries"), [timestamps](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=task-times "help topic query/task-history-models"), [daily reports](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=daily-report "help topic query/task-queries") |
| Items | `GET /items`, `GET /items/{listId}`, `GET /items_categories` | [Item IDs, category IDs and full models](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-items "help topic query/get-items") |
| Skills | `GET /skills`, `GET /skill_groups` | [Skills and groups](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-skills "help topic query/get-skills") |
| Achievements | `GET /achievements`, `GET /achievements/{id}`, `GET /achievement_categories`, `GET /achievement_conditions/{id}` | [Achievements and conditions](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-achievements "help topic query/get-achievements") |
| Synthesis | `GET /synthesis`, `GET /synthesis/{id}`, `GET /synthesis_categories`, `GET /synthesis_categories/{id}` | [Recipes and categories](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-synthesis "help topic query/get-synthesis") |
| Feelings | `GET /feelings` | [Paged feelings](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-feelings "help topic query/get-feelings") |
| EXP / coins | `GET /exp_records`, `GET /coin_records` | [Journal parameters and models](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=reward-journals "help topic query/reward-journals") |
| Other records | `GET /pomodoro_records`, `GET /inventory_records`, `GET /step_records` | [Pomodoro](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-pomodoro-records "help topic query/get-pomodoro-records"), [inventory](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-inventory-records "help topic query/get-inventory-records"), [steps](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-step-records "help topic query/get-step-records") |
| Levels / statistics | `GET /level_defines`, `GET /statistics` | [Levels](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-level-defines "help topic query/get-level-defines"), [statistics](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-statistics "help topic query/get-statistics") |
| App / balance | `GET /info`, `GET /coin` | [Version information](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-info "help topic query/get-info"), [coin result](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-coin "help topic query/get-info") |
| Invoke actions | `GET` / `POST /api`, `GET` / `POST /api/contentprovider` | [Invocation bridge](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=invoke-api "help topic query/invoke-api"); business methods remain in the [URL Scheme catalog](https://docs.lifeupapp.fun/en/#/guide/api) |
| Files / backups | `GET /files/{url}`, `POST /files/upload`, `GET /data/export`, `POST /data/import` | [Files](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=files-download "help topic query/files-download"), [backup transfer](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=files-backup "help topic query/files-backup") |
| Events | `GET /events`, WebSocket `/events` | [Polling and live events](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-events "help topic query/get-events") |
| Landing page | `GET /` | HTML introduction, not a JSON resource. |

MCP uses a separate client/tool representation; see [MCP differences](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=mcp-differences "help topic query/mcp-differences") and the [MCP guide](https://docs.lifeupapp.fun/en/#/guide/api_mcp).
