# edit_task

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**edit_task

**说明：**编辑已有任务的内容和属性

**示例：**
[lifeup://api/edit_task?id=1&todo=修改后的任务内容&notes=备注&coin=10&exp=20&skills=1&skills=2&category=0](lifeup://api/edit_task?id=1&todo=修改后的任务内容&notes=备注&coin=10&exp=20&skills=1&skills=2&category=0)

| 参数                | 含义           | 取值                | 示例     | 是否必须 | 备注                           |
| ------------------ | -------------- | ------------------ | -------- | -------- | ------------------------------ |
| id                 | 任务ID         | 大于 0 的数字      | 1        | 否*      | id、gid、name 必须提供其中一个  |
| gid                | 任务组ID       | 大于 0 的数字      | 1        | 否*      | id、gid、name 必须提供其中一个  |
| name               | 任务名称       | 任意文本           | 写日记    | 否*      | id、gid、name 必须提供其中一个  |
| todo               | 任务内容       | 任意文本           | 写周记    | 否       |                               |
| notes              | 备注           | 任意文本           | 备注内容  | 否       |                               |
| coin               | 金币奖励       | 大于等于0的数字     | 10       | 否       | 完成任务获得的金币数量，受系统限制约束         |
| coin_var           | 金币浮动值     | 大于 0 的数字      | 1        | 否       | 奖励将在 [coin, coin+coin_var] 范围内随机 |
| exp                | 经验值奖励     | 大于等于0的数字     | 20       | 否       | 完成任务获得的经验值，受系统限制约束           |
| skills             | 技能ID         | 大于 0 的数字数组   | 1        | 否       | 支持数组（如 &skills=1&skills=2）|
| category           | 列表ID         | 大于或等于 0 的数字 | 0        | 否       | 0 表示默认列表，不能选择智能列表 |
| frequency       | 重复频次       | 整数               | 0         | 否       | 默认为 0（单次）<br/>0 - 单次<br/>1 - 每日<br/>N (N>1) - 每 N 日<br/>-1 - 无限<br/>-3 - 艾宾浩斯（需v1.99.1）<br/>-4 - 每月<br/>-5 - 每年 |
| importance         | 重要程度       | [1, 4]            | 1        | 否       | 默认为 1                       |
| difficulty         | 难度等级       | [1, 4]            | 2        | 否       | 默认为 1                       |
| deadline           | 截止时间       | 时间戳（毫秒）     | 1640995200000 | 否 |                               |
| no_deadline        | 无期限         | true/false         | true     | 否       | v1.104.0+；仅对重复任务有效，传入 `&no_deadline=true` 时会清除具体截止时间 |
| remind_time        | 提醒时间       | 时间戳（毫秒）     | 1640995200000 | 否 |                               |
| start_time         | 开始时间       | 时间戳（毫秒）     | 1640995200000 | 否 |                               |
| color              | 标签颜色       | 颜色字符串         | #66CCFF  | 否       | #需要转义为%23                 |
| background_url     | 背景图片URL    | 网络地址URL        | http://example.com/bg.jpg | 否 |                    |
| background_alpha   | 背景透明度     | [0, 1] 之间的浮点数 | 0.5      | 否       |                               |
| enable_outline     | 启用文字轮廓   | true 或者 false    | false    | 否       | 仅当设置 background_url 时有效，为文字添加轮廓以提高可读性 |
| use_light_remark_text_color | 使用浅色备注文字 | true 或者 false | false | 否 | 仅当设置 background_url 时有效，使用浅色显示备注文字 |
| item_id            | 物品ID         | 大于 0 的数字      | 1        | 否*      | 与 item_name 必须提供其中一个  |
| item_name          | 物品名称       | 任意文本           | 宝箱      | 否*      | 与 item_id 必须提供其中一个    |
| item_amount        | 物品数量       | [1, 99]           | 1        | 否       | 默认为 1                       |
| items              | 物品奖励JSON   | JSON文本           | [{"item_id":1,"amount":1}] | 否 | 可一次设置多个物品奖励 |
| auto_use_item      | 自动使用物品   | true 或者 false    | false    | 否       |                               |
| frozen             | 是否冻结       | true 或者 false    | false    | 否       | 默认为 false                  |
| freeze_until       | 冻结截止时间   | 时间戳（毫秒）     | 1640995200000 | 否 | 仅当 frozen 为 true 时生效    |
| coin_penalty_factor| 金币惩罚系数   | [0, 100) 之间的浮点数 | 0.5    | 否       |                               |
| exp_penalty_factor | 经验惩罚系数   | [0, 100) 之间的浮点数 | 0.5    | 否       |                               |
| write_feelings     | 是否启用感想   | true 或者 false    | false    | 否       |                               |
| pin                | 是否置顶       | true 或者 false    | false    | 否       |                               |
| words              | 完成激励语     | 任意文本           | 太棒了！  | 否       | 任务完成时显示的激励文本       |
| task_type       | 任务类型       | [0, 4]            | 0         | 否       | 需v1.99.1, 0 - 普通任务<br/>1 - 计数任务<br/>2 - 负面任务<br/>3 - API任务<br/>4 - 计时任务（v1.102.0+） |
| target_times    | 目标次数       | 大于0的整数        | 1         | 否       | 仅当 task_type 为1(计数任务)时有效 |
| is_affect_shop_reward | 是否影响商店奖励 | true/false      | false    | 否       | 当 task_type 为1(计数任务)时有效，是否影响商品的奖励计算         |
| enable_proportional_settlement | 启用按比例结算 | true/false | false | 否 | v1.104.0+；仅当 task_type 为1(计数任务)时有效。编辑现有计数任务时，可单独传入该参数来开关按比例结算；改变该设置或奖励配置时，可能按应用内规则重置/回滚已结算进度 |
| expected_focus_minutes | 预期专注分钟数 | 大于 0 的整数 | 25 | 否 | 仅当 task_type 为4(计时任务)时有效；默认 25（v1.102.0+） |
| repeat_target_times | 重复结束次数 | 大于 0 的整数 | 3 | 否 | 仅对重复任务有效（frequency 非 0 / -1）；当 repeat_target_times 与 repeat_end_date 同时提供时，repeat_target_times 优先生效（v1.102.0+） |
| repeat_end_date | 重复结束日期 | 时间戳（毫秒） | 1640995200000 | 否 | 仅对重复任务有效（frequency 非 0 / -1）（v1.102.0+） |
| repeat_end_behavior | 重复结束后的行为 | 0 或 1 | 0 | 否 | 0 - 终止任务<br/>1 - 冻结任务（v1.102.0+） |
| coin_set_type     | 如何设置金币值 | One of:<br/>absolute<br/>relative | absolute | 否 | absolute - 直接设置金币为 value<br/>relative - 在原金币值的基础上增加或减少 |
| exp_set_type      | 如何设置经验值 | One of:<br/>absolute<br/>relative | absolute | 否 | absolute - 直接设置经验值为 value<br/>relative - 在原经验值的基础上增加或减少 |

**返回数据：**

| 字段名    | 类型   | 说明       | 示例 | 备注             |
| --------- | ------ | ---------- | ---- | ---------------- |
| task_id   | 数字   | 任务ID     | 1000 |                  |
| task_gid  | 数字   | 任务组ID   | 1000 |                  |

<br/>
