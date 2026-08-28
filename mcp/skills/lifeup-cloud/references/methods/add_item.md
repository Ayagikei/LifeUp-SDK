# add_item

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** add_item

**Description:** Create a shop item with customizable properties including purchase limits and use effects.

**Example:** [lifeup://api/add_item?name=Take a 10-minute break&desc=Go and take a short break!&price=10&action_text=rest&icon=☕](lifeup://api/add_item?name=Take a 10-minute break&desc=Go and take a short break!&price=10&action_text=rest&icon=☕)

| Parameter        | Meaning                | Values               | Example       | Required | Notes                           |
| --------------- | --------------------- | -------------------- | ------------- | -------- | ------------------------------- |
| name            | Item name             | any text             | 10 minute break | Yes    |                                 |
| desc            | Description           | any text             | Take a break  | No       |                                 |
| icon            | Icon                  | emoji, http(s) URL, or built-in sample name | ☕ | No | Stored as `emoji_*.webp`, `lifeup_sample_*`, or URL. Name emoji does not set the icon. Sample catalog: `help` `sample_icons`. |
| price           | Price                 | [0, 999999]         | 10            | No       | Default is 0                    |
| stock_number    | Stock quantity        | [-1, 99999]         | -1            | No       | -1 means unlimited              |
| action_text     | Action button text    | any text             | rest          | No       |                                 |
| disable_purchase| Disable purchase      | true or false        | false         | No       | Default is false                |
| disable_use     | Disable use           | true or false        | false         | No       | Default is false                |
| category        | Category ID           | number greater than or equal to 0 | 0 | No    | 0 for default category          |
| order           | Display order         | integer              | 1             | No       | Position in category            |
| purchase_limit  | Restriction rules     | JSON text            | `help` `item_structures` § Purchase Limit | No | Configurable purchase/use restrictions |
| limit_scope     | Restriction scope     | purchase / use / both | purchase | No | Only effective when `purchase_limit` is not empty; defaults to `purchase` |
| effects         | Use effects           | JSON text            | `help` `item_structures` § Item Effects | No | Item usage effects |
| own_number      | Initial owned quantity | integer             | 0             | No       | Set initial inventory quantity  |
| unlist          | Hide from shop        | true or false        | false         | No       | Default is false                |

**Return Data:**

| Field    | Type    | Description    | Example | Notes                    |
| -------- | ------- | -------------- | ------- | ------------------------ |
| item_id  | Number  | Item ID        | 1000    | ID of the created item   |

!> The effects parameter will override disable_use. If you set effects to indicate an unusable item, disable_use will be ignored.

?> Edit existing items: `help` `item`.
