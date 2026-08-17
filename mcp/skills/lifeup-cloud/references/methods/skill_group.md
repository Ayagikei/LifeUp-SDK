# skill_group

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**skill_group

**说明：**创建、编辑、删除技能组，也支持一次性提交技能组与技能的混排排序结果。

**示例：**

- 创建技能组：[lifeup://api/skill_group?content=战斗](lifeup://api/skill_group?content=战斗)
- 编辑技能组：[lifeup://api/skill_group?id=10&content=战斗&order=20&collapsed=true](lifeup://api/skill_group?id=10&content=战斗&order=20&collapsed=true)
- 删除技能组：[lifeup://api/skill_group?id=10&delete=true](lifeup://api/skill_group?id=10&delete=true)
- 混排排序：

```text
lifeup://api/skill_group?sort_json=[{"type":"skill","id":2},{"type":"group","id":10},{"type":"skill","id":3}]
```

| 参数 | 含义 | 取值 | 示例 | 是否必须 | 备注 |
| ---- | ---- | ---- | ---- | -------- | ---- |
| id | 技能组ID | 大于 0 的数字 | 10 | 否* | 编辑或删除时必须提供 |
| content | 技能组名称 | 任意文本 | 战斗 | 否* | 新建时必须提供 |
| order | 排序值 | 整数 | 20 | 否 | 原始 `orderInCategory`；在技能与技能组的混排列表中必须唯一 |
| collapsed | 折叠状态 | true 或 false | true | 否 | 是否折叠该技能组 |
| delete | 是否删除 | true 或 false | false | 否 | 仅编辑模式下有效 |
| sort_json | 混排排序节点 | JSON 数组 | `[{"type":"skill","id":2},{"type":"group","id":10}]` | 否* | 提供后会忽略 CRUD 参数，直接按该排序计划回写。支持部分排序：未传入的节点会保持原相对顺序 |

`sort_json` 节点格式：

| 字段 | 含义 | 取值 |
| ---- | ---- | ---- |
| type | 节点类型 | `skill` / `group` |
| id | 实体 ID | 大于 0 的数字 |

**返回数据：**

| 字段名 | 类型 | 说明 | 示例 | 备注 |
| ------ | ---- | ---- | ---- | ---- |
| id | 数字 | 技能组ID | 10 | 创建 / 编辑 / 删除时返回 |
| count | 数字 | 参与排序的节点数量 | 3 | 使用 `sort_json` 时返回 |

<br/>
