# edit_coin

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** edit_coin

**Description:** Edit the user's coin balance directly. The current coin amount will be set to the specified value. The reason for the change can be customized and will be displayed in the coin history.

**Example:**

- Set coins to 1000 with reason "API adjustment": <a href="lifeup://api/edit_coin?coin=1000&content=API adjustment">lifeup://api/edit_coin?coin=1000&content=API adjustment</a>
- Set coins to 500 silently: <a href="lifeup://api/edit_coin?coin=500&silent=true">lifeup://api/edit_coin?coin=500&silent=true</a>

| Parameter | Meaning | Type | Example | Required | Notes |
| --------- | ------- | ---- | ------- | -------- | ----- |
| coin | Target coin amount | number >= 0 | 1000 | Yes | The final coin balance after the operation, maximum value is 999999 |
| content | Reason for change | any text | API adjustment | No | Defaults to system default reason if not provided |
| reason | Reason for change (alias) | any text | API adjustment | No | Alternative to content parameter |
| silent | Disable UI notification | true or false | false | No | Defaults to false, set to true to suppress toast message |

<br/>
