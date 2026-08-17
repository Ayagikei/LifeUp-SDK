# add_item

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**add_item

**说明：**创建商品，包含自定义购买限制和使用效果等功能

**示例：**[lifeup://api/add_item?name=休息10分钟&desc=去好好休息一小段时间吧！&price=10&action_text=休息](lifeup://api/add_item?name=休息10分钟&desc=去好好休息一小段时间吧！&price=10&action_text=休息)

| 参数             | 含义           | 取值                | 示例         | 是否必须 | 备注                           |
| --------------- | -------------- | ------------------ | ------------ | -------- | ------------------------------ |
| name            | 商品名称       | 任意文本           | 休息10分钟    | 是      |                                |
| desc            | 描述           | 任意文本           | 休息一下      | 否       |                               |
| icon            | 图标           | 网络地址URL        | http://...    | 否      | 必须是网络URL地址              |
| price           | 价格           | [0, 999999]       | 10            | 否      | 默认为 0                       |
| stock_number    | 库存数量       | [-1, 99999]       | -1            | 否      | -1 表示无限                    |
| action_text     | 使用按钮文案   | 任意文本           | 休息          | 否      |                                |
| disable_purchase| 禁止购买       | true 或者 false    | false         | 否      | 默认为 false                   |
| disable_use     | 禁止使用       | true 或者 false    | false         | 否      | 默认为 false                   |
| category        | 分类ID         | 大于或等于 0 的数字 | 0             | 否      | 0 表示默认分类                 |
| order           | 显示顺序       | 整数              | 1             | 否       | 在分类中的排序位置              |
| purchase_limit  | 限制规则       | JSON文本          | 参见[购买限制结构](#3-购买限制结构) | 否 | 可配置购买/使用限制 |
| limit_scope     | 限制作用范围   | purchase / use / both | purchase | 否 | 仅在 `purchase_limit` 非空时生效，默认值为 `purchase` |
| effects         | 使用效果       | JSON文本          | 参见[商品效果结构](#4-商品效果结构) | 否 | 使用商品时的效果 |
| own_number      | 初始拥有数量   | 整数              | 0             | 否       | 设置初始库存数量               |
| unlist          | 从商店隐藏     | true 或者 false   | false         | 否       | 默认为 false                   |

**返回数据：**

| 字段名   | 类型   | 说明       | 示例  | 备注             |
| -------- | ------ | ---------- | ----- | ---------------- |
| item_id  | 数字   | 商品ID     | 1000  | 创建的商品ID     |

!> effects 参数会覆盖 disable_use 的设置。如果您设置了 effects 指定商品不可使用，disable_use 将被忽略。

<br/>

#### 调整商品

?> 需要 v1.98.0+
