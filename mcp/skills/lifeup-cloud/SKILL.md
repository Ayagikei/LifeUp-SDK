---
name: lifeup-cloud
description: Use when connecting to LifeUp Cloud, listing or completing LifeUp tasks, rewarding coins/exp/items, or calling LifeUp APIs over LAN.
---

# LifeUp Cloud

Talk to the LifeUp app through LifeUp Cloud on the LAN. This package already exposes these docs as MCP `lifeup_help` and `lifeup://skill/*` resources — do not install a second copy.

Phone: LifeUp running + LifeUp Cloud started + "Read LifeUp Data" granted.

## Connect

1. `lifeup_status`
2. `lifeup_discover` (mDNS `_lifeup._tcp`, name contains `lifeup_cloud`, TXT `port`)
3. `lifeup_connect` with `host` if several/none. Token only if Cloud set one — raw `Authorization`, not Bearer.

Details: `lifeup_help` `discovery`. Errors / encoding / JSON: `lifeup_help` `basics`.

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

1. `lifeup_help` `api-index` — method names + one-line purpose. Do not open every method file.
2. `lifeup_help` `<method>` — full wiki param table, only for the method you will call.
3. `call_api` / `call_api_batch` with that method.


Destructive: `delete_task`, `edit_coin`, `export_backup`, `history_operation` need `confirm: true`.
Wiki may lag; Cloud HTTP is source of truth.

