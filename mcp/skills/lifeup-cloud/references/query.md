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
### `achievements` status

`achievements` 行的 `status` 是**三态**，除了表示是否完成，还区分解锁后是否已领取奖励：

| status | 含义 |
|---|---|
| 0 | 未完成 |
| 1 | 已完成，**未领取奖励** |
| 2 | 已完成，已领取奖励 |

多端同步 / 自动化补领奖励时，用 `status == 1` 判断哪些成就可以领取（`detail=true` 时同一条记录还带 `exp` / `coin` / `items` 奖励内容）。

## List field values

From LifeUp ContentProvider (not wiki). Compact rows keep these.

| resource | field | values |
|---|---|---|
| `tasks` / `history` | `status` | `0` unfinished · `1` done · `2` overdue · `3` given up |
| `tasks` | `frequency` | `0` once · `1` daily · `N>1` every N days · `-1` unlimited · `-3` Ebbinghaus · `-4` monthly · `-5` yearly |
| `tasks` | `repeatEndCondition.mode` | `COUNT` / `DATE` (writes use 0/1) |
| `tasks` | `repeatEndCondition.behavior` | `TERMINATE` / `FREEZE` (writes use 0/1) |
| `task_categories` | `status` | `0` normal · `1` archived |
| `task_categories` | `type` | `<10` normal list · `10` daily · `11` weekly · `12` monthly · `20` doing |
| `achievements` | `status` | `0` locked · `1` unlocked, reward unclaimed · `2` unlocked, claimed |
| `achievements` | `type` | `0` normal · `1` subcategory |
| `achievement_categories` | `type` | `0` user · `1` system |
| `feelings` | `isFav` | bool (`true` when CP `1`) |
| `feelings` | `type` | `0` task · `1` achievement · `2` raw · `3` item use |
| `skills` | `type` | `0` user · `1` strength · `2` learning · `3` charm · `4` endurance · `5` vitality · `6` creative |
| `skills` | `status` | on `query`/`query_skill` only: `0` normal · `1` hidden. **Not** on `GET /skills`. |
| `items` | `disablePurchase` | bool |
| `pomodoro_records` | `reward` | `0` abandoned · `0.5*n` half · `n` full. No status/mode on the list. |

Hidden shop/synthesis categories exist in the app but **are not** on Cloud category lists. Hidden task lists are `task_categories.status=1` if Cloud returns them.

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
