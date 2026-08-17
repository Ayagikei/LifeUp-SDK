# achievement

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**achievement

**说明：**添加或编辑自定义成就和成就子分类

**示例：**

- 创建一个成就：[lifeup://api/achievement?name=收藏家&desc=收集100个物品&category_id=1](lifeup://api/achievement?name=收藏家&desc=收集100个物品&category_id=1)
  - 你可能需要将`category_id`替换成你实际可用的成就清单 id 以测试该示例
- 创建需要解锁条件的成就：[lifeup://api/achievement?name=百万富翁&conditions_json=%5B%7B%22type%22%3A7%2C%22target%22%3A1000000%7D%5D&category_id=1](lifeup://api/achievement?name=百万富翁&conditions_json=%5B%7B%22type%22%3A7%2C%22target%22%3A1000000%7D%5D&category_id=1)
  - 你可能需要将`category_id`替换成你实际可用的成就清单 id 以测试该示例
  - `conditions_json`编码前内容为`[{"type":7,"target":1000000}]`
- 编辑现有成就：[lifeup://api/achievement?edit_id=1&name=新成就名称&exp=100](lifeup://api/achievement?edit_id=1&name=新成就名称&exp=100)

#### 1. 成就参数

| 参数          | 含义           | 取值                | 示例     | 是否必须 | 备注                           |
| ------------ | -------------- | ------------------ | -------- | -------- | ------------------------------ |
| edit_id      | 编辑的成就ID   | 大于 0 的数字      | 1        | 否       | 编辑时必须提供                 |
| is_subcategory| 是否为子分类   | true 或者 false    | false    | 否       | 默认为 false                   |
| name         | 成就名称       | 任意文本           | 收藏家    | 否*      | 新建时必须提供                 |
| desc         | 成就描述       | 任意文本           | 收集100个物品 | 否    |                               |
| order        | 排序           | 整数              | 1         | 否       | 在列表中的排序位置              |
| category_id  | 所属分类ID     | 大于 0 的数字      | 1         | 否*      | 创建子分类时必须提供            |
| unlocked     | 是否解锁       | true 或者 false    | true      | 否       | true-立即解锁<br/>false-重置为未解锁 |
| unlock_time  | 解锁时间       | 时间戳（毫秒）     | 1640995200000 | 否   | 仅当成就已解锁时有效           |
| delete       | 是否删除       | true 或者 false    | false     | 否       |                                |
| secret       | 是否为隐藏成就 | true 或者 false    | false     | 否       |                                |
| write_feeling| 是否记录感想   | true 或者 false    | false     | 否       |                                |
| color        | 标题颜色       | 颜色字符串         | #66CCFF   | 否       | #需要转义为%23                  |
| auto_use_item| 自动使用物品   | true 或者 false    | false     | 否       |                                |
| skills       | 技能ID         | 大于 0 的数字数组   | 1         | 否       | 支持数组（如 &skills=1&skills=2）|
| exp          | 经验值奖励     | 整数              | 100       | 否       |                                |
| item_id      | 物品ID         | 大于 0 的数字      | 1         | 否*      | 与 item_name 必须提供其中一个   |
| item_name    | 物品名称       | 任意文本           | 宝箱      | 否*      | 与 item_id 必须提供其中一个     |
| item_amount  | 物品数量       | [1, 99]           | 1         | 否       | 默认为 1                       |
| items        | 物品奖励JSON   | JSON文本           | [{"item_id":1,"amount":2}] | 否 | 可一次设置多个物品奖励，格式见下文 |
| conditions_json | 解锁条件JSON | JSON文本          | [{"type":7,"target":1000000}] | 否 | 设置解锁条件，格式见下文 |
| coin         | 金币奖励       | [0, 999999]      | 10         | 否       | 解锁成就时获得的金币数量        |
| coin_var     | 金币奖励浮动值 | 整数              | 5          | 否       | 金币奖励的浮动范围             |
| coin_set_type| 金币奖励类型   | 以下数值其一：<br/>absolute<br/>relative | absolute | 否 | absolute - 直接设置金币为 value<br/>relative - 在原金币值的基础上增加或减少 |
| exp_set_type | 经验值奖励类型 | 以下数值其一：<br/>absolute<br/>relative | absolute | 否 | absolute - 直接设置经验值为 value<br/>relative - 在原经验值的基础上增加或减少 |

**返回数据：**

| 字段名 | 类型   | 说明     | 示例 | 备注             |
| ------ | ------ | -------- | ---- | ---------------- |
| id     | 数字   | 成就ID   | 1000 | 新建或编辑的成就ID |

#### 2. 子分类专用参数

| 参数          | 含义           | 取值                | 示例     | 是否必须 | 备注                           |
| ------------ | -------------- | ------------------ | -------- | -------- | ------------------------------ |
| is_collapsed | 是否折叠       | true 或者 false    | false    | 否       | 仅适用于子分类                  |

**返回数据：**

| 字段名 | 类型   | 说明     | 示例 | 备注             |
| ------ | ------ | -------- | ---- | ---------------- |
| id     | 数字   | 成就ID   | 1000 | 新建或编辑的成就ID (子分类) |

#### 3. 解锁条件类型说明

| 类型代码 | 含义               | 是否需要related_id | related_id类型 | target说明        |
| ------- | ------------------ | ---------------- | -------------- | ----------------- |
| 0       | 完成任务次数        | 是              | 任务ID         | 完成次数           |
| 1       | 连续完成任务次数     | 是              | 任务ID         | 连续次数           |
| 3       | 番茄数量           | 否              | -              | 番茄数量           |
| 4       | 使用人升天数        | 否              | -              | 使用天数           |
| 5       | 被点赞数           | 否              | -              | 点赞数量           |
| 6       | 连续完成天数        | 否              | -              | 连续天数           |
| 7       | 当前金币数         | 否              | -              | 金币数量           |
| 8       | 一天内金币获得数     | 否              | -              | 金币数量           |
| 9       | 任务的番茄数        | 是              | 任务ID         | 番茄数量           |
| 10      | 物品购买数         | 是              | 物品ID         | 购买次数           |
| 11      | 物品使用数         | 是              | 物品ID         | 使用次数           |
| 12      | 物品开箱获得数      | 是              | 物品ID         | 获得次数           |
| 13      | 技能达到指定等级     | 是              | 技能ID         | 等级数值           |
| 14      | 人生等级           | 否              | -              | 等级数值           |
| 15      | 物品累计获得数      | 是              | 物品ID         | 获得总次数         |
| 16      | 物品合成获得数      | 是              | 物品ID         | 合成获得次数        |
| 17      | 物品当前拥有数量     | 是              | 物品ID         | 拥有数量           |
| 18      | 任务番茄钟专注时长   | 是              | 任务ID         | 专注时长(分钟)      |
| 19      | ATM存款           | 否              | -              | 存款数量           |
| 20      | 外部API           | 否              | -              | 根据API定义        |
| 520     | 每日完成 N 个不同任务 | 否            | -              | 不同任务数（按 groupId 去重；已有类型） |
| 524     | 每日累计完成 N 次任务 | 否            | -              | 当日有效完成记录总次数（v1.104.4+） |

> 自 v1.104.4 起，类型 `520` 与 `524` 的语义如下：
>
> - 两者共享相同的完成口径与本地自然日边界（`TimeRange.today()`）。
> - 普通任务统计 `COMPLETED`，负面任务统计 `GIVE_UP`。
> - 类型 `520` 按有效 `groupId` 去重（缺失 groupId 时回退到任务记录 id）。同一无限任务当天多次完成只计 1 个不同任务。
> - 类型 `524` 按有效完成记录逐行计数。同一无限任务当天完成 5 次则 `completionCount = 5`。
> - 已有 `type=520` 的成就保持不同任务语义，无需迁移。

#### 4. JSON格式说明

##### 解锁条件 (conditions_json)

```json
[
   {
       "type": 7,           // 条件类型（参考上表）
       "related_id": null,  // 关联ID（部分类型必须提供）
       "target": 1000000    // 目标数值
   },
   {
       "type": 10,          // 示例：购买指定物品
       "related_id": 1,     // 物品ID
       "target": 5          // 购买5次
   },
   {
       "type": 520,         // 每日完成 N 个不同任务
       "related_id": null,
       "target": 5
   },
   {
       "type": 524,         // 每日累计完成 N 次任务
       "related_id": null,
       "target": 10
   }
]
```

##### 物品奖励 (items)

```
[
    {
        "item_id": 1,    // 物品ID
        "amount": 2      // 数量
    },
    {
        "item_id": 2,
        "amount": 3
    }
]
```

<br/>
