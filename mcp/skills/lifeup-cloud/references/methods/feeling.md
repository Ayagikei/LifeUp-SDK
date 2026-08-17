# feeling

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**feeling

**说明：**创建或更新感想。

**示例：**

- 创建一个新的感想：[lifeup://api/feeling?content=开心&time=1633036800](lifeup://api/feeling?content=开心&time=1633036800)
- 更新特定 id 的感想，并标记为收藏状态：[lifeup://api/feeling?id=1&is_favorite=true](lifeup://api/feeling?id=1&is_favorite=true)

| 参数                     | 含义       | 取值                           | 示例           | 是否必须 | 备注                                                                                                   |
| ------------------------ | ---------- | ------------------------------ | -------------- | -------- | ------------------------------------------------------------------------------------------------------ |
| id                       | 感想记录id | 大于 0 的数字                  | 1              | 否       | 如果提供，则用于更新特定记录                                                                           |
| content                  | 内容       | 任意文本                       | 快乐           | 否       | 用于创建记录或更新记录的内容                                                                           |
| time                     | 时间戳     | Unix 时间戳                    | 1633036800     | 否       | 记录的时间，默认为当前时间                                                                             |
| is_favorite              | 是否收藏   | true 或 false                  | true           | 否       | 标记记录是否为收藏                                                                                     |
| relate_type              | 关联类型   | 数字 0-3                       | 1              | 否       | 指定记录的关联类型<br/>0：任务<br/>1：自定义成就<br/>2：无关联<br/>3：物品使用                         |
| relate_id                | 关联id     | 大于 0 的数字                  | 2              | 否       | 指定记录的关联id<br/>当 relate_type 为 0 时，代表任务 id<br/>当 relate_type 为 1 时，代表成就 id<br/>当 relate_type 为 3 时，代表物品 id<br/>当 relate_type 为 2，无需提供 |
| usage_count              | 使用次数   | 大于 1 的整数                  | 1              | 否       | 仅当 relate_type 为 3（物品使用）时有效，记录该物品的使用次数                                          |
| image_uris               | 图片 URI   | URI 字符串列表                 |                | 否       | 支持本地文件 URI (file://) 或 远程网络图片 (http/https)。支持数组（如 &image_uris=uri1&image_uris=uri2） |
| image_uris_update_mode | 更新模式   | APPEND 或者 REPLACE | REPLACE | 否       | 仅当更新现有记录且提供 image_uris 时有效<br/>APPEND：追加图片<br/>REPLACE：替换现有图片（默认）        |

**注意：**

1. 如果提供 `id` 参数，则方法会尝试更新对应的感想记录。如果没有找到相应的记录，将抛出异常。
2. 如果不提供 `id`，但提供了 `content`，则方法会创建一个新的感想记录。

<br/>
