# synthesize

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**synthesize

**说明：**使用已有的合成配方合成物品

**示例：**

- 使用ID为1的配方合成一次：[lifeup://api/synthesize?id=1](lifeup://api/synthesize?id=1)
- 使用ID为1的配方合成5次：[lifeup://api/synthesize?id=1&times=5](lifeup://api/synthesize?id=1&times=5)

**广播行为：**

- 该接口对应的是**配方合成**。
- 当开启`广播事件`且配方合成成功后，人升还会额外发送 `app.lifeup.synthesis.complete` 广播事件。
- 该广播**不会**用于 `use_item` 中的简易合成；简易合成仍然归属于 `app.lifeup.item.use`。

| 参数  | 含义     | 取值            | 示例 | 是否必须 | 备注          |
| ----- | -------- | --------------- | ---- | -------- | ------------- |
| id    | 配方ID   | 大于 0 的数字    | 1    | 是       | 合成配方的ID  |
| times | 合成次数 | 大于 0 的数字    | 5    | 否       | 默认值为 1    |

**返回数据：**

| 字段名           | 类型   | 说明         | 示例            | 备注                |
| --------------- | ------ | ------------ | --------------- | ------------------ |
| formulaId       | 数字   | 配方ID       | 1               |                    |
| result          | 整数   | 结果代码     | 0               | 见下方结果代码说明  |
| desc            | 文本   | 结果描述     | SynthesisSuccess| 见下方结果代码说明  |
| synthesisResults| 文本   | 合成结果列表 | {...}           | 仅合成成功时返回    |

**结果代码说明：**

| 代码 | 描述                 | 说明         |
| ---- | ------------------- | ------------ |
| 0    | SynthesisSuccess    | 合成成功     |
| 1    | FormulaNotFound     | 配方不存在   |
| 2    | InsufficientMaterials| 材料不足     |
| 3    | DatabaseError       | 数据库错误   |
| 4    | UnknownError        | 其他错误     |

<br/>
