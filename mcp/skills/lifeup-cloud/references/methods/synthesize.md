# synthesize

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** synthesize

**Description:** Synthesize items using an existing formula

**Examples:**

- Synthesize once using formula ID 1: [lifeup://api/synthesize?id=1](lifeup://api/synthesize?id=1)
- Synthesize 5 times using formula ID 1: [lifeup://api/synthesize?id=1&times=5](lifeup://api/synthesize?id=1&times=5)

**Broadcast behavior:**

- This API is for **recipe synthesis**.
- When `Broadcast events` is enabled and the synthesis succeeds, LifeUp also sends the broadcast event `app.lifeup.synthesis.complete`.
- This event is **not** sent for simple synthesis inside `use_item`; that path still belongs to `app.lifeup.item.use`.

| Parameter | Meaning            | Values                | Example | Required | Notes                    |
| --------- | ----------------- | -------------------- | ------- | -------- | ------------------------ |
| id        | Formula ID        | number greater than 0 | 1       | Yes      | ID of synthesis formula  |
| times     | Number of times   | number greater than 0 | 5       | No       | Defaults to 1           |

**Response:**

| Field           | Type    | Description     | Example          | Notes                    |
| -------------- | ------- | --------------- | ---------------- | ------------------------ |
| formulaId      | Number  | Formula ID      | 1                |                          |
| result         | Integer | Result code     | 0                | See result codes below   |
| desc           | Text    | Result description | SynthesisSuccess | See result codes below |
| synthesisResults| Text   | Synthesis results | {...}           | Only returned on success |

**Result Codes:**

| Code | Description          | Notes                 |
| ---- | ------------------- | --------------------- |
| 0    | SynthesisSuccess    | Synthesis successful  |
| 1    | FormulaNotFound     | Formula not found     |
| 2    | InsufficientMaterials| Not enough materials |
| 3    | DatabaseError       | Database error        |
| 4    | UnknownError        | Other errors         |

<br/>
