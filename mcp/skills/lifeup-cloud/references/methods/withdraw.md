# withdraw

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** withdraw

**Description:** Withdrawals will be checked for legality (whether the ATM balance is sufficient).

**Example:** [lifeup://api/withdraw?amount=500](lifeup://api/withdraw?amount=500)

**Explanation:** Withdraw 500 coins.

| Parameter | Meaning           | Type                    | Example | Required | Notes |
| --------- | ----------------- | ----------------------- | ------- | -------- | ----- |
| amount    | withdrawal amount | a number greater than 0 | 100     | yes      | -     |

**Return:**

| Parameter | Meaning                              | Type              | Example | Required | Notes |
| --------- | ------------------------------------ | ----------------- | ------- | -------- | ----- |
| result    | Whether the operation was successful | `true` or `false` | true    | yes      | -     |

<br/>
