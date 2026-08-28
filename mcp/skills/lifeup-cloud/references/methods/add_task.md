# add_task

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** add_task

**Description:** Create a task directly

**Example:**
[lifeup://api/add_task?todo=This is an auto-added task&notes=notes&coin=10&coin_var=1&exp=2048&skills=1&skills=2&skills=3&category=0&item_name=coin](lifeup://api/add_task?todo=This is an auto-added task&notes=notes&coin=10&coin_var=1&exp=2048&skills=1&skills=2&skills=3&category=0&item_name=coin)

| Parameter         | Meaning             | Values                | Example   | Required | Notes                           |
| ---------------- | ------------------- | -------------------- | --------- | -------- | ------------------------------- |
| todo             | Task content        | any text             | Write diary | Yes     |                                |
| notes            | Notes               | any text             | Notes      | No       | Defaults to empty               |
| coin             | Coin reward         | number >= 0         | 10         | No       | Defaults to 0, subject to system limits                   |
| coin_var         | Coin reward variance| number >= 0          | 1          | No       | Defaults to 0; if >0, random reward between [coin, coin+coin_var] |
| exp              | Experience reward   | number >= 0          | 100        | No       | Defaults to 0, subject to system limits                   |
| skills           | Skill IDs           | array of numbers > 0 | 1          | No       | Supports arrays (e.g., &skills=1&skills=2) |
| category         | List ID             | number >= 0          | 0          | No       | Defaults to 0 (default list); smart lists not allowed |
| frequency        | Repeat frequency    | integer              | 0          | No       | Defaults to 0 (once)<br/>0 - Once<br/>1 - Daily<br/>N (N>1) - Every N days<br/>-1 - Unlimited<br/>-3 - Ebbinghaus (requires v1.99.1)<br/>-4 - Monthly<br/>-5 - Yearly |
| weekdays         | Weekdays            | `1,3,5` or `none`    | 1,3,5      | No       | v1.106.0+; 1=Monday … 7=Sunday, days **to repeat**. If present, frequency must be omitted or 1. `none` is rejected on add; on edit, `none` clears back to daily. All 7 days is treated as daily |
| importance       | Importance level    | [1, 4]              | 1          | No       | Defaults to 1                   |
| difficulty       | Difficulty level    | [1, 4]              | 1          | No       | Defaults to 1                   |
| deadline         | Due time            | timestamp (milliseconds) | 1640995200000 | No |                               |
| no_deadline      | No deadline         | true/false         | true      | No       | v1.104.0+; only valid for repeating tasks. Passing `&no_deadline=true` clears the specific due time |
| color            | Tag color           | color string         | #66CCFF    | No       | # must be escaped as %23        |
| background_url   | Background image URL| web URL             | http://example.com/bg.jpg | No | Must be accessible web image |
| background_alpha | Background opacity  | float between [0, 1] | 0.5        | No       | Defaults to 1.0                |
| enable_outline   | Enable text outline | true or false       | false      | No       | Only valid with background_url, adds outline to text for better readability |
| use_light_remark_text_color | Use light text for notes | true or false | false | No | Only valid with background_url, uses light color for notes text |
| start_time       | Start time          | timestamp (milliseconds) | 1640995200000 | No | Task start time              |
| auto_use_item    | Auto use reward items| true or false      | false      | No       | Automatically use rewards on completion |
| remind_time      | Reminder time       | timestamp (milliseconds) | 1640995200000 | No | Task reminder time          |
| pin              | Pin task            | true or false       | false      | No       | Pin task to top                |
| words            | Completion reward text | any text         | Great job! | No       | Motivational text shown when task is completed |
| frozen           | Freeze status       | true or false       | false      | No       | Defaults to false              |
| freeze_until     | Freeze until        | timestamp (milliseconds) | 1640995200000 | No | Only effective when frozen is true |
| coin_penalty_factor | Coin penalty factor| float between [0, 100) | 0.5    | No       |                               |
| exp_penalty_factor | Experience penalty factor| float between [0, 100) | 0.5 | No    |                               |
| write_feelings   | Enable feelings     | true or false       | false      | No       | Defaults to false              |
| item_id          | Item ID             | number > 0          | 1          | No*      | Either item_id or item_name required |
| item_name        | Item name           | any text            | Treasure   | No*      | Either item_id or item_name required |
| item_amount      | Item quantity       | [1, 99]             | 1          | No       | Defaults to 1                  |
| items            | Item rewards        | JSON text           | `help` `item_structures` § Item Reward | No | Set multiple item rewards |
| task_type        | Task type           | [0, 4]              | 0          | No       | Requires v1.99.1<br/>0 - Normal task<br/>1 - Count task<br/>2 - Negative task<br/>3 - API task<br/>4 - Timed task (v1.102.0+) |
| target_times     | Target times        | number > 0          | 1          | No       | Only valid when task_type is 1 (count task) |
| is_affect_shop_reward | Affect shop reward | true/false      | false    | No       | Only valid when task_type is 1 (count task), whether to affect the reward calculation of items |
| enable_proportional_settlement | Enable proportional settlement | true/false | false | No | v1.104.0+; only valid when task_type is 1 (count task). When enabled, count progress settlement grants rewards proportionally, and final completion will not grant already-settled rewards again |
| expected_focus_minutes | Expected focus minutes | number > 0 | 25 | No | Only valid when task_type is 4 (timed task); defaults to 25 (v1.102.0+) |
| repeat_end_mode | Repeat end mode | 0 or 1 | 0 | No | Only valid for repeating tasks (frequency is not 0 / -1)<br/>0 - End by count<br/>1 - End by date (v1.102.0+) |
| repeat_target_times | Repeat end count | number > 0 | 3 | No | Used when repeat_end_mode=0 (or inferred by presence of this field); do not confuse with target_times (v1.102.0+) |
| repeat_end_date | Repeat end date | timestamp (milliseconds) | 1640995200000 | No | Used when repeat_end_mode=1 (or inferred by presence of this field) (v1.102.0+) |
| repeat_end_behavior | Repeat end behavior | 0 or 1 | 0 | No | 0 - Terminate task after reaching end condition<br/>1 - Freeze task after reaching end condition (v1.102.0+) |

**Response:**

| Field    | Type    | Description      | Example | Notes                    |
| -------- | ------- | ---------------- | ------- | ------------------------ |
| task_id  | Number  | Task ID          | 1000    |                          |
| task_gid | Number  | Task group ID    | 1000    |                          |
