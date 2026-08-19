# random

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** random

**Description:** A simple random interface that can trigger one of multiple APIs at random.

**Example:**

- Equally likely to randomly display `scissors`, `rock`, or `paper`: [lifeup://api/random?api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Drock&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dscissors&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dpaper](lifeup://api/random?api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Drock&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dscissors&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dpaper)

- 90% probability to display `rock`, 5% probability for `scissors`, and 5% probability for `paper`: [lifeup://api/random?api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Drock&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dscissors&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dpaper&weight=90&weight=5&weight=5](lifeup://api/random?api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Drock&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dscissors&api=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dpaper&weight=90&weight=5&weight=5)

| Parameter | Meaning    | Values                 | Example                                | Required | Notes |
| --------- | ---------- | ---------------------- | -------------------------------------- | -------- | ----- |
| api       | Random API | Any text               | lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Drock | Yes      | Supports calling in array form (i.e., multiple api parameters, as seen in the examples above) |
| weight    | Weight     | Numbers greater than 0 | 1                                      | No       | Supports calling in array form.<br/><br/>If weight is not specified, all weights are equal (equal probability).<br/>If weights are specified, they are assigned sequentially: e.g., the first weight corresponds to the first api parameter.<br/><br/>**Please ensure that the number of weight parameters matches the number of api parameters, or it may not take effect.** |

<br/>

#### Confirm Dialog
