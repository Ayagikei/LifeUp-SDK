# tomato

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** tomato

**Description:** Adjust the number of tomatoes (increase, decrease, or set to a specific amount)

**Examples:**

- Add 1 tomato: [lifeup://api/tomato?action=increase&number=1](lifeup://api/tomato?action=increase&number=1)
- Remove 2 tomatoes: [lifeup://api/tomato?action=decrease&number=2](lifeup://api/tomato?action=decrease&number=2)
- Set pomodoro count to 10: [lifeup://api/tomato?action=set&number=10](lifeup://api/tomato?action=set&number=10)

| Parameter | Meaning        | Values                                        | Example   | Required | Notes                                                         |
| --------- | -------------- | --------------------------------------------- | --------- | -------- | ------------------------------------------------------------- |
| action    | Operation type | One of:<br/>increase<br/>decrease<br/>set     | increase  | No       | increase - Add pomodoros (default)<br/>decrease - Remove pomodoros<br/>set - Set pomodoro count to specified value |
| number    | Amount         | Integer                                       | 1         | Yes      | Different meanings based on action:<br/>increase/decrease - Amount to add/remove<br/>set - Target amount to set |

**Response:**

| Field    | Type    | Description              | Example |
| -------- | ------- | ------------------------ | ------- |
| tomatoes | Integer | Current pomodoro count   | 10      |

<br/>
