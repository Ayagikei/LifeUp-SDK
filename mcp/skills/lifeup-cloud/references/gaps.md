# Gaps (live MCP + Cloud)

Needs a new LifeUp + Cloud build for the latest CP routes.

## Closed

| Wanted | How |
|---|---|
| Complete / claim achievement | `call_api` `complete_achievement` |
| Feelings / pomodoro delete | `feeling` / `edit_pomodoro` + `delete=true` `confirm=true` |
| Shop settings / loot contents | `shop_settings query=true` / `loot_box/v2 query=true` |
| Unlock conditions / skill groups | `list_data` `achievement_conditions` / `skill_groups` |
| Coin / item / exp journals | `coin_records` / `inventory_records` / `exp_records` |
| Level curve | `list_data` `level_defines` · write `call_api` `level_define` |
| Global punishment factors | `call_api` `query` `key=punishment` · write `app_settings` |
| Statistics aggregates | `list_data` `statistics` |
| Step history | `list_data` `step_records` · write still `step` |
