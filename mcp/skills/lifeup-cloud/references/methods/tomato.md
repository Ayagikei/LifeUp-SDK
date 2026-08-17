# tomato

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**tomato

**说明：**调整番茄数量（增加、减少或设置指定数量）

**示例：**

- 增加1个番茄：[lifeup://api/tomato?action=increase&number=1](lifeup://api/tomato?action=increase&number=1)
- 减少2个番茄：[lifeup://api/tomato?action=decrease&number=2](lifeup://api/tomato?action=decrease&number=2)
- 设置番茄数为10：[lifeup://api/tomato?action=set&number=10](lifeup://api/tomato?action=set&number=10)

| 参数   | 含义     | 取值                                           | 示例     | 是否必须 | 备注                                                    |
| ------ | -------- | ---------------------------------------------- | -------- | -------- | ------------------------------------------------------- |
| action | 操作类型 | 以下值之一：<br/>increase<br/>decrease<br/>set | increase | 否       | increase - 增加番茄数（默认）<br/>decrease - 减少番茄数<br/>set - 设置番茄数为指定值 |
| number | 数量     | 整数                                           | 1        | 是       | 根据 action 不同含义不同：<br/>increase/decrease - 增加/减少的数量<br/>set - 设置的目标数量 |

**返回数据：**

| 字段名   | 类型 | 说明         | 示例 |
| -------- | ---- | ------------ | ---- |
| tomatoes | 整数 | 当前番茄总数 | 10   |

<br/>
