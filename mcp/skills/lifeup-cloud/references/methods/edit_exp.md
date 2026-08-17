# edit_exp

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**edit_exp

**说明：**该 API 能批量设置属性的当前经验值，能直接设置某个经验值或者某个等级。

**示例：**

> 该 API 会影响数据，为避免误触，此处不提供点击跳转。

- 将【力量】、【学识】属性的经验值重置为0：[lifeup://api/edit_exp?skills=1&skills=2&exp=0](lifeup://api/edit_exp?skills=1&skills=2&exp=0)
- 将【魅力】经验值直接调整为50级：[lifeup://api/edit_exp?skills=3&level=50](lifeup://api/edit_exp?skills=3&level=50)

| 参数   | 含义           | 取值                         | 示例 | 是否必须                               | 备注                                                         |
| ------ | -------------- | ---------------------------- | ---- | -------------------------------------- | ------------------------------------------------------------ |
| skills | 属性（技能）id | 大于 0 的数字数组            | 1    | 否                                     | 支持数组（即&skills=1&skills=2&skills=3）<br/>获取方式请查看上文 「基础知识 - 人升数据 ID」 |
| exp    | 设置的经验值   | 大于或等于 0 的数字（int32） | 9999 | 否<br/>但 exp和 level 必须提供其中一个 |                                                              |
| level  | 设置的等级     | 大于或等于 0 的数字（int32） | 50   | 否<br/>但 exp和 level 必须提供其中一个 | 代表某个等级起始的经验值<br/>并且会受自定义等级梯度的影响    |

<br/>
