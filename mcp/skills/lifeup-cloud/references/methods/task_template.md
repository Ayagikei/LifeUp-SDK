# task_template

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**task_template

**说明：**任务模板的 CRUD（列出/获取/创建/更新/删除）。

**示例：**

- 列出模板：`lifeup://api/task_template?method=list`
- 创建（直接指定模板参数）：`lifeup://api/task_template?method=create&name=我的模板&todo=模板任务&frequency=0`
- 创建（从已有任务生成）：`lifeup://api/task_template?method=create&name=我的模板&from_task_id=1`
- 获取模板：`lifeup://api/task_template?method=get&id=1`
- 更新模板名称：`lifeup://api/task_template?method=update&id=1&name=新名称`
- 从任务更新模板内容：`lifeup://api/task_template?method=update&id=1&from_task_id=1`
- 删除模板：`lifeup://api/task_template?method=delete&id=1`

| 参数 | 含义 | 取值 | 示例 | 是否必须 | 备注 |
| ---- | ---- | ---- | ---- | -------- | ---- |
| method | 操作 | list / get / create / update / delete | list | 是 | - |
| id | 模板ID | 大于 0 的数字 | 1 | 否* | get/update/delete 必填；别名：template_id |
| template_id | 模板ID | 大于 0 的数字 | 1 | 否* | id 的别名 |
| name | 模板名称 | 任意文本 | 我的模板 | 否* | create 必填；update 时如果不传 from_task_id/from_task_gid 则必填 |
| from_task_id | 从任务生成/更新 | 大于 0 的数字 | 1 | 否 | 用于 create/update |
| from_task_gid | 从任务组生成/更新 | 大于 0 的数字 | 1 | 否 | 用于 create/update |
| todo | 模板任务内容 | 任意文本 | 写日记 | 否* | create 且不传 from_task_id/from_task_gid 时必填 |
| notes | 模板备注 | 任意文本 | 备注 | 否 | 默认为空 |
| category | 清单ID | 大于等于 0 的数字 | 0 | 否 | 别名：category_id |
| category_id | 清单ID | 大于等于 0 的数字 | 0 | 否 | category 的别名 |
| frequency | 重复频次 | 整数 | 0 | 否 | 与 add_task 的 frequency 含义一致 |
| importance | 重要程度 | [1, 4] | 1 | 否 | - |
| difficulty | 困难程度 | [1, 4] | 1 | 否 | - |
| coin | 金币奖励 | 数字 | 10 | 否 | - |
| coin_var | 金币奖励浮动值 | 数字 | 1 | 否 | - |
| exp | 经验值奖励 | 数字 | 100 | 否 | - |
| skills | 技能ID | 多参数数组 | 1 | 否 | 支持数组（如 &skills=1&skills=2） |
| skill_ids | 技能ID | JSON数组或逗号分隔 | [1,2] | 否 | skills 的替代写法 |
| deadline | 截止时间 | 时间戳（毫秒） | 1640995200000 | 否 | - |
| start_time | 开始时间 | 时间戳（毫秒） | 1640995200000 | 否 | - |
| remind_time | 提醒时间 | 时间戳（毫秒） | 1640995200000 | 否 | - |
| words | 完成激励语 | 任意文本 | 太棒了！ | 否 | - |
| task_type | 任务类型 | [0, 4] | 0 | 否 | 0 - 普通任务<br/>1 - 计数任务<br/>2 - 负面任务<br/>3 - API任务<br/>4 - 计时任务 |
| target_times | 目标次数 | 大于 0 的整数 | 10 | 否 | 仅当 task_type 为 1(计数任务)时有效 |
| is_affect_shop_reward | 是否影响商店奖励 | true/false | false | 否 | 仅当 task_type 为 1(计数任务)时有效 |
| enable_proportional_settlement | 启用按比例结算 | true/false | false | 否 | v1.104.0+；仅当 task_type 为 1(计数任务)时有效。模板生成任务后会保留该计数任务结算设置 |
| expected_focus_minutes | 预期专注分钟数 | 大于 0 的整数 | 25 | 否 | 仅当 task_type 为 4(计时任务)时有效 |
| repeat_end_mode | 重复结束模式 | 0 或 1 | 0 | 否 | 仅对重复任务有效（frequency 非 0 / -1）<br/>0 - 按次数结束<br/>1 - 按日期结束 |
| repeat_target_times | 重复结束次数 | 大于 0 的整数 | 3 | 否 | repeat_end_mode=0 时使用（或通过该字段自动推断） |
| repeat_end_date | 重复结束日期 | 时间戳（毫秒） | 1640995200000 | 否 | repeat_end_mode=1 时使用（或通过该字段自动推断） |
| repeat_end_behavior | 重复结束后的行为 | 0 或 1 | 0 | 否 | 0 - 终止任务<br/>1 - 冻结任务 |

**返回值：**

| 字段 | 含义 | 类型 | 备注 |
| ---- | ---- | ---- | ---- |
| templates | 模板列表（JSON字符串） | 文本 | 仅 method=list |
| count | 模板数量 | 数字 | 仅 method=list |
| template | 模板详情（JSON字符串） | 文本 | 仅 method=get |
| id | 模板ID | 数字 | get/create/update/delete |
| name | 模板名称 | 文本 | get/create/update |
| success | 是否成功 | true/false | create/update/delete |

<br/>
