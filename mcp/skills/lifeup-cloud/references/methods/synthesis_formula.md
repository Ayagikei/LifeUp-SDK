# synthesis_formula

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**synthesis_formula

**说明：**新建、修改或删除合成配方

**示例：**

- 创建新配方：[lifeup://api/synthesis_formula?inputItems=%5B%7B%22item_id%22%3A%20296%2C%20%22amount%22%3A%2088%7D%5D&outputItems=%5B%7B%22item_id%22%3A%20295%2C%20%22amount%22%3A%201%7D%5D](lifeup://api/synthesis_formula?inputItems=%5B%7B%22item_id%22%3A%20296%2C%20%22amount%22%3A%2088%7D%5D&outputItems=%5B%7B%22item_id%22%3A%20295%2C%20%22amount%22%3A%201%7D%5D)
  - 这里的 inputItems 为 `[{"item_id": 296, "amount": 88}]`
  - 这里的 outputItems 为 `[{"item_id": 295, "amount": 1}]`
- 删除配方：[lifeup://api/synthesis_formula?id=1&delete=true](lifeup://api/synthesis_formula?id=1&delete=true)

| 参数        | 含义       | 取值                | 示例                               | 是否必须 | 备注                   |
| ----------- | ---------- | ------------------- | ---------------------------------- | -------- | ---------------------- |
| id          | 配方ID     | 大于 0 的数字       | 1                                  | 否       | 修改或删除时必须提供   |
| delete      | 是否删除   | true 或者 false     | true                               | 否       | 仅删除配方时使用       |
| inputItems  | 材料列表   | 商品数组，格式见下文 | [{"item_id":1,"amount":2}]          | 是       | 新建或修改时必须提供   |
| outputItems | 产物列表   | 商品数组，格式见下文 | [{"item_id":3,"amount":1}]          | 是       | 新建或修改时必须提供   |
| category    | 分类ID     | 大于 0 的数字       | 1                                  | 否       | 如不提供，默认为默认清单         |

!> inputItems 和 outputItems 的格式为 JSON 数组，每个商品包含 item_id（商品ID）和 amount（数量）两个字段，所有商品ID必须存在且数量必须大于0

**返回数据：**

| 字段名    | 类型   | 说明       | 示例        | 备注               |
| --------- | ------ | ---------- | ----------- | ------------------ |
| formulaId | 数字   | 配方ID     | 1           | 操作成功时返回     |
| result    | 整数   | 结果代码   | 0           | 见下方结果代码说明 |
| desc      | 文本   | 结果描述   | AddSuccess  | 见下方结果代码说明 |

**结果代码说明：**

| 代码 | 描述         | 说明       |
| ---- | ------------ | ---------- |
| 0    | Success      | 操作成功   |
| 1    | Failed       | 操作失败   |

<br/>
