# category

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**category

**说明：**添加或编辑各类清单（任务清单、成就清单、商店清单、合成清单）

**示例：**

- 创建任务清单：[lifeup://api/category?type=tasks&name=学习清单](lifeup://api/category?type=tasks&name=学习清单)
- 编辑商店清单：[lifeup://api/category?type=shop&edit_id=1&name=装备商店&order=1](lifeup://api/category?type=shop&edit_id=1&name=装备商店&order=1)

| 参数             | 含义           | 取值                | 示例       | 是否必须 | 备注                           |
| --------------- | -------------- | ------------------ | ---------- | -------- | ------------------------------ |
| type            | 清单类型       | 以下数值其一：<br/>tasks<br/>achievements<br/>shop<br/>synthesis | tasks | 是 | tasks - 任务清单<br/>achievements - 成就清单<br/>shop - 商店清单<br/>synthesis - 合成清单 |
| edit_id         | 编辑的清单ID   | 大于 0 的数字      | 1          | 否       | 编辑时必须提供                 |
| name            | 清单名称       | 任意文本           | 学习清单    | 否       | 新建时必须提供；编辑时可选      |
| order           | 排序           | 整数               | 1          | 否       | 清单在列表中的排序位置          |
| hidden          | 是否隐藏       | true 或者 false    | false      | 否       | 仅任务清单和商店清单支持        |
| inventory_hidden| 是否在仓库隐藏 | true 或者 false    | false      | 否       | 仅商店清单支持                 |
| icon_uri        | 图标URI        | URI文本            | content://... | 否    | 仅成就清单支持                 |
| desc            | 描述           | 任意文本           | 这是描述     | 否      | 仅成就清单支持                 |
| color           | 标签颜色       | 颜色字符串         | #66CCFF     | 否      | 仅任务清单支持；#需要转义为%23  |

**返回数据：**

| 字段名 | 类型   | 说明     | 示例 | 备注             |
| ------ | ------ | -------- | ---- | ---------------- |
| id     | 数字   | 清单ID   | 1000 | 新建或编辑的清单ID |

<br/>
