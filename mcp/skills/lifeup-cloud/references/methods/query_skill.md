# query_skill

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** query_skill

**Description:** Query the basic information, raw sort fields, and level/experience data of a specified skill.

It is possible to use this api to customize your attributes widgets.

**Example:**

- Query strength attribute: [lifeup://api/query_skill?id=1](lifeup://api/query_skill?id=1)

| Parameter | Meaning              | Type                    | Example | Required | Notes |
| --------- | -------------------- | ----------------------- | ------- | -------- | ----- |
| id        | attribute (skill) id | a number greater than 0 | 1       | yes      | For the acquisition method, please refer to the above "Basic Knowledge - Person Level Data ID" |

**Return Value:**

Only supported since version 1.90.6

| Parameter            | Meaning                              | Type   | Example  | Required | Notes |
| -------------------  | ------------------------------------ | ------ | -------- | -------- | ----- |
| id                   | skill id                             | number | 1        | yes      | Added to `query_skill` in v1.103.0+ |
| name                 | attribute name                       | string | strength | yes      |       |
| order                | raw sort order                       | number | 20       | yes      | Added in v1.103.0+; `orderInCategory` |
| group_id             | skill group ID                       | number | 10       | yes      | Added in v1.103.0+; returns `0` when the skill is not in a group |
| status               | status                               | number | 0        | yes      | Added in v1.103.0+; `0` = normal, `1` = hidden |
| level                | level                                | number | 10       | yes      |       |
| total_exp            | total experience points              | number | 10000    | yes      |       |
| until_next_level_exp | EXP required to reach the next level | number | 99       | yes      |       |
| current_level_exp    | Earned EXP above current level       | Number | 1000     | Yes      |       |

<br/>
