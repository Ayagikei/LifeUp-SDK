# Synthesis

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-synthesis

Help topic: `query/get-synthesis`

## Synthesis :id=get-synthesis

`GET /synthesis` returns `Synthesis[]` across visible categories. `GET /synthesis/{id}` returns recipes **in category `id`**, including that category's hidden content. Nonnegative IDs select a category; negative IDs mean all visible categories. Invalid IDs fall back to the collection route. Results follow stored recipe order ascending; deleted recipes are excluded. No pagination.

```json
{"code":200,"message":"success","data":[{"id":1,"name":"Tea","desc":"","input":[{"itemId":2,"itemName":"Tea leaf","amount":2,"ownedNumber":5,"icon":""}],"output":[{"itemId":3,"itemName":"Tea","amount":1,"ownedNumber":0,"icon":""}],"categoryId":0,"canSynthesisTimes":2}]}
```

### Synthesis :id=model-synthesis

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Recipe ID. |
| `name` | string | Name. |
| `desc` | string | Description. |
| `input` | SynthesisItemInfo[] | Required items. |
| `output` | SynthesisItemInfo[] | Produced items. |
| `categoryId` | integer or null | Category ID. |
| `canSynthesisTimes` | integer | Currently available synthesis count. |

### SynthesisItemInfo :id=model-synthesis-item-info

| Field | JSON type | Meaning |
| --- | --- | --- |
| `itemId` | integer | Item ID. |
| `itemName` | string | Item name. |
| `amount` | integer | Input/output quantity per synthesis. |
| `ownedNumber` | integer | Owned quantity. |
| `icon` | string | Readable icon URI or empty. |

### GET /synthesis_categories and /synthesis_categories/{id} :id=get-synthesis-categories

Both return `SynthesisCategory[]`. The optional path `id` filters category ID; it does not change the array shape. Invalid IDs fall back to all categories. Optional `include_hidden` uses strict lowercase Boolean parsing and defaults to false, including when a category ID is supplied. No pagination.

```json
{"code":200,"message":"success","data":[{"id":0,"name":"Default","isAsc":true,"sort":"order","order":0,"hidden":false}]}
```

### SynthesisCategory :id=model-synthesis-category

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Category ID. |
| `name` | string | Name. |
| `isAsc` | boolean | Stored ascending sort flag. |
| `sort` | string | Stored sort preference. |
| `order` | integer | Category order. |
| `hidden` | boolean | Hidden flag. |
