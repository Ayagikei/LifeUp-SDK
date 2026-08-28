# task_template

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** task_template

**Description:** CRUD for task templates.

**Examples:**

- List templates: `lifeup://api/task_template?method=list`
- Create from parameters: `lifeup://api/task_template?method=create&name=MyTemplate&todo=Write diary&frequency=0`
- Create from an existing task: `lifeup://api/task_template?method=create&name=MyTemplate&from_task_id=1`
- Get template: `lifeup://api/task_template?method=get&id=1`
- Update template name: `lifeup://api/task_template?method=update&id=1&name=NewName`
- Update template content from a task: `lifeup://api/task_template?method=update&id=1&from_task_id=1`
- Delete template: `lifeup://api/task_template?method=delete&id=1`

| Parameter | Meaning | Value | Example | Required | Notes |
| --------- | ------- | ----- | ------- | -------- | ----- |
| method | Operation | list / get / create / update / delete | list | Yes | - |
| id | Template id | number > 0 | 1 | No* | Required for get/update/delete; alias: template_id |
| template_id | Template id | number > 0 | 1 | No* | Alias of id |
| name | Template name | text | MyTemplate | No* | Required for create; required for update if not using from_task_id/from_task_gid |
| from_task_id | Build from task id | number > 0 | 1 | No | For create/update |
| from_task_gid | Build from task group id | number > 0 | 1 | No | For create/update |
| todo | Task content | text | Write diary | No* | Required for create when not using from_task_id/from_task_gid |
| notes | Notes | text | Notes | No | Default is empty |
| category | List ID | number >= 0 | 0 | No | Alias: category_id |
| category_id | List ID | number >= 0 | 0 | No | Alias of category |
| frequency | Repeat frequency | integer | 0 | No | Same as add_task |
| weekdays | Weekdays | `1,3,5` | 1,3,5 | No | v1.106.0+; same as add_task; create rejects `none` |
| importance | Importance level | [1, 4] | 1 | No | - |
| difficulty | Difficulty level | [1, 4] | 1 | No | - |
| coin | Coin reward | number | 10 | No | - |
| coin_var | Coin reward variance | number | 1 | No | - |
| exp | Experience reward | number | 100 | No | - |
| skills | Skill IDs | array params | 1 | No | Supports arrays (e.g., &skills=1&skills=2) |
| skill_ids | Skill IDs | JSON array or comma list | [1,2] | No | Alternative to skills |
| deadline | Due time | timestamp (milliseconds) | 1640995200000 | No | - |
| start_time | Start time | timestamp (milliseconds) | 1640995200000 | No | - |
| remind_time | Reminder time | timestamp (milliseconds) | 1640995200000 | No | - |
| words | Completion reward text | text | Great job! | No | - |
| task_type | Task type | [0, 4] | 0 | No | 0 - Normal<br/>1 - Count<br/>2 - Negative<br/>3 - API<br/>4 - Timed |
| target_times | Target times | number > 0 | 10 | No | Only valid when task_type is 1 (count task) |
| is_affect_shop_reward | Affect shop reward | true / false | false | No | Only valid when task_type is 1 (count task) |
| enable_proportional_settlement | Enable proportional settlement | true / false | false | No | v1.104.0+; only valid when task_type is 1 (count task). Tasks created from the template keep this count-task settlement setting |
| expected_focus_minutes | Expected focus minutes | number > 0 | 25 | No | Only valid when task_type is 4 (timed task) |
| repeat_end_mode | Repeat end mode | 0 or 1 | 0 | No | Only valid for repeating tasks (frequency is not 0 / -1)<br/>0 - End by count<br/>1 - End by date |
| repeat_target_times | Repeat end count | number > 0 | 3 | No | Used when repeat_end_mode=0 (or inferred by presence of this field) |
| repeat_end_date | Repeat end date | timestamp (milliseconds) | 1640995200000 | No | Used when repeat_end_mode=1 (or inferred by presence of this field) |
| repeat_end_behavior | Repeat end behavior | 0 or 1 | 0 | No | 0 - Terminate<br/>1 - Freeze |

**Return:**

| Field | Meaning | Type | Notes |
| ----- | ------- | ---- | ----- |
| templates | templates list (JSON string) | text | Only for method=list |
| count | templates count | number | Only for method=list |
| template | template detail (JSON string) | text | Only for method=get |
| id | template id | number | For get/create/update/delete |
| name | template name | text | For get/create/update |
| success | whether success | true / false | For create/update/delete |

<br/>
