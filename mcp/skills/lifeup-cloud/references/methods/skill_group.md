# skill_group

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** skill_group

**Description:** Create, edit, delete, or reorder skill groups. The sort API also supports mixed ordering of groups and skills.

**Examples:**

- Create a group: [lifeup://api/skill_group?content=Combat](lifeup://api/skill_group?content=Combat)
- Edit a group: [lifeup://api/skill_group?id=10&content=Combat&order=20&collapsed=true](lifeup://api/skill_group?id=10&content=Combat&order=20&collapsed=true)
- Delete a group: [lifeup://api/skill_group?id=10&delete=true](lifeup://api/skill_group?id=10&delete=true)
- Sort groups and skills together:

```text
lifeup://api/skill_group?sort_json=[{"type":"skill","id":2},{"type":"group","id":10},{"type":"skill","id":3}]
```

| Parameter | Meaning | Values | Example | Required | Notes |
| --------- | ------- | ------ | ------- | -------- | ----- |
| id | Skill group ID | number greater than 0 | 10 | No* | Required when editing or deleting |
| content | Group name | any text | Combat | No* | Required when creating |
| order | Sort order | integer | 20 | No | Raw `orderInCategory` value; it must be unique in the mixed skill/group list |
| collapsed | Collapse state | true or false | true | No | Whether the group is collapsed |
| delete | Delete flag | true or false | false | No | Only valid when editing |
| sort_json | Mixed sort nodes | JSON array | `[{"type":"skill","id":2},{"type":"group","id":10}]` | No* | When provided, CRUD parameters are ignored and the mixed sort plan is applied. Partial sorting is supported: unspecified nodes keep their relative order |

`sort_json` node format:

| Field | Meaning | Values |
| ----- | ------- | ------ |
| type | Node type | `skill` / `group` |
| id | Entity ID | number greater than 0 |

**Response:**

| Field | Type | Description | Example | Notes |
| ----- | ---- | ----------- | ------- | ----- |
| id | Number | Skill group ID | 10 | Returned for create / edit / delete |
| count | Number | Number of sorted nodes | 3 | Returned for `sort_json` requests |

<br/>
