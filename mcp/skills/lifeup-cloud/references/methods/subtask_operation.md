# subtask_operation

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** subtask_operation

**Description:** Complete, undo completion, or delete subtasks

**Examples:**

- Complete a subtask: [lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=complete](lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=complete)
- Delete a subtask: [lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=delete](lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=delete)
- Undo subtask completion: [lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=undo_complete](lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=undo_complete)

| Parameter     | Meaning          | Values               | Example    | Required | Notes                          |
| ------------ | ---------------- | ------------------- | ---------- | -------- | ------------------------------ |
| main_id      | Main task ID     | number greater than 0| 1         | No*      | One of main_id, main_gid, or main_name required |
| main_gid     | Main task group ID| number greater than 0| 1        | No*      | One of main_id, main_gid, or main_name required |
| main_name    | Main task name   | any text            | Study task | No*      | One of main_id, main_gid, or main_name required |
| edit_id      | Subtask ID       | number greater than 0| 2         | No*      | One of edit_id, edit_gid, or edit_name required |
| edit_gid     | Subtask group ID | number greater than 0| 2         | No*      | One of edit_id, edit_gid, or edit_name required |
| edit_name    | Subtask name     | any text            | Do homework| No*      | One of edit_id, edit_gid, or edit_name required |
| operation    | Operation type   | One of the following:<br/>complete<br/>undo_complete<br/>delete | complete | Yes | complete - Complete task<br/>undo_complete - Undo completion<br/>delete - Delete task |

**Response:**

| Field        | Type    | Description      | Example | Notes            |
| ------------ | ------- | ---------------- | ------- | ---------------- |
| main_task_id | Number  | Main task ID     | 1       |                  |
| subtask_id   | Number  | Subtask ID       | 2       |                  |
| subtask_gid  | Number  | Subtask group ID | 3       | May be empty     |

<br/>
