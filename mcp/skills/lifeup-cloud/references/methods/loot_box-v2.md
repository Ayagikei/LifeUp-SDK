# loot_box/v2

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**loot_box/v2

**说明：**loot_box API 的改进版本，修改指定箱子的开箱效果，支持调整概率、奖励数、增加内容物和**删除内容物**。

**相比 v1 的改进：**
- **`sub_amount` 精准匹配**：当开箱中存在同一物品的多个不同数量条目时（如 A x1 50%、A x2 30%），用 `sub_amount` 精准定位指定条目。默认值为 `1`。匹配不到时按 `sub_id` / `sub_name` 查找商品并新增条目；如果本次请求是 `amount=0` 删除语义，则不会新增。
- **`set_type` 独立控制**：`amount_set_type` 和 `probability_set_type` 可独立设置，全局 `set_type` 作为默认回退值。
- **支持删除条目**：`amount=0`（absolute 模式）或计算后 `<=0`（relative 模式）时删除匹配到的条目。
- **重复条目合并**：如果调整 `amount` 后与同一箱子中已有的相同物品、相同数量条目重复，会合并到已有条目，并继续应用本次传入的 `probability` / `fixed`。

**示例：**[lifeup://api/loot_box/v2?name=金币箱&sub_name=【大】袋金币&sub_amount=2&probability_set_type=relative&probability=10](lifeup://api/loot_box/v2?name=金币箱&sub_name=【大】袋金币&sub_amount=2&probability_set_type=relative&probability=10)

**解释：**增加金币箱中的【大】袋金币（x2）的比重 10 点。

| 参数                 | 含义                  | 取值                                     | 示例         | 是否必须 | 备注                                                         |
| -------------------- | --------------------- | ---------------------------------------- | ------------ | -------- | ------------------------------------------------------------ |
| id                   | 商品id                | 大于0的数字                              | 1            | 否*      | 获取方式请查看上文 「基础知识 - 人升数据 ID」                |
| name                 | 商品名称              | 任意文本                                 | 金币箱       | 否*      | 用于未知 id 时，模糊搜索商品，并非修改名称                   |
| sub_id               | 箱子内容物的 id       | 大于0的数字                              | 1            | 否*      | 箱子内容物的 id。与 sub_name 同时提供时，优先使用 sub_id 匹配 |
| sub_name             | 箱子内容物的名称      | 任意文本                                 | 【大】袋金币 | 否*      | 用于箱子内容物未知 id 时，模糊搜索商品                       |
| sub_amount           | 箱子内容物数量（匹配） | 数字                                    | 2            | 否       | 精准匹配 amount 等于该值的条目，最小值为 `1`，默认 `1`。匹配不到且不是删除语义时新增条目。 |
| set_type             | 全局调整方式          | `absolute` / `relative`                  | relative     | 否       | 作为 amount_set_type 和 probability_set_type 的默认值        |
| amount_set_type      | 奖励数调整方式        | `absolute` / `relative`                  | relative     | 否       | 覆盖 set_type 对 amount 字段的设置方式                       |
| probability_set_type | 奖励比重调整方式      | `absolute` / `relative`                  | absolute     | 否       | 覆盖 set_type 对 probability 字段的设置方式                  |
| amount               | 奖励数                | 数字                                     | 1            | 否       | 某个单一物品的奖励个数。`0`（absolute）或计算后 `<=0`（relative）时删除条目 |
| probability          | 奖励比重              | 数字                                     | 1            | 否       | -                                                            |
| fixed                | 是否是固定奖励        | 布尔值                                   | true/false   | 否       | -                                                            |

**注意：**

1. 为了搜索到商品，必须提供 id 或 name 其一。
1. 为了搜索到内容物，必须提供 sub_id 或 sub_name 其一。
1. 如果同时提供 `sub_id` 和 `sub_name`，`sub_id` 优先；只有未提供有效 `sub_id` 时才会使用 `sub_name`。
1. `name` 和 `sub_name` 会先完整匹配，匹配不到再模糊匹配。
1. `sub_amount` 默认值为 `1`。当开箱中存在同一物品的多个不同数量条目时，可提供 `sub_amount` 来指定要编辑的条目。匹配不到且不是删除语义时，会新增一条 `amount=sub_amount` 的内容物。
1. 删除条目：设置 `amount=0`（absolute 模式）或使用 relative 模式使计算结果 `<=0`。删除仅对匹配到的已有条目生效；如果没有匹配到已有条目，`amount=0` 不会新增内容物。
1. 如果把某个内容物的 `amount` 调整为同一箱子内已有的相同物品、相同数量条目，会合并到已有条目，并用本次请求继续覆盖 `probability` / `fixed`。
1. 当删除后开箱为空时，整个开箱效果会被软删除（商品本身保留，后续仍可重新添加开箱条目）。

<br/>

#### 使用商品

?> 该 API 于 v1.93.0-beta01（502）版本更新引入。
