# Inventory records

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-inventory-records

Help topic: `query/get-inventory-records`

## Inventory records :id=get-inventory-records

`GET /inventory_records` returns `InventoryRecord[]`. `offset` defaults to 0 and is clamped to at least 0; `limit` defaults to 100 and is clamped to 1–200. Optional `time_range_start` and `time_range_end` are forwarded only if both parse as 64-bit integers and start < end; invalid/incomplete ranges are silently omitted by the SDK. Valid ranges use inclusive `time >= start AND time <= end`. Deleted records are excluded (`isDel` null or 0); sorting is `time DESC, id DESC`. No item-ID filter is exposed.

```json
{"code":200,"message":"success","data":[{"id":1,"time":1700000000000,"itemId":2,"itemName":"Tea","changeNumber":1,"isDecrease":false,"resCode":0,"desc":"Purchase"}]}
```

### InventoryRecord :id=model-inventory-record

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Record ID. |
| `time` | integer or null | Creation Unix time in milliseconds. |
| `itemId` | integer or null | Related item ID. |
| `itemName` | string or null | Related item name when available. |
| `changeNumber` | integer | Recorded quantity change. |
| `isDecrease` | boolean | Whether the change decreases inventory. |
| `resCode` | integer | Source reason code; see below. |
| `desc` | string or null | Record description. |

Reason codes: 0 purchase; 1 use; 2 task completion; 3 undo completion; 4 clear data; 5 give up task; 6 overdue task; 7 built-in achievement unlock; 8 undo give-up; 9 undo overdue; 10 return item; 11 subtask completion; 12 undo subtask completion; 13 user achievement unlock; 14 undo user achievement unlock; 15 deposit; 16 withdrawal; 17 tomato sale; 20 reward item; 21 undo reward; 23 synthesis; 24 loot box; 25 ATM interest; 26 tomato exchange item; 27 credit interest; 28 API; 29 item-effect stock change. Clients should preserve unknown future codes.
