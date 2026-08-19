# skill

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** skill

**Description:** Create or edit custom skills (attributes)

**Examples:**

- Create a skill: [lifeup://api/skill?content=Programming&desc=Coding ability&color=%23FF6B6B](lifeup://api/skill?content=Programming&desc=Coding ability&color=%23FF6B6B)
- Edit skill experience: [lifeup://api/skill?id=1&exp=100](lifeup://api/skill?id=1&exp=100)
- Delete skill: [lifeup://api/skill?id=1&delete=true](lifeup://api/skill?id=1&delete=true)

| Parameter    | Meaning           | Values               | Example    | Required | Notes                           |
| ----------- | ----------------- | -------------------- | ---------- | -------- | ------------------------------- |
| id          | Skill ID          | number greater than 0 | 1         | No       | Required when editing           |
| content     | Skill name        | any text             | Programming| No*      | Required for new skills         |
| desc        | Description       | any text             | Coding ability | No    |                                |
| icon        | Icon              | any text             | 💻         | No       | Can use emoji                   |
| color       | Color             | color string         | #FF6B6B    | No       | # must be escaped as %23        |
| type        | Type              | integer              | 0          | No       |                                |
| order       | Sort order        | integer              | 1          | No       | Raw mixed-list position. When used alone, the skill is placed at that position and the final group is inferred from layout; when used with `group_id`, it is snapped to the nearest legal position inside the target group |
| group_id    | Skill group ID    | integer              | 10         | No       | Requires v1.103.0+; it cannot be less than `0`, and only `0` moves the skill to the ungrouped area. When used alone, new skills are appended to the target group tail, while edited skills keep their current position if already in that group, otherwise they move to the group tail; when used with `order`, `group_id` takes priority |
| status      | Status            | integer              | 0          | No       |                                |
| exp         | Experience points | number greater than or equal to 0 | 100 | No | Current skill experience        |
| delete      | Delete flag       | true or false        | false      | No       | Only valid when editing         |

**Response:**

| Field  | Type    | Description    | Example | Notes                    |
| ------ | ------- | -------------- | ------- | ------------------------ |
| id     | Number  | Skill ID       | 1000    | ID of new or edited skill |

<br/>
