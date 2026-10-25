# Achievements

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-achievements

Help topic: `query/get-achievements`

## Achievements :id=get-achievements

`GET /achievements` returns `Achievement[]` by loading each category in category order and concatenating its achievements. `GET /achievements/{id}` returns achievements **in category `id`**, not an achievement detail. Invalid path IDs behave like the unfiltered route. Sorting inside each category follows the category's app sort preference. Neither endpoint is paginated.

```json
{"code":200,"message":"success","data":[{"id":10,"name":"First step","desc":"Complete a task","iconUri":"","categoryId":1,"status":0,"exp":10,"coin":5,"coinVariable":0,"type":0,"progress":0,"order":0,"itemId":null,"itemAmount":null,"unlockedTime":null,"items":[],"color":""}]}
```

### Achievement :id=model-achievement

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Achievement ID. |
| `name` | string | Title. |
| `desc` | string | Description. |
| `iconUri` | string | Icon URI or empty. |
| `categoryId` | integer | Category ID. |
| `status` | integer | 0 incomplete; 1 completed with unclaimed reward; 2 reward claimed/other completed state. |
| `exp` | integer | EXP reward. |
| `coin` | integer | Coin reward. |
| `coinVariable` | integer | Variable coin reward. |
| `type` | integer | 0 achievement; 1 subcategory heading. |
| `progress` | integer | Stored progress. |
| `order` | integer | Order within category. |
| `itemId` | integer or null | First reward item ID (legacy convenience field). |
| `itemAmount` | integer or null | First reward item quantity. |
| `unlockedTime` | integer or null | Unlock Unix time in milliseconds. |
| `items` | RewardItem[] or null | Complete reward item list; current parser supplies an empty array for absent/malformed item JSON. |
| `color` | string or null | Custom title color. |

Reward items use the shared [RewardItem](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=reward-item "help topic query/task-history-models") model.

### GET /achievement_categories :id=get-achievement-categories

Returns `AchievementCategory[]`, without parameters or pagination. The HTTP server does not expose `/achievement_categories/{id}`.

```json
{"code":200,"message":"success","data":[{"id":1,"name":"Milestones","desc":"","iconUri":"","isAsc":true,"sort":"order","filter":"","order":0,"type":0}]}
```

### AchievementCategory :id=model-achievement-category

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Category ID. |
| `name` | string | Name. |
| `desc` | string | Description. |
| `iconUri` | string | Icon URI/string. |
| `isAsc` | boolean | Ascending sort flag. |
| `sort` | string | Stored sort preference. |
| `filter` | string | Stored grouping/filter preference. |
| `order` | integer | Category order. |
| `type` | integer | 0 user achievements; 1 system achievements. |

### GET /achievement_conditions/{id} :id=get-achievement-conditions

Returns `AchievementCondition[]` for **achievement** ID `id`. A non-integer ID returns an error envelope with code 500. Missing matching conditions produce an empty array. There is no `/achievement_conditions` collection route.

```json
{"code":200,"message":"success","data":[{"id":1,"type":0,"relatedId":42,"target":10,"current":3,"progress":30}]}
```

### AchievementCondition :id=model-achievement-condition

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Condition ID. |
| `type` | integer | Condition code; see below. |
| `relatedId` | integer | Related task/item/skill identifier according to the condition. |
| `target` | integer | Target value. |
| `current` | integer | Current accumulated value. |
| `progress` | integer | Stored progress. |

Condition codes are: 0 task completions; 1 current task streak; 2 legacy attribute level; 3 tomatoes; 4 app usage days; 5 likes; 6 consecutive completion days; 7 current coins; 8 coins gained in one day; 9 task tomatoes; 10 item purchases; 11 item uses; 12 loot-box item gains; 13 skill level; 14 life level; 15 cumulative item gains; 16 synthesis item gains; 17 currently owned item quantity; 18 task focus time; 19 ATM savings; 20 external API. Repeating conditions: 500 task completions; 501 skill level increases; 502 tomatoes; 503 coins; 504 item gains; 505 item purchases; 506 loot-box gains; 507 synthesis gains; 508 item uses; 509 task focus minutes; 510 task tomatoes; 511 likes; 512 ATM savings increases; 513 external API calls; 514 usage days; 515 life level increases; 516 daily coins; 517 consecutive task completions; 518 daily tomatoes; 519 daily focus minutes; 520 daily distinct task groups; 521 daily item uses; 522 daily item purchases; 523 daily completions of a specified task; 524 daily total task completion records. These are raw integer codes, not serialized enum names.
