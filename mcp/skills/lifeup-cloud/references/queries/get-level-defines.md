# Level definitions

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-level-defines

Help topic: `query/get-level-defines`

## Level definitions :id=get-level-defines

`GET /level_defines` returns a single `LevelDefines` object. No parameters. Uses custom definitions when custom level mode is enabled, otherwise built-in definitions.

```json
{"code":200,"message":"success","data":{"custom":true,"levels":[{"id":1,"levelStart":1,"levelEnd":10,"perLevelExp":100}]}}
```

### LevelDefines :id=model-level-defines

| Field | JSON type | Meaning |
| --- | --- | --- |
| `custom` | boolean | Custom level mode. |
| `levels` | LevelDefine[] | Level ranges. |

### LevelDefine :id=model-level-define

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Definition ID. |
| `levelStart` | integer | Starting level. |
| `levelEnd` | integer | Ending level. |
| `perLevelExp` | integer | EXP required per level in the range. |

Malformed level JSON currently becomes an empty `levels` array rather than failing the complete request.
