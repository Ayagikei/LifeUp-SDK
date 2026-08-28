# Item JSON structures

Shared shapes for shop/task/achievement/synthesis params. Pass each JSON blob as one **string** param; MCP encodes it.

#### 1. Item Reward Structure

Used by task `items`, achievement `items`, subtask `items`, and synthesis `inputItems` / `outputItems`.

```json
[
  { "item_id": 1, "amount": 2 },
  { "item_id": 2, "amount": 3 }
]
```

Field name is **`item_id`** (snake_case), not `itemId`.

#### 3. Purchase Limit Structure

`purchase_limit` is a JSON array. Each object is one restriction rule.

| Field | Meaning | Type | Required | Notes |
| ----- | ------- | ---- | -------- | ----- |
| limitType | Restriction type | number | Yes | See table below |
| limitNumber | Primary numeric value | number | No* | Quantity / range rules |
| maxNumber | Range upper bound | number | No | Attribute level / owned-item quantity range |
| limitId | Related target ID | number | No* | Attribute / item / task / task cycle / achievement rules |
| extendInfo | Extra payload | string | No | Time rules; value is a **JSON string** |

**limitType**

| limitType | Meaning | Required fields / notes |
| --------- | ------- | ----------------------- |
| 0 | Daily quantity limit | `limitNumber` |
| 1 | Weekly quantity limit | `limitNumber` |
| 2 | Monthly quantity limit | `limitNumber` |
| 3 | Yearly quantity limit | `limitNumber` |
| 10 | Attribute level rule | `limitId`, `limitNumber`; optional `maxNumber` |
| 20 | Daily time range | `extendInfo`: `{"startMinuteOfDay":540,"endMinuteOfDay":1320}` |
| 21 | Weekday selection | `extendInfo`: `{"weekdays":[1,2,3,4,5]}` (Mon=1 … Sun=7) |
| 22 | Absolute time range | `extendInfo`: `{"startMillis":…,"endMillis":…}` |
| 23 | Month selection | `extendInfo`: `{"months":[1,6,12]}` |
| 24 | Day-of-month selection | `extendInfo`: `{"daysOfMonth":[1,15,31]}` |
| 30 | Owned item quantity rule | `limitId`, `limitNumber`; optional `maxNumber` |
| 31 | Task completed rule | `limitId`: task ID |
| 32 | Achievement unlocked rule | `limitId`: achievement ID |
| 33 | Task cycle completed rule | `limitId`: repeat task **groupId** (not task id) |

`limitType=33` uses the repeat task `groupId` of the latest started cycle. `extendInfo` needs an extra encode layer in raw URLs.

**Example**

```json
[
  { "limitType": 0, "limitNumber": 5 },
  { "limitType": 10, "limitId": 1, "limitNumber": 5, "maxNumber": 10 }
]
```

#### 4. Item Effects Structure

`effects` is a JSON array. Each object runs when the item is **used** (after purchase).

```json
[
  {
    "type": 2,
    "info": { "min": 100, "max": 200 }
  }
]
```

**Effect types** (10–16 need LifeUp v1.102.0+)

| Type | Meaning | `info` params |
| ---- | ------- | ------------- |
| 0 | No special effect | — |
| 1 | Not usable | — |
| 2 | Add coins | `min`, optional `max`, optional `using_limit` |
| 3 | Remove coins | same as 2 |
| 4 | Add experience | `ids` (skill id array), `value` or `min`, optional `max`, optional `using_limit` |
| 5 | Remove experience | same as 4 |
| 6 | Simple synthesis | `require_number`, `item_id` |
| 7 | Loot box | `items`: `[{ item_id, amount, probability, is_fixed_reward }]` |
| 8 | Countdown | `seconds` |
| 9 | Link / URL | `url`, optional `use_web_view` (default `false`) — see **Type 9** below |
| 10 | Record feeling | — |
| 11 | Change coins (+/−) | `min` (can be negative), optional `max`, optional `using_limit` |
| 12 | Change experience (+/−) | same shape as 4; `min` can be negative |
| 13 | Add item stock | `item_id`, `min`, optional `max`, optional `using_limit` |
| 14 | Remove item stock | same as 13 |
| 15 | Change item stock (+/−) | same as 13; `min` can be negative |
| 16 | Play sound | `file_name` or `uri`, optional `display_name` |

**Type 9 — link / scheme behavior**

When the item is used, LifeUp opens `info.url`:

| URL form | Behavior |
| -------- | -------- |
| `lifeup://api/...` | Runs a LifeUp API (reward, complete, toast, …). Same as tapping a lifeup link in notes. Use MCP `call_api` when you need return values. |
| `http(s)://...` | External page. `use_web_view: true` opens LifeUp's built-in browser; otherwise the system browser / handler. |
| Other schemes (`weixin://`, `intent://`, app-specific …) | Opens the target app via Android Intent. LifeUp does not ship third-party scheme lists. |

Multiple type-9 effects on one item all fire; LifeUp API URLs are ordered after external URLs.

**Examples**

Random coins:

```json
{ "type": 2, "info": { "min": 100, "max": 200 } }
```

LifeUp API on use:

```json
{ "type": 9, "info": { "url": "lifeup://api/reward?type=coin&content=Rest&number=5" } }
```

External page in WebView:

```json
{ "type": 9, "info": { "url": "https://example.com/game", "use_web_view": true } }
```

Loot box:

```json
{
  "type": 7,
  "info": {
    "items": [
      { "item_id": 1, "amount": 1, "probability": 50, "is_fixed_reward": false },
      { "item_id": 2, "amount": 1, "probability": 50, "is_fixed_reward": true }
    ]
  }
}
```

!> `effects` overrides `disable_use`. Type 1 (not usable) in `effects` wins over `disable_use=false`.
