# query_skill_group

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** query_skill_group

**Description:** Query a single skill group and return its raw sort and collapsed state.

**Example:**

- Query a skill group: [lifeup://api/query_skill_group?id=10](lifeup://api/query_skill_group?id=10)

| Parameter | Meaning | Type | Example | Required | Notes |
| --------- | ------- | ---- | ------- | -------- | ----- |
| id | Skill group ID | number greater than 0 | 10 | yes | - |

**Return Value:**

| Parameter | Meaning | Type | Example | Required | Notes |
| --------- | ------- | ---- | ------- | -------- | ----- |
| id | Skill group ID | number | 10 | yes | - |
| content | Group name | string | Combat | yes | - |
| order | Raw sort order | number | 20 | yes | `orderInCategory` |
| collapsed | Collapse state | string | true | yes | Returned as `true` / `false` text |

<br/>

<br/>
