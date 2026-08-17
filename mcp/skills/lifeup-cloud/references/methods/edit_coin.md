# edit_coin

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**edit_coin

**说明：**直接编辑用户的金币余额。金币数量将被设置为指定的值。变更原因可自定义，并将显示在金币历史记录中。

**示例：**

- 将金币设置为1000，原因为"API调整"：<a href="lifeup://api/edit_coin?coin=1000&content=API调整">lifeup://api/edit_coin?coin=1000&content=API调整</a>
- 静默将金币设置为500：<a href="lifeup://api/edit_coin?coin=500&silent=true">lifeup://api/edit_coin?coin=500&silent=true</a>

| 参数 | 含义 | 取值 | 示例 | 是否必须 | 备注 |
| ---- | ---- | ---- | ---- | -------- | ---- |
| coin | 目标金币数量 | 大于等于0的数字 | 1000 | 是 | 操作后的最终金币余额，最大值为999999 |
| content | 变更原因 | 任意文本 | API调整 | 否 | 如未提供则使用系统默认原因 |
| reason | 变更原因（别名） | 任意文本 | API调整 | 否 | content参数的别名 |
| silent | 禁用UI通知 | true或false | false | 否 | 默认为false，设置为true时不显示提示消息 |

<br/>
