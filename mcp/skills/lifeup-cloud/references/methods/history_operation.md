# history_operation

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** history_operation

**Description:** Operate on completed/abandoned/expired tasks

**Examples:**

- Delete history task: [lifeup://api/history_operation?id=1&operation=delete](lifeup://api/history_operation?id=1&operation=delete)
- Mark task as given up: [lifeup://api/history_operation?id=1&operation=set_to_give_up](lifeup://api/history_operation?id=1&operation=set_to_give_up)
- Restart task: [lifeup://api/history_operation?id=1&operation=restart](lifeup://api/history_operation?id=1&operation=restart)

!> This API is only applicable to non-uncompleted tasks (completed, given up, or expired)

| Parameter      | Meaning           | Values               | Example    | Required | Notes                           |
| ------------- | ----------------- | -------------------- | ---------- | -------- | ------------------------------- |
| id            | Task ID           | number greater than 0 | 1         | Yes      | ID of the history task          |
| operation     | Operation type    | One of:<br/>delete<br/>complete<br/>undo_complete<br/>set_to_give_up<br/>set_to_overdue<br/>edit_completed_time<br/>restart | delete | Yes | delete - Delete task<br/>complete - Mark as completed<br/>undo_complete - Undo completion<br/>set_to_give_up - Mark as given up<br/>set_to_overdue - Mark as expired<br/>edit_completed_time - Modify completion time<br/>restart - Restart task |
| completed_time | Completion time   | timestamp (milliseconds) | 1640995200000 | No* | Required only when operation is edit_completed_time |

**Response:**

| Field    | Type    | Description    | Example | Notes                    |
| -------- | ------- | -------------- | ------- | ------------------------ |
| task_id  | Number  | Task ID        | 1000    | ID of the operated task  |

<br/>
