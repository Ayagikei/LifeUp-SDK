# shop_settings

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**shop_settings

**说明：**调整各种商店设置

**示例：**

- 将ATM利率设置为0.01%：[lifeup://api/shop_settings?key=atm_interest&value=0.01](lifeup://api/shop_settings?key=atm_interest&value=0.01)
- 每次点击将利率提升0.01%：[lifeup://api/shop_settings?key=atm_interest&value=0.01&set_type=relative](lifeup://api/shop_settings?key=atm_interest&value=0.01&set_type=relative)

| 参数     | 含义                     | 取值                                                         | 示例         | 是否必须 | 备注                                                         |
| -------- | ------------------------ | ------------------------------------------------------------ | ------------ | -------- | ------------------------------------------------------------ |
| key      | 类型                     | 目前仅支持：<br/>atm_interest<br/>credit_interest<br/>line_of_credit<br/>discount_rate_for_returning<br/>atm_balance | atm_interest | 是       | atm_interest - ATM日利率<br/>credit_interest - 贷款日利率<br/>line_of_credit - 可贷款金额<br/>discount_rate_for_returning - 退货打折比例<br/>atm_balance - ATM 余额 |
| value    | 数值                     | 浮点数（小数点）                                             | 0.01         | 是       | 不同的 key 对应不同的数值范围<br/>比如 ATM 余额不支持小数点  |
| set_type | 如何设置数值             | 以下数值其一：<br/>absolute<br/>relative                     | absolute     | 否       | absolute - 绝对取值，即直接将目标设置为 value<br/>relative - 相对取值，在原数值的基础上增加或减少 |
| silent   | 是否沉默执行（不显示UI） | 布尔值                                                       | false        | 否       | 仅 v1.93.0-beta01（502）+ 支持<br/>默认为 false，即会显示 UI 提示 |

<br/>
