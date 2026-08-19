# edit_task

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** edit_task

**Description:** Edit content and properties of an existing task

**Example:**
[lifeup://api/edit_task?id=1&todo=Modified task content&notes=notes&coin=10&exp=20&skills=1&skills=2&category=0](lifeup://api/edit_task?id=1&todo=Modified task content&notes=notes&coin=10&exp=20&skills=1&skills=2&category=0)

| Parameter           | Meaning              | Values                | Example   | Required | Notes                           |
| ------------------ | -------------------- | -------------------- | --------- | -------- | ------------------------------- |
| id                 | Task ID              | number greater than 0 | 1        | No*      | One of id, gid, or name required |
| gid                | Task group ID        | number greater than 0 | 1        | No*      | One of id, gid, or name required |
| name               | Task name            | any text             | Write diary| No*      | One of id, gid, or name required |
| todo               | Task content         | any text             | Write weekly| No      |                                |
| notes              | Notes                | any text             | Note content| No      |                                |
| coin               | Coin reward          | number >= 0         | 10        | No       | Coins earned upon completion, subject to system limits    |
| coin_var           | Coin variance        | number greater than 0 | 1        | No       | Random reward between [coin, coin+coin_var] |
| exp                | Experience reward    | number >= 0          | 20        | No       | Experience points earned, subject to system limits        |
| skills             | Skill IDs            | array of numbers greater than 0 | 1 | No    | Supports arrays (e.g., &skills=1&skills=2) |
| category           | List ID              | number greater than or equal to 0 | 0 | No  | 0 for default list, smart lists not supported |
| frequency          | Repeat frequency     | integer              | 0         | No       | Defaults to 0 (once)<br/>0 - Once<br/>1 - Daily<br/>N (N>1) - Every N days<br/>-1 - Unlimited<br/>-3 - Ebbinghaus (requires v1.99.1)<br/>-4 - Monthly<br/>-5 - Yearly |
| importance         | Importance level     | [1, 4]              | 1         | No       | Defaults to 1                   |
| difficulty         | Difficulty level     | [1, 4]              | 2         | No       | Defaults to 1                   |
| deadline           | Due date             | timestamp (milliseconds) | 1640995200000 | No |                               |
| no_deadline        | No deadline          | true/false         | true     | No       | v1.104.0+; only valid for repeating tasks. Passing `&no_deadline=true` clears the specific due time |
| remind_time        | Reminder time        | timestamp (milliseconds) | 1640995200000 | No |                               |
| start_time         | Start time           | timestamp (milliseconds) | 1640995200000 | No |                               |
| color              | Tag color            | color string         | #66CCFF   | No       | # must be escaped as %23        |
| background_url     | Background image URL | web URL address      | http://example.com/bg.jpg | No |                         |
| background_alpha   | Background opacity   | floating point between [0, 1] | 0.5 | No   |                                |
| enable_outline     | Enable text outline  | true or false       | false      | No       | Only valid with background_url, adds outline to text for better readability |
| use_light_remark_text_color | Use light text for notes | true or false | false | No | Only valid with background_url, uses light color for notes text |
| item_id            | Item ID              | number greater than 0 | 1        | No*      | One of item_id or item_name required |
| item_name          | Item name            | any text             | Treasure  | No*      | One of item_id or item_name required |
| item_amount        | Item amount          | [1, 99]             | 1         | No       | Defaults to 1                   |
| items              | Items reward JSON    | JSON text           | [{"itemId":1,"amount":1}] | No | Set multiple item rewards |
| auto_use_item      | Auto use item        | true or false        | false     | No       |                                |
| frozen             | Freeze status        | true or false        | false     | No       | Defaults to false              |
| freeze_until       | Freeze until         | timestamp (milliseconds) | 1640995200000 | No | Only effective when frozen is true |
| coin_penalty_factor| Coin penalty factor  | floating point between [0, 100) | 0.5 | No |                                |
| exp_penalty_factor | Experience penalty factor | floating point between [0, 100) | 0.5 | No |                             |
| write_feelings     | Enable feelings      | true or false        | false     | No       |                                |
| pin                | Pin task             | true or false        | false     | No       |                                |
| words              | Completion reward text | any text           | Great job!| No       | Motivational text shown when task is completed |
| task_type        | Task type           | [0, 4]              | 0          | No       | Requires v1.99.1<br/>0 - Normal task<br/>1 - Count task<br/>2 - Negative task<br/>3 - API task<br/>4 - Timed task (v1.102.0+) |
| target_times     | Target times        | number > 0          | 1          | No       | Only valid when task_type is 1 (count task) |
| is_affect_shop_reward | Affect shop reward | true/false      | false    | No       | Only valid when task_type is 1 (count task), whether to affect the reward calculation of items |
| enable_proportional_settlement | Enable proportional settlement | true/false | false | No | v1.104.0+; only valid when task_type is 1 (count task). For existing count tasks, this parameter can be passed alone to turn proportional settlement on or off; changing this setting or reward configuration may reset or roll back settled progress according to app rules |
| expected_focus_minutes | Expected focus minutes | number > 0 | 25 | No | Only valid when task_type is 4 (timed task); defaults to 25 (v1.102.0+) |
| repeat_target_times | Repeat end count | number > 0 | 3 | No | Only valid for repeating tasks (frequency is not 0 / -1); when both repeat_target_times and repeat_end_date are provided, repeat_target_times takes priority (v1.102.0+) |
| repeat_end_date | Repeat end date | timestamp (milliseconds) | 1640995200000 | No | Only valid for repeating tasks (frequency is not 0 / -1) (v1.102.0+) |
| repeat_end_behavior | Repeat end behavior | 0 or 1 | 0 | No | 0 - Terminate task after reaching end condition<br/>1 - Freeze task after reaching end condition (v1.102.0+) |
| coin_set_type     | How to set coin value | One of:<br/>absolute<br/>relative | absolute | No | absolute - directly set coin to value<br/>relative - add/subtract from original coin value |
| exp_set_type      | How to set exp value | One of:<br/>absolute<br/>relative | absolute | No | absolute - directly set exp to value<br/>relative - add/subtract from original exp value |

**Response:**

| Field     | Type    | Description      | Example | Notes             |
| --------- | ------- | ---------------- | ------- | ---------------- |
| task_id   | Number  | Task ID          | 1000    |                  |
| task_gid  | Number  | Task group ID    | 1000    |                  |

<br/>
