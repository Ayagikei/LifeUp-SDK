# Gaps (live MCP + Cloud)

Recorded while exercising the MCP. Not a promise to implement.

## Missing or incomplete Cloud / MCP

| Wanted | Tried | Result |
|---|---|---|
| Hidden shop / synthesis / achievement categories | `list_data` category lists | Cloud category CP omits hide status. App models have HIDE=1. No restore/list-hidden HTTP. |
| Skill hidden flag on list | `list_skills` | `GET /skills` has no `status`. Use `call_api` `query_skill`. |
| Shop settings | help / list_data | No GET. Known Cloud gap. |
| Loot box inventory query | help | `loot_box/v2` is mutation; no list GET. |
| Feelings delete | `call_api` | Check `help` `feelings` — delete may exist as scheme; no list-filter-by-id first-class tool. |
| Coin ledger / resCode history | `get_coin` | Only `{ value }`. No CP coin ledger list. |
| Count complete on `complete_task` | first-class tool | **Fixed**: `count` / `count_set_type` now on the tool. |

## Agent friction (fixed or noted)

| Issue | Status |
|---|---|
| `lifeup_lifeup_*` double prefix | Fixed: tools are `status`/`discover`/`connect`/`help` |
| 7 extra `read_skill_*` tools | Fixed: resources no longer registered as tools (19 tools) |
| `connect` name=`manual` after mDNS | Fixed: keeps `lifeup_cloud` when host matches discover |
| Docs split help vs resources | Fixed: `help` with no topic = workflow |
| mDNS empty then success | Discover timeout 2s→5s; one retry still required |
| `complete_task` cannot +1 a count task | Fixed |

## Not missing (use `list_data`)

`feelings`, `achievements`, `synthesis`, `pomodoro_records`, `coin`, `info` — no extra `list_*` shortcuts. `help` / `list_data` resource=…
