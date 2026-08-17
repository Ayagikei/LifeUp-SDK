# history_operation

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**history_operation

**说明：**对已完成/已放弃/已过期的任务进行操作

**示例：**

- 删除历史任务：[lifeup://api/history_operation?id=1&operation=delete](lifeup://api/history_operation?id=1&operation=delete)
- 将任务标记为放弃：[lifeup://api/history_operation?id=1&operation=set_to_give_up](lifeup://api/history_operation?id=1&operation=set_to_give_up)
- 重新开始任务：[lifeup://api/history_operation?id=1&operation=restart](lifeup://api/history_operation?id=1&operation=restart)

!> 此 API 仅适用于非未完成状态的任务（已完成、已放弃或已过期）

| 参数           | 含义           | 取值                | 示例     | 是否必须 | 备注                           |
| ------------- | -------------- | ------------------ | -------- | -------- | ------------------------------ |
| id            | 任务ID         | 大于 0 的数字      | 1        | 是       | 历史任务的ID                   |
| operation     | 操作类型       | 以下数值其一：<br/>delete<br/>complete<br/>undo_complete<br/>set_to_give_up<br/>set_to_overdue<br/>edit_completed_time<br/>restart | delete | 是 | delete - 删除任务<br/>complete - 标记为已完成<br/>undo_complete - 撤销完成<br/>set_to_give_up - 标记为放弃<br/>set_to_overdue - 标记为过期<br/>edit_completed_time - 修改完成时间<br/>restart - 重新开始任务 |
| completed_time | 完成时间       | 时间戳（毫秒）     | 1640995200000 | 否* | 仅当 operation 为 edit_completed_time 时必须提供 |

**返回数据：**

| 字段名   | 类型   | 说明     | 示例 | 备注             |
| -------- | ------ | -------- | ---- | ---------------- |
| task_id  | 数字   | 任务ID   | 1000 | 操作的任务ID     |

<br/>
