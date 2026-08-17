# edit_pomodoro

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**edit_pomodoro

**说明：**编辑现有的番茄计时记录或添加新的记录，如果提供有效的 `edit_item_id`。

**示例：**

- 编辑指定 ID 的记录，设置时长为 45 分钟（2700000 毫秒），并奖励番茄：[lifeup://api/edit_pomodoro?edit_item_id=123&duration=2700000&reward_tomatoes=true](lifeup://api/edit_pomodoro?edit_item_id=123&duration=2700000&reward_tomatoes=true)
- 根据开始和结束时间编辑记录：[lifeup://api/edit_pomodoro?start_time=1659322800000&end_time=1659326400000&edit_item_id=456](lifeup://api/edit_pomodoro?start_time=1659322800000&end_time=1659326400000&edit_item_id=456)

**参数：**

| 参数            | 含义                   | 取值            | 示例          | 是否必须 | 备注                                        |
| --------------- | ---------------------- | --------------- | ------------- | -------- | ------------------------------------------- |
| task_id         | 任务 ID                | 大于 0 的数字   | 101           | 否       | 任务的唯一标识                              |
| task_gid        | 任务组 ID              | 大于 0 的数字   | 5             | 否       | 如果提供，会覆盖 task_id                    |
| task_name       | 任务名称               | 任意文本        | 学习          | 否       | 如果 task_id 或 task_gid 不提供，则必须提供 |
| start_time      | 计时开始时间           | 时间戳          | 1659322800000 | 否*      | 可以百度了解时间戳的定义                    |
| end_time        | 计时结束时间           | 时间戳          | 1659326400000 | 否*      | -                                           |
| duration        | 专注时长               | 数字（毫秒）    | 2700000       | 否*      | 至少为 30000 毫秒（30秒）                   |
| reward_tomatoes | 是否奖励番茄           | true 或者 false | true          | 否       | 默认为 false                                |
| edit_item_id    | 编辑项的 ID            | 大于 0 的数字   | 123           | 是       | 指定编辑的记录 ID                           |
| ui              | 是否展示奖励番茄数的UI | true 或者 false | true          | 否       |                                             |

**返回值：**

| 参数     | 含义                 | 取值 | 示例 | 是否必须 | 备注                     |
| -------- | -------------------- | ---- | ---- | -------- | ------------------------ |
| tomatoes | 此次操作获得的番茄数 | 数字 | 2    | 否       | 如果 `ui` 为 true 时返回 |

**注意：**

1. `start_time`, `end_time`, `duration` 必须至少提供一个。
2. `end_time` 需要大于 `start_time`。
3. `duration` 应该小于或等于 (`end_time` - `start_time`)。
4. 如果提供 `edit_item_id` 且找到对应记录进行编辑；否则根据其他参数创建新记录。

<br/>
