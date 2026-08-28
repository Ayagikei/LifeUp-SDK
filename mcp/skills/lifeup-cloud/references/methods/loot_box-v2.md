# loot_box/v2

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** loot_box/v2

**Description:** An improved version of the loot_box API. Modify the loot box effect of the specified box item, support adjustment of probability, number of rewards, adding content, and **deleting content**.

**Improvements over v1:**
- **`sub_amount` for precise matching**: When the box contains multiple entries of the same item with different amounts (e.g. A x1 50%, A x2 30%), use `sub_amount` to target a specific entry. Default value is `1`. If no matching entry is found, LifeUp looks up the item by `sub_id` / `sub_name` and adds a new entry; if the request is an `amount=0` deletion, no new entry is added.
- **Independent `set_type`**: `amount_set_type` and `probability_set_type` can be controlled independently. The global `set_type` serves as fallback default.
- **Delete support**: Setting `amount=0` with `amount_set_type=absolute` (or computing to `<=0` with `relative`) deletes the matched entry.
- **Duplicate merge**: If changing `amount` would duplicate an existing entry with the same item and amount in the same box, LifeUp merges into the existing entry and continues applying the request's `probability` / `fixed` values.

**Example:** <a href="lifeup://api/loot_box/v2?name=Coin loot box&sub_name=A big bag of coins&sub_amount=2&probability_set_type=relative&probability=10">lifeup://api/loot_box/v2?name=Coin loot box&sub_name=A big bag of coins&sub_amount=2&probability_set_type=relative&probability=10</a>

**Explanation:** Increase the probability of the [large] bag of gold coins (x2) in the gold coin box by 10 points.

| Parameter              | Meaning                               | Type                                                    | Example        | Required | Notes                                                        |
| ---------------------- | ------------------------------------- | ------------------------------------------------------- | -------------- | -------- | ------------------------------------------------------------ |
| id                     | item id                               | a number greater than 0                                 | 1              | no*      | Please refer to the above "Basic Knowledge - LifeUp Data ID" for how to obtain |
| name                   | item name                             | any text                                                | Treasure chest | no*      | When used for unknown id, fuzzy search product, not name modification |
| sub_id                 | content item id                       | a number greater than 0                                 | 1              | no*      | id of chest contents. If both sub_id and sub_name are provided, sub_id takes precedence |
| sub_name               | content item name                     | any text                                                | Get a gift     | no*      | For fuzzy search items when the id of the contents of the box is unknown |
| sub_amount             | content item amount for matching      | number                                                  | 2              | no       | Used to precisely match an entry with this amount. Minimum `1`, default `1`. If no match is found and this is not a deletion request, a new entry is added. |
| set_type               | global adjustment method              | one of: `absolute` / `relative`                         | relative       | no       | Default for `amount_set_type` and `probability_set_type` if not specified |
| amount_set_type        | adjustment method for amount          | one of: `absolute` / `relative`                         | relative       | no       | Overrides `set_type` for the amount field                    |
| probability_set_type   | adjustment method for probability     | one of: `absolute` / `relative`                         | absolute       | no       | Overrides `set_type` for the probability field               |
| amount                 | number of content item                | number                                                  | 1              | no       | number of rewards for a single item. `0` (absolute) or computed `<=0` (relative) deletes the entry |
| probability            | probability of the content item       | number                                                  | 1              | no       | -                                                            |
| fixed                  | whether it is a fixed reward          | boolean                                                 | true/false     | no       | -                                                            |
| query                  | list box contents                     | true or false                                           | true           | no       | v1.105.1+. Returns item JSON only; sub_id / sub_name not required |

**Notice:**

1. In order to search for a product, either id or name must be provided.
1. In order to search for content, either sub_id or sub_name must be provided. Use `query=true` to list contents without sub_id / sub_name.
1. If both `sub_id` and `sub_name` are provided, `sub_id` takes precedence. `sub_name` is used only when no valid `sub_id` is provided.
1. `name` and `sub_name` try exact matching first, then fall back to fuzzy matching.
1. `sub_amount` defaults to `1`. When the box has multiple entries of the same item with different amounts, provide `sub_amount` to target a specific entry. If no match is found and this is not a deletion request, a new entry with `amount=sub_amount` is added.
1. To delete an entry, set `amount=0` with `amount_set_type=absolute`, or use `amount_set_type=relative` with a negative value that brings the total to `<=0`. Deletion only applies to matched entries; if no existing entry is matched, `amount=0` does not add a new entry.
1. If changing an entry's `amount` would duplicate an existing entry with the same item and amount in the same box, LifeUp merges into the existing entry and continues applying the request's `probability` / `fixed` values.
1. When deletion leaves the box empty, the entire loot box effect is soft-deleted (the item itself is preserved and you can re-add loot box entries later).

?> Use owned items from inventory: `help` `use_item`.
