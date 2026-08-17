# withdraw

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**withdraw

**说明：**取款，会进行合法性校验（ATM 余额是否充足）。

**示例：**[lifeup://api/withdraw?amount=500](lifeup://api/withdraw?amount=500)

**解释：**取款 500 金币。

| 参数   | 含义     | 取值          | 示例 | 是否必须 | 备注 |
| ------ | -------- | ------------- | ---- | -------- | ---- |
| amount | 取款数量 | 大于 0 的数字 | 100  | 是       | -    |

**返回值：**

| 参数   | 含义         | 取值   | 示例 | 是否必须 | 备注 |
| ------ | ------------ | ------ | ---- | -------- | ---- |
| result | 操作是否成功 | 布尔值 | true | 是       | -    |

<br/>
