# Skills and skill groups

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-skills

Help topic: `query/get-skills`

## Skills and skill groups :id=get-skills

`GET /skills` returns `Skill[]`, with no parameters or pagination. Hidden/deleted skills are excluded and stored order is used. `group_id` and `status` from the underlying ContentProvider are **not** fields of the HTTP model.

```json
{"code":200,"message":"success","data":[{"id":1,"name":"Strength","desc":"","icon":"","order":0,"color":-65536,"exp":150,"level":2,"untilNextLevelExp":50,"currentLevelExp":50,"type":1}]}
```

### Skill :id=model-skill

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Skill ID. |
| `name` | string | Localized skill name. |
| `desc` | string | Description. |
| `icon` | string | Readable icon URI, or empty. |
| `order` | integer | Stored order. |
| `color` | integer | Android packed color integer. |
| `exp` | integer | Current total EXP. |
| `level` | integer | Current level. |
| `untilNextLevelExp` | integer | EXP remaining before the next level. |
| `currentLevelExp` | integer | EXP earned since the current level began. |
| `type` | integer | 0 user; 1 strength; 2 learning; 3 charm; 4 endurance; 5 vitality; 6 creativity. |

### GET /skill_groups :id=get-skill-groups

Returns `SkillGroup[]`. `include_hidden` has the same strict Boolean parsing as `/items_categories`, default false. The current app repository ignores this flag for groups and always excludes deleted groups. Stored order is ascending. No pagination.

```json
{"code":200,"message":"success","data":[{"id":1,"content":"Personal","order":0,"collapsed":false}]}
```

### SkillGroup :id=model-skill-group

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Group ID. |
| `content` | string | Group title. |
| `order` | integer | Stored group order. |
| `collapsed` | boolean | Collapsed state. |
