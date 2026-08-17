# loot_box

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**loot_box

**说明：**修改指定箱子的开箱效果，支持调整概率、奖励数和增加内容物。（暂不支持删除）

**示例：**[lifeup://api/loot_box?name=金币箱&sub_name=【大】袋金币&set_type=relative&probability=1&fixed=false](lifeup://api/loot_box?name=金币箱&sub_name=【大】袋金币&set_type=relative&amount=1&probability=1&fixed=false)

**解释：**增加金币箱中的【大】袋金币的比重 1 点。

| 参数        | 含义                  | 取值                                     | 示例         | 是否必须 | 备注                                                         |
| ----------- | --------------------- | ---------------------------------------- | ------------ | -------- | ------------------------------------------------------------ |
| id          | 商品id                | 大于0的数字                              | 1            | 否*      | 获取方式请查看上文 「基础知识 - 人升数据 ID」                |
| name        | 商品名称              | 任意文本                                 | 金币箱       | 否*      | 用于未知 id 时，模糊搜索商品，并非修改名称                   |
| sub_id      | 箱子内容物的 id       | 大于0的数字                              | 1            | 否*      | 箱子内容物的 id                                              |
| sub_name    | 箱子内容物的名称      | 任意文本                                 | 【大】袋金币 | 否*      | 用于箱子内容物未知 id 时，模糊搜索商品                       |
| set_type    | 调整方式（绝对/相对） | 以下数值其一：<br/>absolute<br/>relative | relative     | 否       | absolute - 绝对取值，即直接将目标设置为 value<br/>relative - 相对取值，比如在原数值的基础上增加或减少<br/>**同时作用于[amount]、[probability]两个字段** |
| amount      | 奖励数                | 数字                                     | 1            | 否       | 某个单一物品的奖励个数                                       |
| probability | 奖励比重              | 数字                                     | 1            | 否       | -                                                            |
| fixed       | 是否是固定奖励        | 布尔值                                   | true/false   | 否       | -                                                            |

**注意：**

1. 为了搜索到商品，必须提供 id 或 name 其一。
1. 为了搜索到内容物，必须提供 sub_id 或 sub_name 其一。
1. `name` 和 `sub_name` 会先完整匹配，匹配不到再模糊匹配。
1. 旧版 `loot_box` API 保持兼容语义：如果同一内容物存在多个不同数量的条目，会编辑匹配到的第一条，不能用 `sub_amount` 消歧。需要按数量精确编辑、删除或合并条目时，请使用 `loot_box/v2`。

<br/>

#### 调整开箱效果（v2）

?> 该 API 于 v1.104.2 版本引入。
