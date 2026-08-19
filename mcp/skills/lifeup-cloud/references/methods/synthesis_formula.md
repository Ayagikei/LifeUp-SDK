# synthesis_formula

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** synthesis_formula

**Description:** Create, modify, or delete synthesis formulas

**Examples:**

- Create a new formula: [lifeup://api/synthesis_formula?inputItems=%5B%7B%22item_id%22%3A%20296%2C%20%22amount%22%3A%2088%7D%5D&outputItems=%5B%7B%22item_id%22%3A%20295%2C%20%22amount%22%3A%201%7D%5D](lifeup://api/synthesis_formula?inputItems=%5B%7B%22item_id%22%3A%20296%2C%20%22amount%22%3A%2088%7D%5D&outputItems=%5B%7B%22item_id%22%3A%20295%2C%20%22amount%22%3A%201%7D%5D)
  - Here, the inputItems are `[{"item_id": 296, "amount": 88}]`
  - Here, the outputItems are `[{"item_id": 295, "amount": 1}]`
- Delete formula: [lifeup://api/synthesis_formula?id=1&delete=true](lifeup://api/synthesis_formula?id=1&delete=true)

| Parameter   | Meaning        | Values                | Example                        | Required | Notes                          |
| ----------- | -------------- | -------------------- | ------------------------------ | -------- | ------------------------------ |
| id          | Formula ID     | number greater than 0 | 1                             | No       | Required for modify or delete  |
| delete      | Delete flag    | true or false        | true                          | No       | Used only for deletion         |
| inputItems  | Input items    | Item array, see below | [{"item_id":1,"amount":2}]     | Yes      | Required for create or modify  |
| outputItems | Output items   | Item array, see below | [{"item_id":3,"amount":1}]     | Yes      | Required for create or modify  |
| category    | Category ID    | number greater than 0 | 1                             | No       | Defaults to common category    |

!> inputItems and outputItems are JSON arrays where each item contains item_id and amount fields. All item IDs must exist and amounts must be greater than 0

**Response:**

| Field     | Type    | Description      | Example     | Notes                    |
| --------- | ------- | ---------------- | ----------- | ------------------------ |
| formulaId | Number  | Formula ID       | 1           | Returned on success      |
| result    | Integer | Result code      | 0           | See result codes below   |
| desc      | Text    | Result description | AddSuccess | See result codes below   |

**Result Codes:**

| Code | Description     | Notes             |
| ---- | -------------- | ----------------- |
| 0    | Success        | Operation success |
| 1    | Failed         | Operation failed  |

<br/>
