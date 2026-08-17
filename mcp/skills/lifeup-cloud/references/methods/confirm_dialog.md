# confirm_dialog

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**confirm_dialog

**说明：**弹出一个选择弹窗，可以自定义标题、文本、积极按钮、消极按钮。点击按钮时也可以调用其他接口。

**示例：**

- [lifeup://api/confirm_dialog?title=你相信爱吗&positive_action=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D相信&negative_action=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D不相信](lifeup://api/confirm_dialog?title=你相信爱吗&positive_action=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D相信&negative_action=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D不相信)

- 其他使用场景：

  奖励二选一

  分支选择

| 参数            | 含义               | 取值            | 示例                                         | 是否必须 | 备注                                                         |
| --------------- | ------------------ | --------------- | -------------------------------------------- | -------- | ------------------------------------------------------------ |
| title           | 弹窗标题           | 任意文本        | 标题                                         | 是       | -                                                            |
| message         | 弹窗详细描述       | 任意文本        | 这是弹窗内容                                 | 否       | -                                                            |
| positive_text   | 积极按钮文案       | 任意文本        | 确定                                         | 否       | -                                                            |
| negative_text   | 消极按钮文案       | 任意文本        | 拒绝                                         | 否       | -                                                            |
| neutral_text    | 中性按钮文案       | 任意文本        | 说明                                         | 否       | -                                                            |
| positive_action | 积极按钮的链接响应 | URL（其他接口） | lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D你点了确定 | 否       | 实际上就是弹出消息接口经过转义的文本。转义规则可参考`基础知识-转义`。 |
| negative_action | 消极按钮的链接响应 | URL（其他接口） | 同上                                         | 否       | 同上                                                         |
| neutral_action  | 中性按钮的链接响应 | URL（其他接口） | 同上                                         | 否       | 同上                                                         |
| cancel_action   | 取消弹窗的链接响应 | URL（其他接口） | 同上                                         | 否       | 「取消」指的是                                               |

<br/>

#### 空接口
