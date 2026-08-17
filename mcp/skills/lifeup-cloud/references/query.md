# Query (GET / ContentProvider)

Two Cloud layers:

| Layer | HTTP | MCP |
|---|---|---|
| **Query** (this page) | `GET /tasks`, `/items`, `/history`, … | `list_data` / `list_*` |
| **Action** | `POST /api/contentprovider` or `POST /api` | `call_api` / `complete_task` / … |

Query returns large lists from LifeUp's ContentProvider. Actions are URL-scheme methods.

## Progressive disclosure

1. `list_task_categories` / `list_item_categories` / `list_data resource=achievement_categories` — pick an id.
2. `list_data` with that `categoryId`. Default **compact** rows, `limit=20`.
3. `detail=true` only for the few rows you will mutate.
4. Page with `offset` + `hasMore`.

Do not dump `/tasks` with `detail=true`.

## `list_data` resources

| resource | HTTP | Filter | Paging |
|---|---|---|---|
| `tasks` | `GET /tasks` or `/tasks/{categoryId}` | `categoryId` | client slice |
| `task_categories` | `GET /tasks_categories` | | client |
| `history` | `GET /history` | `gid` | **server** `offset`/`limit` |
| `items` | `GET /items`, `/items/{categoryId}`, `?id=` | `categoryId` or `ids` | client |
| `item_categories` | `GET /items_categories` | | client |
| `skills` | `GET /skills` | | client |
| `achievements` | `GET /achievements` or `/{categoryId}` | `categoryId` | client |
| `achievement_categories` | `GET /achievement_categories` | | client |
| `feelings` | `GET /feelings` | | **server** |
| `synthesis` | `GET /synthesis` or `/{categoryId}` | `categoryId` | client |
| `synthesis_categories` | `GET /synthesis_categories` | `categoryId` | client |
| `pomodoro_records` | `GET /pomodoro_records` | `timeRangeStart`/`End` | **server** |
| `coin` | `GET /coin` | | singleton `{ value }` |
| `info` | `GET /info` | | singleton |

Shortcuts: `list_tasks`, `list_history`, `list_items`, `list_skills`, `get_coin`, `get_info` — same compact envelope.

## Envelope

Cloud: `{ code, message, data }`.

| code | meaning |
|---|---|
| 200 | transport OK; `data` is the list or object |
| 10001 | LifeUp not running, or Read Data not granted |
| 10002 | ContentProvider query failed |
| 500 | other server error |
| HTTP 401 | token required or invalid |

MCP list tools then wrap `data` as:

```
{ ok, code: 200, source: "contentprovider", resource, offset, limit, total?, count, hasMore, items }
```

`total` only when Cloud returned the full list (not server-paged). Compact drops notes/icons/subTasks.

History query name is `gid` (not wiki `filterGid`). Files: `GET /files/{url}` — not a list tool.
