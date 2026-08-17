# complete

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**complete

**说明：**触发任务完成，只会搜索到未完成的任务

**示例：**

- 完成id为1的任务：[lifeup://api/complete?id=1](lifeup://api/complete?id=1)
- 完成「任务组id」为1的任务：[lifeup://api/complete?gid=1](lifeup://api/complete?gid=1)
- 根据名字搜索任务并完成：[lifeup://api/complete?name=开始使用&ui=true](lifeup://api/complete?name=开始使用&ui=true)

**解释：**

每个任务都有一个 id。

对于重复任务而言，每次重复id都会刷新，但「任务组id」会保持不变。

id 的获取方法为「实验」页面打开「开发者模式」，然后在「任务详情」页面即可查看。

| 参数                      | 含义                     | 取值                                     | 示例     | 是否必须 | 备注                                                         |
| ------------------------- | ------------------------ | ---------------------------------------- | -------- | -------- | ------------------------------------------------------------ |
| id                        | 任务id                   | 大于 0 的数字                            | 1        | 否*      | 任务id；如果是重复任务，每次重复，id都会更新。               |
| gid                       | 任务组id                 | 大于 0 的数字                            | 1        | 否*      | 任务组id；                                                   |
| name                      | 名称                     | 任意文本                                 | 睡觉     | 否*      | 模糊搜索，只会完成搜索到的其中一个任务                       |
| ui                        | 是否展示弹窗UI           | true 或 false                            | true     | 否       | 默认为 false，只在后台显示一条消息                           |
| 以下参数引入自v1.94.0版本 |                          |                                          |          |          |                                                              |
| count                     | 计数值                   | 数字                                     | 1        | 否       | 仅适用于计数任务，请搭配`count_set_type`参数使用             |
| count_set_type            | 如何设置计数值           | 以下数值其一：<br/>absolute<br/>relative | absolute | 否       | 默认值为relative<br/>absolute - 绝对取值，即直接将目标设置为 value<br/>relative - 相对取值，在原数值的基础上增加或减少 |
| count_force_sum_up        | 是否强制结算计数任务奖励 | true 或 false                            | true     | 否       |                                                              |
| reward_factor             | 奖励系数                 | 大于0的浮点数                            | 1.1      | 否       | 不适用于计数任务<br/>奖励系数会影响奖励的经验值、金币数值（暂不影响商品数量） |

**注意：**

1. 为了能够匹配到任务，id、gid、name 必须提供其一。
2. 计时任务不支持通过该接口手动完成（v1.102.0+）。

<br/>

#### 放弃任务
