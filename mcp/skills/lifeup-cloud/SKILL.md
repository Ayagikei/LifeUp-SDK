---
name: lifeup-cloud
description: Use when connecting to LifeUp Cloud, listing or completing LifeUp tasks, rewarding coins/exp/items, or calling LifeUp APIs over LAN.
---

# LifeUp Cloud

Talk to the LifeUp app through LifeUp Cloud on the LAN. Docs: MCP `help` (no second install).

Phone: LifeUp **1.106.0+** and LifeUp Cloud **3.0.1+**, running, with "Read LifeUp Data" granted. `status.update` warns if either is older.

## Connect

1. `discover` — mDNS `_lifeup._tcp`; auto-connects when exactly one Cloud is found. TXT `port` is HTTP.
2. `connect` `{ host }` only if 0 or >1 instances. Token only if Cloud set one — raw `Authorization`, not Bearer.

Details: `help` `discovery`. Errors / encoding / JSON: `help` `basics`. Built-in icons: `help` `sample_icons`. Shop effects / purchase limits: `help` `item_structures`. QR scan (phone app, not MCP): `help` `qr_scanning`.

## Common ops

| Want | Tool |
|---|---|
| Lists | `list_data` (or `list_tasks` / `list_items` …). Compact. Prefer `categoryId`. |
| History / feelings / pomodoro | `list_data` resource=`history`\|`feelings`\|`pomodoro_records` (server-paged) |

| Coins / info | `get_coin` / `get_info` |
| Complete | `complete_task` — exactly one of `id` / `gid` / `name` |
| Add | `add_task` |
| Reward | `reward` |
| Buy | `purchase_item` |
| Anything else | `call_api` (`via=contentprovider` default) |
| Several APIs | `call_api_batch` |
| Need UI (`goto`, dialogs) | `call_api` `via=launch` |

Repeating tasks: `id` changes each cycle, `gid` does not. Prefer `gid` or `name`. Do not dump all tasks if a category exists.

After writes, read `calls[].ok` and `calls[].data` (full return: ids, codes, query values, tomatoes, …).


## Long tail

**Actions (URL Scheme / write APIs):**

1. `help` `api-index` — method names + one-line purpose. Do not open every method file.
2. `help` `<method>` — full wiki param table, only for the method you will call.
3. `call_api` / `call_api_batch` with that method.

**Queries (HTTP JSON read models):**

1. `help` `query-index` — catalog of generated query docs from [LifeUp Wiki `docs/en/guide/api-reference.md`](https://github.com/Ayagikei/LifeUp-Wiki/blob/master/docs/en/guide/api-reference.md). From the LifeUp-SDK root, regenerate with `python3 mcp/scripts/extract-wiki-queries.py`.
2. `help` `query/<anchor>` — full field tables and examples for that section, e.g. `query/task-history-models` (Task fields including `importance` / `difficulty`), `query/task-queries`, `query/response-errors`, `query/pagination`.
3. `help` `query` — compact MCP `list_data` usage on this page; not a substitute for the generated HTTP reference.

Query docs describe **Cloud HTTP JSON** (`{code, message, data}`), not raw Android Cursor columns or the MCP list envelope. Use `detail=true` on list tools when you need the full HTTP task object; compact rows omit fields such as `importance` and `difficulty`.


Destructive: `delete_task`, `edit_coin`, `export_backup`, `history_operation` need `confirm: true`.
Wiki may lag; Cloud HTTP is source of truth. Phone broadcasts: `help` `broadcasts`. `list_events` always; `subscribe_events` uses Cloud WS (on by default).
