# subtask

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** subtask

**Description:** Create or edit subtasks

**Examples:**

- Add a subtask to main task ID 1: [lifeup://api/subtask?main_id=1&todo=Complete%20homework](lifeup://api/subtask?main_id=1&todo=Complete%20homework)
- Edit subtask and set rewards: [lifeup://api/subtask?main_id=1&edit_id=2&coin=10&exp=5](lifeup://api/subtask?main_id=1&edit_id=2&coin=10&exp=5)

| Parameter     | Meaning            | Values                | Example    | Required | Notes                           |
| ------------ | ------------------ | -------------------- | ---------- | -------- | ------------------------------- |
| main_id      | Main task ID       | number greater than 0 | 1         | No*      | One of main_id, main_gid, or main_name required |
| main_gid     | Main task group ID | number greater than 0 | 1         | No*      | One of main_id, main_gid, or main_name required |
| main_name    | Main task name     | any text             | Study task | No*      | One of main_id, main_gid, or main_name required |
| edit_id      | Subtask ID to edit | number greater than 0 | 2         | No*      | One of edit_id, edit_gid, or edit_name required when editing; not needed for creation |
| edit_gid     | Subtask group ID   | number greater than 0 | 2         | No*      | One of edit_id, edit_gid, or edit_name required when editing; not needed for creation |
| edit_name    | Subtask name       | any text             | Do homework| No*      | One of edit_id, edit_gid, or edit_name required when editing; not needed for creation |
| todo         | Task content       | any text             | Do homework| No       | Required when creating new subtask |
| remind_time  | Reminder time      | timestamp (milliseconds)| 1640995200000 | No | Pass null to clear reminder    |
| order        | Order              | integer              | 1          | No       | Position in task list           |
| coin         | Coin reward        | [0, 999999]         | 10         | No       | Coins earned upon completion    |
| coin_var     | Coin variance      | integer              | 5          | No       | Random variance in coin reward  |
| exp          | Experience reward  | [0, 99999]          | 5          | No       | Experience points earned        |
| auto_use_item| Auto use item      | true or false        | true       | No       | Whether to use item automatically on completion |
| item_id      | Item ID            | number greater than 0 | 1         | No*      | One of item_id or item_name required |
| item_name    | Item name          | any text             | Health Potion| No*    | One of item_id or item_name required |
| item_amount  | Item amount        | number greater than 0 | 1         | No       | Only valid when setting item reward |
| items        | Items JSON         | JSON text            | [{"item_id":1,"amount":1}] | No | `help` `item_structures` § Item Reward |
| coin_set_type     | How to set coin value | One of:<br/>absolute<br/>relative | absolute | No | absolute - directly set coin to value<br/>relative - add/subtract from original coin value |
| exp_set_type      | How to set exp value | One of:<br/>absolute<br/>relative | absolute | No | absolute - directly set exp to value<br/>relative - add/subtract from original exp value |

**Response:**

| Field        | Type    | Description      | Example | Notes            |
| ------------ | ------- | ---------------- | ------- | ---------------- |
| main_task_id | Number  | Main task ID     | 1       |                  |
| subtask_id   | Number  | Subtask ID       | 2       |                  |
| subtask_gid  | Number  | Subtask group ID | 3       | May be empty     |

<br/>
