# Tasks

Wiki: add_task / complete / give_up / freeze / unfreeze / delete_task / edit_task. Extra fields: `lifeup_help` topic `api-index`.

## complete_task

Exactly one selector:

- `id` — this cycle (changes on repeat)
- `gid` — group id (stable)
- `name` — fuzzy, one match
- `ui` default `false`

Count tasks via `call_api` `complete`: `count`, `count_set_type` (`absolute`|`relative`, default relative), `count_force_sum_up`, `reward_factor`. Only unfinished tasks.

## add_task

First-slice: `todo` (required), `notes`, `coin`, `exp`, `skills[]`, `category`, `frequency`, `item_id`|`item_name`.

`frequency`: `0` once, `1` daily, `N>1` every N days, `-1` unlimited, `-3` Ebbinghaus, `-4` monthly, `-5` yearly.

Returns `task_id` / `task_gid` in `calls[].data`. Outer HTTP 200 is not enough.


Also on wiki / `call_api`: `coin_var`, `importance`, `difficulty`, `deadline`, `no_deadline`, `color`, `background_*`, `start_time`, `auto_use_item`, `remind_time`, `pin`, `words`, `frozen`, `freeze_until`, `*_penalty_factor`, `write_feelings`, `item_amount`, `items`, `task_type`, `target_times`, `is_affect_shop_reward`, `enable_proportional_settlement`, `expected_focus_minutes`, `repeat_end_*`.

## Other

`give_up` / `freeze` (`time`) / `unfreeze` / `delete_task` (confirm) / `edit_task` / `task_template` / `subtask` / `subtask_operation`: same id|gid|name selectors. See api-index.
