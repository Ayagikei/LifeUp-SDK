# query

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**query

**说明：**查询参数

**版本：**需要 v1.90.2

**示例：**

- 查询当前金币数：[lifeup://api/query?key=coin](lifeup://api/query?key=coin)

| 参数        | 含义       | 取值                                                         | 示例 | 是否必须                        | 备注                                                         |
| ----------- | ---------- | ------------------------------------------------------------ | ---- | ------------------------------- | ------------------------------------------------------------ |
| key         | 查询的类型 | 仅限以下数值其一：<br/>coin<br/>atm<br/>item<br/>item_id_list<br/>tomato<br/>task | coin | 是                              | coin - 当前金币数<br/>atm - 当前 ATM 存款<br/>item - 指定 `itemId` 的商品信息<br/>item_id_list - 指定`categoryId`的商品id列表<br/>tomato - 番茄数据<br/>task - 任务信息(v1.101.0+) |
| item_id     | 商品id     | 大于 0 的数字                                                | 1    | 当 key 为 item 时，必须         | 查询的商品 id                                                |
| category_id | 清单id     | 大于或者等于 0 的数字                                        | 1    | 当 key 为 item_id_list 时，必须 | 只有当key为`item_id_list`时需要，代表查询的清单 id           |
| task_id / taskId | 任务 ID | 大于 0 的数字 | 1 | 当 key 为 task 时，三选一* | 查询的任务 ID |
| task_gid / taskGid / task_group_id / taskGroupId | 任务组 ID | 大于 0 的数字 | 1 | 当 key 为 task 时，三选一* | 查询的任务组 ID |
| task_name / taskName | 任务名称 | 任意文本 | 学习 | 当 key 为 task 时，三选一* | 模糊匹配任务名称 |
| withSubTasks | 是否包含子任务 | true 或 false | true | 否 | 仅 key 为 task 时可用，默认为 true |

**返回值：**

当查询 coin/atm 时:

| 参数  | 含义           | 取值 | 示例 | 是否必须 | 备注 |
| ----- | -------------- | ---- | ---- | -------- | ---- |
| value | 查询返回的数值 | 数字 | 1000 | 是       |      |

当查询 item 时:

| 参数             | 含义         | 取值     | 示例      | 是否必须 | 备注                           |
| ---------------- | ------------ | -------- | --------- | -------- | ------------------------------ |
| item_id          | 商品id       | 数字     | 1         | 是       | -                              |
| name             | 名称         | 任意文本 | 商品      | 是       | -                              |
| desc             | 描述         | 任意文本 |           | 否       | -                              |
| icon             | 图标         | 任意文本 |           | 否       | -                              |
| category_id      | 类别id       | 数字     |           | 否       | -                              |
| stock_number     | 库存数量     | 数字     | -         | 是       | -1代表无限库存                 |
| own_number       | 仓库拥有数   | 数字     | 10        | 是       | -                              |
| price            | 价格         | 数字     | 100       | 是       | -                              |
| order            | 排序依据     | 数字     | 100       | 是       | 自定义排序时的权重值           |
| disable_purchase | 是否禁止购买 | true 或 false | true | 是 | -                              |
| purchase_limit   | 限制规则     | JSON文本 | [{"limitType":0,"limitNumber":5}] | 是 | 当前商品的限制列表 |
| limit_scope      | 限制作用范围 | purchase / use / both | use | 是 | 以 API 文本值返回 |

当查询 item_id_list 时:

| 参数     | 含义                            | 取值   | 示例    | 是否必须 | 备注 |
| -------- | ------------------------------- | ------ | ------- | -------- | ---- |
| item_ids | 查询返回的商品id数组,以`,`分隔 | 字符串 | 1,2,3,4 | 是       |      |

当查询 tomato 时:

| 参数      | 含义         | 取值 | 示例 | 是否必须 | 备注 |
| --------- | ------------ | ---- | ---- | -------- | ---- |
| total     | 总番茄数量   | 数字 | 100  | 是       |      |
| available | 可用番茄数量 | 数字 | 50   | 是       |      |
| exchanged | 已兑换番茄数量 | 数字 | 50   | 是       |      |

当查询 task 时 (v1.101.0+):

| 参数           | 含义             | 取值      | 示例 | 是否必须 | 备注                           |
| -------------- | ---------------- | --------- | ---- | -------- | ------------------------------ |
| _ID            | 任务ID           | 数字      | 1    | 是       | -                              |
| _GID           | 任务组ID         | 数字      | 1    | 是       | -                              |
| name           | 任务名称         | 文本      | 学习 | 是       | -                              |
| notes          | 备注             | 文本      | -    | 否       | 可能为空                       |
| status         | 任务状态         | 数字      | 0    | 是       | 0=未完成, 1=已完成             |
| startTime      | 开始时间         | 数字      | -    | 是       | Unix时间戳(毫秒)               |
| deadline       | 截止时间         | 数字      | -    | 否       | Unix时间戳(毫秒),可能为空      |
| remindTime     | 提醒时间         | 数字      | -    | 否       | Unix时间戳(毫秒),可能为空      |
| frequency      | 重复频率         | 数字      | -    | 是       | -                              |
| exp            | 经验值奖励       | 数字      | -    | 是       | -                             |
| skillIds       | 技能ID列表       | JSON文本  | -    | 是       | JSON数组格式                   |
| coin           | 金币奖励         | 数字      | -    | 否       | 可能为空                       |
| coinVariable   | 随机金币奖励     | 数字      | -    | 否       | 可能为空                       |
| itemId         | 第一个奖励物品ID | 数字      | -    | 否       | 可能为空                       |
| itemCount      | 第一个奖励物品数量 | 数字    | -    | 否       | 当itemId存在时返回             |
| items          | 物品奖励列表     | JSON文本  | -    | 是       | JSON数组格式                   |
| words          | 完成励志语       | 文本      | -    | 否       | 可能为空                       |
| categoryId     | 分类ID           | 数字      | -    | 否       | 可能为空                       |
| order          | 排序             | 数字      | -    | 是       | -                              |
| name_extended  | 扩展名称         | 文本      | -    | 是       | 与name相同                     |
| subTasks       | 子任务列表       | JSON文本  | -    | 是       | JSON数组格式,见下文说明        |

**子任务(subTasks)字段说明:**

`subTasks` 字段是一个JSON数组,每个元素包含以下字段:

- `id`: 子任务ID
- `gid`: 子任务组ID
- `todo`: 子任务内容
- `status`: 子任务状态(0=未完成, 1=已完成)
- `remindTime`: 提醒时间(Unix时间戳,毫秒)
- `exp`: 经验值奖励
- `coin`: 金币奖励
- `coinVariable`: 随机金币奖励
- `items`: 物品奖励列表
- `order`: 排序
- `autoUseItem`: 是否自动使用物品

<br/>
