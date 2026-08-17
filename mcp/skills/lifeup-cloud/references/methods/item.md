# item

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**item

**说明：**对现有商品进行修改，包括价格、库存、效果等各项属性

**示例：**

- 调整价格：[lifeup://api/item?id=1&set_price=1&set_price_type=relative](lifeup://api/item?id=1&set_price=1&set_price_type=relative)
- 修改效果：[lifeup://api/item?effects=%5B%7B%22type%22%3A2%2C%22info%22%3A%7B%22min%22%3A100%2C%22max%22%3A200%7D%7D%5D&id=1](lifeup://api/item?effects=%5B%7B%22type%22%3A2%2C%22info%22%3A%7B%22min%22%3A100%2C%22max%22%3A200%7D%7D%5D&id=1)
  - effect 参数的编码前内容为：`[{"type":2,"info":{"min":100,"max":200}}]`

| 参数              | 含义           | 取值                | 示例       | 是否必须 | 备注                           |
| ---------------- | -------------- | ------------------ | ---------- | -------- | ------------------------------ |
| id               | 商品ID         | 大于 0 的数字      | 1          | 否*      | id 和 name 必须提供其中一个    |
| name             | 商品名称       | 任意文本           | 宝箱       | 否*      | 用于模糊搜索商品，不是用于改名 |
| set_name         | 修改名称       | 任意文本           | 宝箱       | 否       | 不可为空                       |
| set_desc         | 修改描述       | 任意文本           | 获得礼物   | 否       |                               |
| set_icon         | 修改图标       | URL文本            | http://... | 否       | 必须是网络URL地址              |
| set_price        | 调整价格       | 整数               | 1          | 否       |                               |
| set_price_type   | 价格调整方式   | absolute 或 relative | relative | 否       | absolute-直接设置<br/>relative-增减值 |
| own_number       | 调整拥有数量   | 整数               | 1          | 否       | 使用relative时支持负数         |
| own_number_type  | 拥有数调整方式 | absolute 或 relative | relative | 否       | absolute-直接设置<br/>relative-增减值 |
| stock_number     | 调整库存数量   | [-1, 99999]       | 1          | 否       | -1表示无限库存                 |
| stock_number_type| 库存调整方式   | absolute 或 relative | relative | 否       | absolute-直接设置<br/>relative-增减值 |
| disable_purchase | 禁止购买       | true 或者 false    | false      | 否       | 默认为 false                   |
| disable_use      | 禁止使用       | true 或者 false    | false      | 否       | 默认为 false                   |
| action_text      | 使用按钮文案   | 任意文本           | 使用       | 否       |                               |
| title_color_string| 标题颜色      | 颜色字符串         | #66CCFF    | 否       | #需要转义为%23<br/>传空值可恢复默认 |
| effects          | 使用效果       | JSON文本           | 参见[商品效果结构](#4-商品效果结构) | 否 | 设置商品使用效果 |
| purchase_limit   | 限制规则       | JSON文本           | 参见[购买限制结构](#3-购买限制结构) | 否 | 传 `null` 可清空全部限制 |
| limit_scope      | 限制作用范围   | purchase / use / both | purchase | 否 | 仅在传入该字段时才会更新；当 `purchase_limit` 被清空时会自动一并清空 |
| category_id      | 所属分类ID     | 大于等于 0 的数字  | 1          | 否       | 0表示默认分类                  |
| order            | 显示顺序       | 整数               | 1          | 否       | 在分类中的排序位置             |
| unlist           | 下架商品       | true 或者 false    | false      | 否       | 默认为 false                   |

!> id 和 name 参数必须提供其中一个，用于定位要修改的商品

<br/>

#### 调整开箱效果
