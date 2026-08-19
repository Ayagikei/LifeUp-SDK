# deposit

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** deposit

**Description:** The deposit will be checked for legality (whether the coin balance is sufficient).

**Example:**[lifeup://api/deposit?amount=500](lifeup://api/deposit?amount=500)

**Explanation:** Deposit 500 coins.

| Parameter | Meaning        | Type                    | Example | Required | Notes |
| --------- | -------------- | ----------------------- | ------- | -------- | ----- |
| amount    | deposit amount | a number greater than 0 | 100     | yes      | -     |

**Return:**

| Parameter | Meaning                              | Type              | Example | Required | Notes |
| --------- | ------------------------------------ | ----------------- | ------- | -------- | ----- |
| result    | Whether the operation was successful | `true` or `false` | true    | yes      | -     |

<br/>

#### Withdraw
