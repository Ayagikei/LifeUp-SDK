# subtask_operation

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**subtask_operation

**说明：**对子任务进行完成、撤销完成或删除操作

**示例：**

- 完成子任务：[lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=complete](lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=complete)
- 删除子任务：[lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=delete](lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=delete)
- 撤销完成子任务：[lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=undo_complete](lifeup://api/subtask_operation?main_id=1&edit_id=2&operation=undo_complete)

| 参数          | 含义           | 取值                | 示例      | 是否必须 | 备注                           |
| ------------ | -------------- | ------------------ | --------- | -------- | ------------------------------ |
| main_id      | 主任务ID       | 大于 0 的数字      | 1         | 否*      | main_id、main_gid、main_name 必须提供其中一个 |
| main_gid     | 主任务组ID     | 大于 0 的数字      | 1         | 否*      | main_id、main_gid、main_name 必须提供其中一个 |
| main_name    | 主任务名称     | 任意文本           | 学习任务   | 否*      | main_id、main_gid、main_name 必须提供其中一个 |
| edit_id      | 子任务ID       | 大于 0 的数字      | 2         | 否*      | edit_id、edit_gid、edit_name 必须提供其中一个 |
| edit_gid     | 子任务组ID     | 大于 0 的数字      | 2         | 否*      | edit_id、edit_gid、edit_name 必须提供其中一个 |
| edit_name    | 子任务名称     | 任意文本           | 完成作业   | 否*      | edit_id、edit_gid、edit_name 必须提供其中一个 |
| operation    | 操作类型       | 以下数值其一：<br/>complete<br/>undo_complete<br/>delete | complete | 是 | complete - 完成任务<br/>undo_complete - 撤销完成<br/>delete - 删除任务 |

**返回数据：**

| 字段名        | 类型   | 说明         | 示例 | 备注             |
| ------------ | ------ | ------------ | ---- | ---------------- |
| main_task_id | 数字   | 主任务ID     | 1    |                  |
| subtask_id   | 数字   | 子任务ID     | 2    |                  |
| subtask_gid  | 数字   | 子任务组ID   | 3    | 可能为空         |

<br/>
