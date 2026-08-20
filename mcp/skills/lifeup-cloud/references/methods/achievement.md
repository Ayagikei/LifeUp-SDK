# achievement

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** achievement

**Description:** Add or edit custom achievements and achievement subcategories

**Examples:**

- Create an achievement: [lifeup://api/achievement?name=Collector&desc=Collect 100 items&category_id=1](lifeup://api/achievement?name=Collector&desc=Collect 100 items&category_id=1)
  - You may need to replace `category_id` with your actual available achievement list id to test this example
- Create an achievement with unlock conditions: [lifeup://api/achievement?name=Millionaire&conditions_json=%5B%7B%22type%22%3A7%2C%22target%22%3A1000000%7D%5D&category_id=1](lifeup://api/achievement?name=Millionaire&conditions_json=%5B%7B%22type%22%3A7%2C%22target%22%3A1000000%7D%5D&category_id=1)
  - You may need to replace `category_id` with your actual available achievement list id to test this example
  - The decoded content of `conditions_json` is `[{"type":7,"target":1000000}]`
- Edit existing achievement: [lifeup://api/achievement?edit_id=1&name=New Achievement Name&exp=100](lifeup://api/achievement?edit_id=1&name=New Achievement Name&exp=100)
- Set an emoji icon: `call_api` `achievement` `icon_uri=🏆` (param is `icon_uri`, not `icon`)

#### 1. Achievement Parameters

| Parameter      | Meaning           | Values               | Example   | Required | Notes                           |
| ------------- | ----------------- | -------------------- | --------- | -------- | ------------------------------- |
| edit_id       | Achievement ID to edit | number greater than 0 | 1      | No       | Required when editing          |
| is_subcategory| Is subcategory    | true or false        | false     | No       | Defaults to false               |
| name          | Achievement name   | any text             | Collector | No*      | Required for new achievements   |
| desc          | Description       | any text             | Collect 100 items | No |                               |
| icon_uri      | Icon              | emoji, http(s) URL, content URI, or empty | 🏆 | No | Not `icon`. Emoji is stored as an `emoji_` file. Empty clears. |
| order         | Sort order        | integer              | 1         | No       | Position in list                |
| category_id   | Category ID       | number greater than 0 | 1        | No*      | Required when creating subcategory |
| unlocked      | Unlock status     | true or false        | true      | No       | true - unlock immediately<br/>false - reset to locked |
| unlock_time   | Unlock time       | timestamp (milliseconds) | 1640995200000 | No | Only effective when already unlocked |
| delete        | Delete flag       | true or false        | false     | No       |                                |
| secret        | Hidden achievement| true or false        | false     | No       |                                |
| write_feeling | Record feelings   | true or false        | false     | No       |                                |
| color         | Title color       | color string         | #66CCFF   | No       | # must be escaped as %23        |
| auto_use_item | Auto use item     | true or false        | false     | No       |                                |
| skills        | Skill IDs         | array of numbers greater than 0 | 1 | No    | Supports arrays (e.g., &skills=1&skills=2) |
| exp           | Experience reward | integer              | 100       | No       |                                |
| item_id       | Item ID           | number greater than 0 | 1        | No*      | One of item_id or item_name required |
| item_name     | Item name         | any text             | Treasure  | No*      | One of item_id or item_name required |
| item_amount   | Item quantity     | [1, 99]             | 1         | No       | Defaults to 1                   |
| items         | Item rewards JSON | JSON text            | [{"item_id":1,"amount":2}] | No | Set multiple item rewards, see format below |
| conditions_json| Unlock conditions JSON | JSON text      | [{"type":7,"target":1000000}] | No | Set unlock conditions, see format below |
| coin         | Coin reward       | [0, 999999]      | 10         | No       | Amount of coins earned when unlocking the achievement |
| coin_var     | Coin reward variation | integer              | 5          | No       | Variation range for coin rewards |
| coin_set_type| How to set coin value | One of:<br/>absolute<br/>relative | absolute | No | absolute - directly set coin to value<br/>relative - add/subtract from original coin value |
| exp_set_type | How to set exp value | One of:<br/>absolute<br/>relative | absolute | No | absolute - directly set exp to value<br/>relative - add/subtract from original exp value |

**Response:**

| Field  | Type    | Description      | Example | Notes                    |
| ------ | ------- | ---------------- | ------- | ------------------------ |
| id     | Number  | Achievement ID   | 1000    | ID of new or edited achievement |

#### 2. Subcategory Parameters

| Parameter     | Meaning           | Values               | Example   | Required | Notes                           |
| ------------ | ----------------- | -------------------- | --------- | -------- | ------------------------------- |
| is_subcategory | Must be true | true | true | Yes | Required to create/edit a subcategory |
| is_collapsed | Collapse status   | true or false        | false     | No       | Only applies to subcategories   |

Subcategories reject `icon_uri` (including emoji) with `unsupported_parameter`. Editing a subcategory without `is_subcategory=true` returns `is_subcategory_required`, not a missing `edit_id`.
**Response:**

| Field  | Type    | Description      | Example | Notes                    |
| ------ | ------- | ---------------- | ------- | ------------------------ |
| id     | Number  | Achievement ID   | 1000    | ID of new or edited achievement (subcategory) |

#### 3. Unlock Condition Types

| Type Code | Description             | Requires related_id | related_id Type | target Description  |
| --------- | ----------------------- | ------------------ | --------------- | ------------------ |
| 0         | Task completion count   | Yes                | Task ID         | Number of completions |
| 1         | Task completion streak  | Yes                | Task ID         | Streak count       |
| 3         | Pomodoro count         | No                 | -               | Number of pomodoros |
| 4         | Days using LifeUp      | No                 | -               | Number of days     |
| 5         | Like count             | No                 | -               | Number of likes    |
| 6         | Daily completion streak | No                 | -               | Streak days        |
| 7         | Current coins          | No                 | -               | Amount of coins    |
| 8         | Coins earned in one day| No                 | -               | Amount of coins    |
| 9         | Task pomodoro count    | Yes                | Task ID         | Number of pomodoros |
| 10        | Item purchase count    | Yes                | Item ID         | Purchase count     |
| 11        | Item usage count       | Yes                | Item ID         | Usage count        |
| 12        | Loot box item count    | Yes                | Item ID         | Obtained count     |
| 13        | Skill level reached    | Yes                | Skill ID        | Level value        |
| 14        | Life level            | No                 | -               | Level value        |
| 15        | Total items obtained   | Yes                | Item ID         | Total obtain count |
| 16        | Items from synthesis   | Yes                | Item ID         | Synthesis count    |
| 17        | Current item quantity  | Yes                | Item ID         | Own count          |
| 18        | Task focus duration    | Yes                | Task ID         | Duration (minutes) |
| 19        | ATM savings           | No                 | -               | Savings amount     |
| 20        | External API          | No                 | -               | API defined        |
| 520       | Complete N distinct tasks daily | No         | -               | Distinct task count (deduplicated by group ID; existing type) |
| 524       | Complete N task completions daily | No       | -               | Total valid completion count in a day (v1.104.4+) |

> As of v1.104.4, types `520` and `524` use the following semantics:
>
> - Both share the same completion definition and local calendar day boundary (`TimeRange.today()`).
> - Ordinary tasks count `COMPLETED`; negative tasks count `GIVE_UP`.
> - Type `520` deduplicates by effective `groupId` (falls back to task record id when group id is missing). Completing the same unlimited task multiple times in a day still counts as 1 distinct task.
> - Type `524` counts each valid completion row. Completing the same unlimited task 5 times yields `completionCount = 5`.
> - Existing achievements with `type=520` keep the distinct-task semantics; no migration is required.

#### 4. JSON Format Specifications

##### Unlock Conditions (conditions_json)

```json
[
    {
        "type": 7,           // Condition type (refer to table above)
        "related_id": null,  // Related ID (required for some types)
        "target": 1000000    // Target value
    },
    {
        "type": 10,          // Example: Purchase specific item
        "related_id": 1,     // Item ID
        "target": 5          // Purchase 5 times
    },
    {
        "type": 520,         // Complete N distinct tasks daily
        "related_id": null,
        "target": 5
    },
    {
        "type": 524,         // Complete N task completions daily
        "related_id": null,
        "target": 10
    }
]
```

##### Item Rewards (items)

```json
[
    {
        "item_id": 1,    // Item ID
        "amount": 2      // Quantity
    },
    {
        "item_id": 2,
        "amount": 3
    }
]
```

<br/>
