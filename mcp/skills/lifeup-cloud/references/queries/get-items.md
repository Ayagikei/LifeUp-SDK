# Items and categories

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-items

Help topic: `query/get-items`

## Items and categories :id=get-items

`GET /items` returns `ShopItem[]`. Optional repeated query parameter `id` selects item IDs: `/items?id=1&id=2`. Non-integer values are discarded. Missing IDs are skipped; valid IDs retain request order, including duplicates. If every supplied ID is invalid, the SDK sends no ID filter and the result is the normal item list. There is no pagination.

`GET /items/{listId}` returns `ShopItem[]` in a **category**, not a single item. `listId` is a signed 64-bit category ID; `-1` means all. An invalid path value falls back to all. The normal list excludes unlisted/deleted items (`isDel=0`) and follows the configured shop/category sorting. ID selection can retrieve an item independently of that normal list filter.

```json
{"code":200,"message":"success","data":[{"id":1,"name":"Coffee","desc":"A small reward","icon":"","categoryId":0,"stockNumber":-1,"ownNumber":2,"price":10,"order":0,"disablePurchase":false,"maxPurchaseNumber":5,"titleColorString":"","actionText":"Use","unlist":false,"disableUse":false,"purchaseLimit":"[]","limitScope":"purchase"}]}
```

### ShopItem :id=model-shop-item

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Item ID. |
| `name` | string | Item name. |
| `desc` | string | Description. |
| `icon` | string | Readable image URI, or empty string. Download through `/files/{url}`. |
| `categoryId` | integer | Shop category ID. |
| `stockNumber` | integer | Available stock; negative stock is treated as unlimited when calculating `maxPurchaseNumber`. |
| `ownNumber` | integer | Owned inventory quantity. |
| `price` | integer | Coin price. |
| `order` | integer | Stored order within its category. |
| `disablePurchase` | boolean | Purchase disabled flag. |
| `maxPurchaseNumber` | integer or null | Calculated from available coins plus credit and finite stock; disabled purchases yield 0, free unlimited stock can yield 2147483647. This calculation does not evaluate all purchase-limit rules. |
| `titleColorString` | string or null | Custom title color. |
| `actionText` | string or null | Custom use-button text. |
| `unlist` | boolean | Unlisted/deleted flag; normally false for list results. |
| `disableUse` | boolean | Whether the item has an unusable effect. |
| `purchaseLimit` | string or null | **JSON encoded string**, not a nested array; the item's purchase-limit configuration. |
| `limitScope` | string or null | `purchase`, `use`, or `both`. |

### GET /items_categories :id=get-items-categories

Returns `ShopCategory[]`. Optional `include_hidden` defaults to `false`; only the exact lowercase strings `true` and `false` are parsed, and any other value falls back to false. Includes the virtual all category. The HTTP server does not expose `/items_categories/{id}`.

```json
{"code":200,"message":"success","data":[{"id":0,"name":"Default","isAsc":true,"sort":"order","order":0,"hidden":false,"inventoryHidden":false}]}
```

### ShopCategory :id=model-shop-category

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Category ID. |
| `name` | string | Category name. |
| `isAsc` | boolean | Configured ascending sort flag. |
| `sort` | string | Stored sort preference. |
| `order` | integer | Category order. |
| `hidden` | boolean | Hidden in shop. |
| `inventoryHidden` | boolean | Hidden in inventory. |

### GET /tasks_categories :id=get-tasks-categories

Returns `TaskCategory[]`, including the all category and archived categories. No query parameters or pagination. The HTTP server does not expose `/tasks_categories/{id}`. Category sort/filter values are stored app preferences; this endpoint does not apply them as request filters.

```json
{"code":200,"message":"success","data":[{"id":1,"name":"Daily","isAsc":true,"sort":"order","filter":"","order":0,"status":0,"type":0}]}
```

### TaskCategory :id=model-task-category

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Category ID. |
| `name` | string | Name. |
| `isAsc` | boolean | Stored ascending sort flag. |
| `sort` | string | Stored sort preference. |
| `filter` | string | Stored grouping/filter preference. |
| `order` | integer | Category order. |
| `status` | integer | 0 normal; 1 archived. |
| `type` | integer | 0 normal; smart lists use 10 daily, 11 weekly, 12 monthly, 20 doing. |
