# subtask

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**subtask

**说明：**新建或编辑子任务

**示例：**

- 为任务ID为1的主任务添加子任务：[lifeup://api/subtask?main_id=1&todo=完成作业](lifeup://api/subtask?main_id=1&todo=完成作业)
- 编辑子任务并设置奖励：[lifeup://api/subtask?main_id=1&edit_id=2&coin=10&exp=5](lifeup://api/subtask?main_id=1&edit_id=2&coin=10&exp=5)

| 参数          | 含义           | 取值                | 示例      | 是否必须 | 备注                           |
| ------------ | -------------- | ------------------ | --------- | -------- | ------------------------------ |
| main_id      | 主任务ID       | 大于 0 的数字      | 1         | 否*      | main_id、main_gid、main_name 必须提供其中一个 |
| main_gid     | 主任务组ID     | 大于 0 的数字      | 1         | 否*      | main_id、main_gid、main_name 必须提供其中一个 |
| main_name    | 主任务名称     | 任意文本           | 学习任务   | 否*      | main_id、main_gid、main_name 必须提供其中一个 |
| edit_id      | 编辑的子任务ID | 大于 0 的数字      | 2         | 否*      | 编辑时与 edit_gid、edit_name 必须提供其中一个；新建时无需提供 |
| edit_gid     | 编辑的子任务组ID| 大于 0 的数字     | 2         | 否*      | 编辑时与 edit_id、edit_name 必须提供其中一个；新建时无需提供 |
| edit_name    | 编辑的子任务名称| 任意文本          | 完成作业   | 否*      | 编辑时与 edit_id、edit_gid 必须提供其中一个；新建时无需提供 |
| todo         | 任务内容       | 任意文本           | 完成作业   | 否       | 新建时必须提供                 |
| remind_time  | 提醒时间       | 时间戳（毫秒）     | 1640995200000 | 否    | 传入 null 可清除提醒时间       |
| order        | 排序           | 整数              | 1          | 否       | 任务在列表中的排序位置          |
| coin         | 金币奖励       | [0, 999999]      | 10         | 否       | 完成任务获得的金币数量          |
| coin_var     | 金币奖励浮动值 | 整数              | 5          | 否       | 金币奖励的浮动范围             |
| exp          | 经验值奖励     | [0, 99999]       | 5          | 否       | 完成任务获得的经验值            |
| auto_use_item| 自动使用物品   | true 或者 false   | true       | 否       | 完成任务时是否自动使用物品      |
| item_id      | 物品ID         | 大于 0 的数字     | 1          | 否*      | 与 item_name 必须提供其中一个   |
| item_name    | 物品名称       | 任意文本          | 生命药水    | 否*      | 与 item_id 必须提供其中一个     |
| item_amount  | 物品数量       | 大于 0 的数字     | 1          | 否       | 仅在设置物品奖励时有效          |
| items        | 物品奖励JSON   | JSON文本          | [{"item_id":1,"amount":1}] | 否 | 可一次性设置多个物品奖励      |
| coin_set_type| 金币奖励类型   | 以下数值其一：<br/>absolute<br/>relative | absolute | 否 | absolute - 直接设置金币为 value<br/>relative - 在原金币值的基础上增加或减少 |
| exp_set_type | 经验值奖励类型 | 以下数值其一：<br/>absolute<br/>relative | absolute | 否 | absolute - 直接设置经验值为 value<br/>relative - 在原经验值的基础上增加或减少 |

**返回数据：**

| 字段名        | 类型   | 说明         | 示例 | 备注             |
| ------------ | ------ | ------------ | ---- | ---------------- |
| main_task_id | 数字   | 主任务ID     | 1    |                  |
| subtask_id   | 数字   | 子任务ID     | 2    |                  |
| subtask_gid  | 数字   | 子任务组ID   | 3    | 可能为空         |

<br/>
