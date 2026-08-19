# item

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** item

**Description:** Modify existing items, including price, stock, effects, and other properties

**Examples:**

- Adjust price: [lifeup://api/item?id=1&set_price=1&set_price_type=relative](lifeup://api/item?id=1&set_price=1&set_price_type=relative)
- Modify effects: [lifeup://api/item?effects=%5B%7B%22type%22%3A2%2C%22info%22%3A%7B%22min%22%3A100%2C%22max%22%3A200%7D%7D%5D&id=1](lifeup://api/item?effects=%5B%7B%22type%22%3A2%2C%22info%22%3A%7B%22min%22%3A100%2C%22max%22%3A200%7D%7D%5D&id=1)
  - The decoded content of effects parameter is: `[{"type":2,"info":{"min":100,"max":200}}]`

| Parameter         | Meaning             | Values               | Example   | Required | Notes                           |
| ---------------- | ------------------- | -------------------- | --------- | -------- | ------------------------------- |
| id               | Item ID             | number greater than 0| 1         | No*      | Either id or name required      |
| name             | Item name           | any text             | Treasure  | No*      | For fuzzy search, not renaming  |
| set_name         | Set name            | any text             | Treasure  | No       | Cannot be empty                 |
| set_desc         | Set description     | any text             | Get gift  | No       |                                |
| set_icon         | Set icon            | URL text             | http://...| No       | Must be a web URL               |
| set_price        | Adjust price        | integer              | 1         | No       |                                |
| set_price_type   | Price adjust method | absolute or relative | relative  | No       | absolute-set directly<br/>relative-add/subtract |
| own_number       | Adjust owned quantity| integer             | 1         | No       | Supports negative with relative |
| own_number_type  | Own number adjustment| absolute or relative| relative  | No       | absolute-set directly<br/>relative-add/subtract |
| stock_number     | Adjust stock        | [-1, 99999]         | 1         | No       | -1 means unlimited stock        |
| stock_number_type| Stock adjust method | absolute or relative | relative  | No       | absolute-set directly<br/>relative-add/subtract |
| disable_purchase | Disable purchase    | true or false        | false     | No       | Defaults to false              |
| disable_use      | Disable use         | true or false        | false     | No       | Defaults to false              |
| action_text      | Use button text     | any text             | Use       | No       |                                |
| title_color_string| Title color        | color string         | #66CCFF   | No       | # must be escaped as %23<br/>Empty value restores default |
| effects          | Use effects         | JSON text            | See [Item Effects Structure](#4-item-effects-structure) | No | Set item usage effects |
| purchase_limit   | Restriction rules   | JSON text            | See [Purchase Limit Structure](#3-purchase-limit-structure) | No | Pass `null` to clear all restrictions |
| limit_scope      | Restriction scope   | purchase / use / both | purchase | No | Only updates when this field is provided; cleared automatically when `purchase_limit` becomes empty |
| category_id      | Category ID         | number >= 0          | 1         | No       | 0 for default category         |
| order            | Display order       | integer              | 1         | No       | Position in category           |
| unlist           | Remove from shop    | true or false        | false     | No       | Defaults to false              |

!> Either id or name parameter must be provided to locate the item to modify

<br/>

#### Adjust the Loot Box effect
