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
| `skill_groups` | `GET /skill_groups` | `includeHidden` | client |
| `achievement_conditions` | `GET /achievement_conditions/{id}` | `categoryId` = achievement id | client |
| `coin_records` | `GET /coin_records` | `timeRangeStart`/`End` | **server** |
| `inventory_records` | `GET /inventory_records` | `timeRangeStart`/`End` | **server** |
| `exp_records` | `GET /exp_records` | `timeRangeStart`/`End` | **server** |
| `step_records` | `GET /step_records` | `timeRangeStart`/`End` | **server** |
| `level_defines` | `GET /level_defines` | | singleton `{ custom, levels }` |
| `statistics` | `GET /statistics` | `timeRangeStart`/`End` | singleton aggregates |
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
| `tasks` | `weekdays` | `1,3,5` (Mon=1 … Sun=7). Empty when not weekday mode. v1.106.0+ |
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
| `coin_records` / `inventory_records` | `resCode` | ShopServiceImpl: `0` buy · `1` use · `2` finish task · `3` undo finish · `4` clear · `5` give up · `6` overdue · `7` unlock achievement · `8` revoke give up · `9` revoke overdue · `10` return · `11` finish subtask · `12` undo subtask · `13` unlock user achievement · `14` undo user achievement · `15` deposit · `16` withdraw · `17` sell tomatoes · `20` reward item · `21` undo reward item · `23` synthesis · `24` loot box · `25` ATM interest · `26` tomato exchange · `27` credit interest · `28` API · `29` effect stock |
| `exp_records` | `resCode` | AttributeServiceImpl (not shop 28): `0` unknown · `1` finish · `2` achievement · `3` like exchange · `4` day streak · `5` steps · `6` set finished · `7` revoke give up · `8` revoke overdue · `9` eat tomato · `10` used item · `11` unlock user achievement · `12` finish subtask · `200` API · `101` undo finish · `102` give up · `103` overdue · `104` used item debit · `105` lock user achievement · `106` undo subtask |

Hidden shop/synthesis/skill-group categories: pass `includeHidden=true`. Hidden task lists are `task_categories.status=1`. GET `/info` includes LifeUp `appVersion`/`appVersionName`/`apiVersion` and Cloud `cloudVersion`/`cloudVersionName`.

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
