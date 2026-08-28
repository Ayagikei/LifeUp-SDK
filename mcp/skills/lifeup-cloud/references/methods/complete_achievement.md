# complete_achievement

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** complete_achievement

**Description:** Complete a manual achievement and claim its reward, or claim the reward of an already unlocked automatic achievement. Same behavior as tapping the complete checkbox / claim-reward button in the app.

**Example:**

- Complete or claim the achievement with id 1: [lifeup://api/complete_achievement?id=1](lifeup://api/complete_achievement?id=1)

| Parameter | Meaning        | Type                  | Example | Required | Notes |
| --------- | -------------- | --------------------- | ------- | -------- | ----- |
| id        | achievement id | number greater than 0 | 1       | yes      |       |

**Return value:**

| Field  | Type   | Description | Example | Notes |
| ------ | ------ | ----------- | ------- | ----- |
| id     | number | achievement ID | 1 | |
| status | number | status after the call | 2 | `0` locked · `1` unlocked, reward unclaimed · `2` unlocked, reward claimed |

**Notes:**

1. Manual achievements (no unlock conditions): if still locked, this completes the achievement and grants rewards.
2. Automatic achievements (with unlock conditions): only claims rewards when already unlocked and a reward is still pending. If conditions are not met, the call fails with `error_code=achievement_not_unlocked`.
3. Calling again after the reward is already claimed succeeds with `status=2` and does not grant rewards twice.
4. This is different from `achievement?unlocked=true`, which only writes unlock state and does not grant rewards.

<br/>
