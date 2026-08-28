# loot_box

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** loot_box

**Description:** Modify the loot box effect of the specified box item, support adjustment of probability, number of rewards and increase content. (Delete is not supported for now)

**Example:** <a href="lifeup://api/loot_box?name=Coin loot box&sub_name=A big bag of coins&set_type=relative&probability=1&fixed=false">lifeup://api/loot_box?name=Coin loot box&sub_name=A big bag of coins&set_type=relative&probability=1&fixed=false</a>

**Explanation:** Increase the proportion of the [large] bag of gold coins in the gold coin box by 1 point.

| Parameter   | Meaning                               | Type                                                    | Example        | Required | Notes                                                        |
| ----------- | ------------------------------------- | ------------------------------------------------------- | -------------- | -------- | ------------------------------------------------------------ |
| id          | item id                               | a number greater than 0                                 | 1              | no*      | Please refer to the above "Basic Knowledge - LifeUp Data ID" for how to obtain |
| name        | item name                             | any text                                                | Treasure chest | no*      | When used for unknown id, fuzzy search product, not name modification |
| sub_id      | content item id                       | a number greater than 0                                 | 1              | no*      | id of chest contents                                         |
| sub_name    | content item name                     | any text                                                | Get a gift     | no*      | For fuzzy search items when the id of the contents of the box is unknown |
| set_type    | adjustment method (absolute/relative) | one of the following values: <br/>absolute<br/>relative | relative       | no       | absolute - absolute value, that is, directly set the target to value<br/>relative - relative values, adding or subtracting from the original value |
| amount      | number of content item                | number                                                  | 1              | no       | number of rewards for a single item                          |
| probability | probability of the content item       | number                                                  | 1              | no       | -                                                            |
| fixed       | whether it is a fixed reward          | boolean                                                 | true/false     | no       | -                                                            |

**Notice:**

1. In order to search for a product, either id or name must be provided.
1. In order to search for content, either sub_id or sub_name must be provided.
1. `name` and `sub_name` try exact matching first, then fall back to fuzzy matching.
1. The legacy `loot_box` API keeps its compatibility behavior: if the same content item appears multiple times with different amounts, it edits the first matched entry and does not use `sub_amount` for disambiguation. Use `loot_box/v2` when you need amount-specific editing, deletion, or merge behavior.

?> v2 API (`sub_amount`, delete): `help` `loot_box/v2`.
