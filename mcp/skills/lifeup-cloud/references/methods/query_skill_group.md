# query_skill_group

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**query_skill_group

**说明：**查询单个技能组，并返回原始排序值与折叠状态。

**示例：**

- 查询技能组：[lifeup://api/query_skill_group?id=10](lifeup://api/query_skill_group?id=10)

| 参数 | 含义 | 取值 | 示例 | 是否必须 | 备注 |
| ---- | ---- | ---- | ---- | -------- | ---- |
| id | 技能组ID | 大于 0 的数字 | 10 | 是 | - |

**返回值：**

| 参数 | 含义 | 取值 | 示例 | 是否必须 | 备注 |
| ---- | ---- | ---- | ---- | -------- | ---- |
| id | 技能组ID | 数字 | 10 | 是 | - |
| content | 技能组名称 | 字符串 | 战斗 | 是 | - |
| order | 原始排序值 | 数字 | 20 | 是 | `orderInCategory` |
| collapsed | 折叠状态 | 字符串 | true | 是 | 以 `true` / `false` 文本返回 |

<br/>

<br/>
