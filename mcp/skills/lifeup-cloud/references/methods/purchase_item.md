# purchase_item

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**purchase_item

**说明：**购买指定的商品

**示例：**

- 购买ID为1的商品：[lifeup://api/purchase_item?id=1](lifeup://api/purchase_item?id=1)
- 购买名称为"生命药水"的商品：[lifeup://api/purchase_item?name=生命药水](lifeup://api/purchase_item?name=生命药水)
- 购买5个ID为1的商品：[lifeup://api/purchase_item?id=1&purchase_quantity=5](lifeup://api/purchase_item?id=1&purchase_quantity=5)

如果商品配置了 `purchase_limit`，且 `limit_scope` 包含 `purchase`，该接口也会校验这些限制。

| 参数              | 含义     | 取值              | 示例       | 是否必须 | 备注                            |
| ---------------- | -------- | ----------------- | ---------- | -------- | ------------------------------- |
| id               | 商品ID   | 大于 0 的数字      | 1         | 否*      | id 和 name 必须提供其中一个     |
| name             | 商品名称 | 任意文本           | 生命药水   | 否*      | id 和 name 必须提供其中一个     |
| purchase_quantity| 购买数量 | 大于 0 的数字      | 5         | 否       | 默认值为 1                      |

**返回数据：**

| 字段名 | 类型   | 说明     | 示例            | 备注                    |
| ------ | ------ | -------- | --------------- | ---------------------- |
| itemId | 数字   | 商品ID   | 1               | 仅购买成功时返回        |
| result | 整数   | 结果代码 | 0               | 见下方结果代码说明      |
| desc   | 文本   | 结果描述 | PurchaseSuccess | 见下方结果代码说明      |

**结果代码说明：**

| 代码 | 描述                        | 说明                          |
| ---- | -------------------------- | ----------------------------- |
| 0    | PurchaseSuccess           | 购买成功                       |
| 1    | DatabaseError             | 数据库错误                     |
| 2    | NotEnoughCoin             | 金币不足                       |
| 3    | ItemNotFound              | 商品未找到                     |
| 4    | PurchaseAndUseSuccess     | 购买并使用成功                 |
| 5    | PurchaseSuccessAndUseFailure | 购买成功但使用失败           |
| 6    | NotPurchaseable           | 因商品设置或限制条件而不可购买 |
| 7    | OutOfStock                | 商店库存不足                   |

<br/>
