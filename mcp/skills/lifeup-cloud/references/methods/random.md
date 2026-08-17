# random

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**random

**说明：**简单的随机接口，可以随机触发多个 API 中的其中一个。

**示例：**

- 同等概率随机显示`石头`、`剪刀`、`布`：[lifeup://api/random?api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D石头&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D剪刀&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D布](lifeup://api/random?api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D石头&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D剪刀&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D布)

- 90%概率显示`石头`、5%概率`剪刀`、5%概率`布`，：[lifeup://api/random?api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D石头&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D剪刀&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D布&weight=90&weight=5&weight=5](lifeup://api/random?api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D石头&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D剪刀&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D布&weight=90&weight=5&weight=5)

| 参数   | 含义       | 取值        | 示例                                   | 是否必须 | 备注                                                         |
| ------ | ---------- | ----------- | -------------------------------------- | -------- | ------------------------------------------------------------ |
| api    | 随机的 API | 任意文本    | lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D石头 | 是       | 支持数组形式调用（即可以存在多个api参数，见上述例子）        |
| weight | 比重       | 大于0的数字 | 1                                      | 否       | 支持数组形式调用<br/><br/>如果不指定比重，默认比重都一样（概率同等均等）。<br/>如果指定比重，会按顺序分配：如第一个weight分配到第一个api参数。<br/><br/>**请确保weight参数的数量与 api 参数的数量一致，否则可能会不生效。** |

<br/>

#### 弹窗
