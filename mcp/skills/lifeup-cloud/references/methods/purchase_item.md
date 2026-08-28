# purchase_item

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** purchase_item

**Description:** Purchase a specific item

**Examples:**

- Purchase item ID 1: [lifeup://api/purchase_item?id=1](lifeup://api/purchase_item?id=1)
- Purchase item named "Health Potion": [lifeup://api/purchase_item?name=Health%20Potion](lifeup://api/purchase_item?name=Health%20Potion)
- Purchase 5 copies of item ID 1: [lifeup://api/purchase_item?id=1&purchase_quantity=5](lifeup://api/purchase_item?id=1&purchase_quantity=5)

If the item has `purchase_limit` configured and `limit_scope` includes `purchase`, this API will also enforce those restrictions.

| Parameter         | Meaning          | Values                | Example       | Required | Notes                      |
| ----------------- | ---------------- | --------------------- | ------------- | -------- | -------------------------- |
| id                | Item ID          | number greater than 0 | 1             | No*      | One of id or name required |
| name              | Item name        | any text              | Health Potion | No*      | One of id or name required |
| purchase_quantity | Purchase quantity| number greater than 0 | 5             | No       | Defaults to 1              |

**Response:**

| Field  | Type    | Description        | Example         | Notes                       |
| ------ | ------- | ------------------ | --------------- | --------------------------- |
| itemId | Number  | Item ID            | 1               | Returned on successful buy  |
| result | Integer | Result code        | 0               | See result codes below      |
| desc   | Text    | Result description | PurchaseSuccess | See result codes below      |

**Result Codes:**

| Code | Description               | Notes                         |
| ---- | ------------------------- | ----------------------------- |
| 0    | PurchaseSuccess           | Purchase succeeded            |
| 1    | DatabaseError             | Database error                |
| 2    | NotEnoughCoin             | Not enough coins              |
| 3    | ItemNotFound              | Item not found                |
| 4    | PurchaseAndUseSuccess     | Purchase and use succeeded    |
| 5    | PurchaseSuccessAndUseFailure | Purchase succeeded but use failed |
| 6    | NotPurchaseable           | Purchase was blocked by item settings or restrictions |
| 7    | OutOfStock                | Shop stock is not enough      |

<br/>
