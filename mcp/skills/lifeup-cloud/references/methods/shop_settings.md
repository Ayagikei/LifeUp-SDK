# shop_settings

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** shop_settings

**Instructions:** Adjust various store settings

**Example:**

- Set ATM interest rate to 0.01%: [lifeup://api/shop_settings?key=atm_interest&value=0.01](lifeup://api/shop_settings?key=atm_interest&value=0.01)
- Increase interest rate by 0.01% per click: [lifeup://api/shop_settings?key=atm_interest&value=0.01&set_type=relative](lifeup://api/shop_settings?key=atm_interest&value=0.01&set_type=relative)

| Parameter | Meaning              | Type | Example | Required | Notes |
| --------- | -------------------- | ---- | ------- | -------- | ----- |
| key       | type                 | Currently only following values  supported: <br/>atm_interest<br/>credit_interest<br/>line_of_credit<br/>discount_rate_for_returning<br/>atm_balance | atm_interest | yes | atm_interest - ATM daily rate<br/>credit_interest - loan daily rate<br/>line_of_credit - loanable amount<br/>discount_rate_for_returning - return discount Scale<br/>atm_balance - Set ATM balance |
| value     | numeric value        | decimal number or integer | 0.01 | yes | different keys correspond to different value ranges<br/>For example, ATM balances do not support decimal points |
| set_type  | How to set the value | One of the following values:<br/>absolute<br/>relative | absolute | no |absolute - absolute value, that is, directly set the target to value<br/>relative - relative values, adding or subtracting from the original value|
| silent    | Whether to execute silently (without displaying UI) | Boolean | false | No | Supported from v1.93.0-beta01 (502) and later<br/>Default is false, which means it will display UI prompts |

<br/>
