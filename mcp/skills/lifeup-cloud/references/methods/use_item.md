# use_item

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** use_item

**Description:** Use a specified item.

**Example:**

- Open a coin box: [lifeup://api/use_item?name=coin_box&use_times=1](lifeup://api/use_item?name=coin_box&use_times=1)

| Parameter | Meaning     | Type                    | Example  | Required | Notes                                                        |
| --------- | ----------- | ----------------------- | -------- | -------- | ------------------------------------------------------------ |
| id        | Item ID     | a number greater than 0 | 1        | No*      | For obtaining the item ID, please refer to the "Basic Knowledge - LifeUp Data ID" section |
| name      | Item name   | Any text                | coin_box | No*      | Used for unknown IDs; performs a fuzzy search for items      |
| use_times | Usage times | a number greater than 0 | 1        | No       | Default is 1 time<br/>For regular items or opening boxes, it corresponds to the quantity of the item<br/>For simple synthesis items, this value corresponds to the "synthesis quantity" rather than the number of consumed items |

**Return:**

!> This API may fail for some reasons, and specific failure reasons may be provided in the return values.

| Parameter | Meaning            | Type     | Example          | Required | Notes                                                        |
| --------- | ------------------ | -------- | ---------------- | -------- | ------------------------------------------------------------ |
| result    | Result code        | a number | 0                | Yes      | 0 - Successful usage<br/>1 - Database exception<br/>2 - Insufficient experience points restriction<br/>3 - Item not found<br/>4 - Running countdown conflict<br/>5 - Insufficient inventory<br/>6 - Unusable item<br/>7 - Coin limit<br/>8 - Target stock limit<br/>9 - Attribute level restriction<br/>10 - Time restriction<br/>11 - Owned item quantity restriction<br/>12 - Task completion restriction<br/>13 - Achievement unlock restriction<br/>14 - Period quantity restriction<br/>15 - Task cycle completed restriction |
| desc      | Result description | Text     | RunningCountDown | Yes      |                                                              |

<br/>
