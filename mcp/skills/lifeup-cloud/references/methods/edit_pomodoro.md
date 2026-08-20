# edit_pomodoro

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** edit_pomodoro

**Description:** Edit an existing Pomodoro timing record or add a new record if a valid `edit_item_id` is provided.

**Example:**

- Edit a record with a specified ID, set duration to 45 minutes (2700000 ms), and reward tomatoes: [lifeup://api/edit_pomodoro?edit_item_id=123&duration=2700000&reward_tomatoes=true](lifeup://api/edit_pomodoro?edit_item_id=123&duration=2700000&reward_tomatoes=true)
- Edit a record by start and end time: [lifeup://api/edit_pomodoro?start_time=1659322800000&end_time=1659326400000&edit_item_id=456](lifeup://api/edit_pomodoro?start_time=1659322800000&end_time=1659326400000&edit_item_id=456)

**Parameters:**

| Parameter       | Meaning                    | Type                  | Example       | Required | Notes                                            |
| --------------- | -------------------------- | --------------------- | ------------- | -------- | ------------------------------------------------ |
| task_id         | Task ID                    | Number greater than 0 | 101           | No       | Unique identifier for the task                   |
| task_gid        | Task group ID              | Number greater than 0 | 5             | No       | If provided, it overrides task_id                |
| task_name       | Task name                  | Any text              | Study         | No       | Must be provided if task_id or task_gid is not   |
| start_time      | Timing start time          | Timestamp             | 1659322800000 | No*      | Can Google to understand what a timestamp is     |
| end_time        | Timing end time            | Timestamp             | 1659326400000 | No*      | -                                                |
| duration        | Focus duration             | Number (milliseconds) | 2700000       | No*      | Must be at least 30000 milliseconds (30 seconds) |
| reward_tomatoes | Whether to reward tomatoes | true or false         | true          | No       | Default is false                                 |
| edit_item_id    | ID of the item to edit     | Number greater than 0 | 123           | Yes      | Specifies the record ID to edit                  |
| ui              | Display reward tomatoes UI | true or false         | true          | No       |                                                  |
| delete          | Delete the record          | true or false         | true          | No       | v1.105.1+. Soft-deletes the pomodoro record (`isDel`), same as the app |

**Return values:**

| Parameter | Meaning                          | Type   | Example | Required | Notes                    |
| --------- | -------------------------------- | ------ | ------- | -------- | ------------------------ |
| tomatoes  | Tomatoes gained from this action | Number | 2       | No       | Returned if `ui` is true |

**Notes:**

1. At least one of `start_time`, `duration`, `end_time` must be provided.
2. `end_time` needs to be greater than `start_time`.
3. `duration` should be less than or equal to (`end_time` - `start_time`).
4. If `edit_item_id` is provided and the corresponding record is found, it will be edited; otherwise, a new record will be created based on other parameters.

<br/>
