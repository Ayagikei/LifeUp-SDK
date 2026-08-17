# add_pomodoro

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**add_pomodoro

**说明：**添加番茄计时记录

**示例：**

- 添加时长为 25 分钟（1500000 毫秒）的计时记录，并指向名称包含学习的任务：[lifeup://api/add_pomodoro?task_name=学习&duration=1500000](lifeup://api/add_pomodoro?task_name=学习&duration=1500000)
- 添加2022-08-01 11:00:00 - 2022-08-01 12:00:00 的计时记录：[lifeup://api/add_pomodoro?start_time=1659322800000&end_time=1659326400000](lifeup://api/add_pomodoro?start_time=1659322800000&end_time=1659326400000)

**解释：**

任务id/任务组id的解释可以查看上文的「完成任务」接口。

| 参数            | 含义                   | 取值                          | 示例          | 是否必须 | 备注                                          |
| --------------- | ---------------------- | ----------------------------- | ------------- | -------- | --------------------------------------------- |
| start_time      | 计时开始时间           | 时间戳                        | 1659322800000 | 否*      | 可以百度了解时间戳的定义                      |
| duration        | 专注时长               | 数字（毫秒）<br/>需大于 30000 | 1500000       | 否*      | -                                             |
| end_time        | 计时结束时间           | 时间戳                        | 1659326400000 | 否*      | -                                             |
| reward_tomatoes | 是否奖励番茄           | true 或者 false               | true          | 否       | 默认为 false                                  |
| task_id         | 任务id                 | 大于 0 的数字                 | 1             | 否       | 获取方式请查看上文 「基础知识 - 人升数据 ID」 |
| task_gid        | 任务组id               | 大于 0 的数字                 | 1             | 否       | 获取方式请查看上文 「基础知识 - 人升数据 ID」 |
| task_name       | 名称                   | 任意文本                      | 学习          | 否       | 模糊搜索，只会搜索到的其中一个任务            |
| ui              | 是否展示奖励番茄数的UI | true 或者 false               | true          | 否       | v1.94.0 引入，默认为 true                     |

**注意：**

1. start_time, duration, end_time 必须提供其一。
2. 在只有 duration 的情况下，会默认 end_time 为当前时间。
3. end_time 需要大于  start_time。
4. duration 至少为 30000 毫秒（30秒）。
5. 如果同时提供了 start_time, duration, end_time，duration 应该比 (end_time - start_time) 小或相等。

<br/>

#### 编辑番茄记录

> 该方法引入自v1.94.0
