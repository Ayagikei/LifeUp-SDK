# Task and history data models

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=task-history-models

Help topic: `query/task-history-models`

## Task and history data models :id=task-history-models

This page defines the **HTTP JSON** returned by [task/history queries](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=task-queries "help topic query/task-queries"). Android Cursor columns and URL Scheme action parameters can have different names and representations; see [common conventions](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=representations "help topic query/representations").

The SDK uses one serialized `Task` class for two views. Here, `Task` names the `/tasks` view and `HistoryRecord` names the `/history` view. These names describe existing output; they do not introduce another API. Current Cloud serialization includes default values and `null` fields.

### Task / HistoryRecord fields :id=task-fields

All fields below appear in the current HTTP object. Missing provider values are often converted into the listed fallbacks; a fallback does not prove that the source data contained that value.

| Field | JSON type | Meaning / fallback |
| --- | --- | --- |
| `id` | integer or `null` | ID of this task/cycle. |
| `gid` | integer or `null` | Task-group ID shared across repeating cycles. |
| `name` | string | Task name. A missing provider name becomes `ERROR: name is null`. |
| `notes` | string | Notes; empty string if absent. |
| `status` | integer | Task status; missing value defaults to `0`. See the status table below. |
| `startTime` | integer | Configured start time in milliseconds; SDK fallback `0`. |
| `deadline` | integer | Due time in milliseconds; SDK fallback `0` if absent. |
| `remindTime` | integer | Reminder time in milliseconds; SDK fallback `0` if absent. |
| `frequency` | integer | Recurrence code; `0` once, `1` daily, `-1` unlimited. Other recurrence modes are described in [task actions](https://docs.lifeupapp.fun/en/#/guide/api). SDK fallback `0`. |
| `weekdays` | string | Weekday selection, e.g. `1,3,5` (Monday `1`, Sunday `7`); empty when not supplied, and a fixed empty placeholder in history. |
| `importance` | integer or `null` | Importance level `1`–`4` (ascending); corresponds to the App task urgency degree (`taskUrgencyDegree`). Returned on `GET /tasks` and `GET /tasks/{categoryId}` when the task-list ContentProvider exposes the column; SDK parses a missing or NULL column as `null`. The history ContentProvider does not return this column, so `/history` uses `null` here without implying the App lacks stored urgency on the record. |
| `difficulty` | integer or `null` | Difficulty level `1`–`4` (ascending); corresponds to the App task difficulty degree (`taskDifficultyDegree`). Same `/tasks` versus `/history` ContentProvider behavior as `importance`. |
| `exp` | integer | Configured EXP reward, not a transaction amount; default `0`. |
| `skillIds` | integer array | Attributes associated with the reward; empty array if unavailable. |
| `coin` | integer | Configured coin reward; default `0`. |
| `coinVariable` | integer | Random-coin reward setting; default `0`. |
| `itemId` | integer | Legacy first reward-item ID; default `0`. Use `items` for the full list. |
| `itemAmount` | integer | Legacy first reward-item quantity; default `0`. This HTTP name differs from the provider's `itemCount` column. |
| `words` | string | Completion incentive text; empty if absent. |
| `categoryId` | integer | Task category/list ID; default `0` if absent. |
| `order` | integer | Task-list ordering value; fixed `0` placeholder in history because that provider does not return the column. |
| `nameExtended` | string | Extended display name; fixed empty placeholder in history because that provider does not return the column. |
| `endTime` | integer | Recorded end time for history. `/tasks` fills this with `0`; see [time semantics](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=task-times "help topic query/task-history-models"). |
| `items` | `RewardItem[]` | Item rewards; empty array if unavailable. |
| `subTasks` | `SubTask[]` | Nested subtasks; empty array if unavailable. |
| `countProgress` | `TaskCountProgress` or `null` | Count-progress snapshot when available; can appear in both views. |
| `repeatEndCondition` | `TaskRepeatEndCondition` or `null` | Repetition end rule in `/tasks`; always `null` in `/history`. |

### Task status and timestamps :id=task-times

| `status` | Meaning | History `endTime` meaning |
| --- | --- | --- |
| `0` | Unfinished | Not a completion time; `/history` selects ended records instead. |
| `1` | Completed | Recorded completion time. |
| `2` | Overdue | Recorded overdue time. |
| `3` | Given up | Recorded give-up time. |

| Time field | Definition |
| --- | --- |
| `startTime` | The task's configured/scheduled start time, not when the user actually started working. |
| `deadline` | The scheduled due time, separate from the recorded end time. |
| `endTime` | The recorded time the history task ended; interpret with `status` as above. |

All three use Unix milliseconds. For normal completion, the app records the current time. `history_operation` with `operation=edit_completed_time` takes a `completed_time` input and edits this same saved completion time; reading history still returns the field name `endTime`.

Saved times can be edited. If an ended record has no saved end date, the app falls back to the current time. Consequently, `endTime` is the currently recorded time, not an immutable audit timestamp. `0` on `/tasks` is only the SDK placeholder, not a real event at the Unix epoch.

### RewardItem :id=reward-item

| Field | JSON type | Meaning |
| --- | --- | --- |
| `itemId` | integer | Reward item ID. |
| `itemCount` | integer | Reward quantity. |

```json
{"itemId":7,"itemCount":1}
```

### SubTask :id=subtask

| Field | JSON type | Meaning / default |
| --- | --- | --- |
| `id` | integer | Subtask ID. |
| `gid` | integer | Subtask-group ID. |
| `todo` | string | Subtask text. |
| `status` | integer | `0` unfinished, `1` completed. |
| `remindTime` | integer or `null` | Reminder timestamp in milliseconds; `null` if unavailable. |
| `endTime` | integer or `null` | Subtask completion timestamp in milliseconds; `null` if unavailable. |
| `exp` | integer | Configured EXP reward. |
| `coin` | integer or `null` | Configured coin reward. |
| `coinVariable` | integer or `null` | Random-coin reward setting. |
| `items` | `RewardItem[]` | Item rewards; default empty array. |
| `order` | integer or `null` | Ordering value; model default `0`. |
| `autoUseItem` | boolean or `null` | Whether rewards are automatically used; model default `false`. |

```json
{
  "id":201,"gid":20,"todo":"Read ten pages","status":1,
  "remindTime":null,"endTime":1792072800000,"exp":2,
  "coin":0,"coinVariable":0,"items":[{"itemId":7,"itemCount":1}],
  "order":0,"autoUseItem":false
}
```

### TaskCountProgress :id=count-progress

| Field | JSON type | Meaning |
| --- | --- | --- |
| `currentCount` | integer | Current count for this task/cycle. |
| `targetCount` | integer | Target count for this task/cycle. |

```json
{"currentCount":2,"targetCount":5}
```

### TaskRepeatEndCondition :id=repeat-end-condition

| Field | JSON type | Meaning / default |
| --- | --- | --- |
| `mode` | string | `COUNT` or `DATE`. These are read-side strings, not the numeric action parameters. |
| `behavior` | string | `TERMINATE` or `FREEZE`. |
| `targetCycleCount` | integer or `null` | Cycle-count target for `COUNT`; default `null`. |
| `endDateMillis` | integer or `null` | End-date timestamp in milliseconds for `DATE`; default `null`. |
| `inclusive` | boolean | Inclusive end-date flag; default `false`. |

```json
{"mode":"COUNT","behavior":"TERMINATE","targetCycleCount":10,"endDateMillis":null,"inclusive":false}
```

### Reward amounts :id=reward-amounts

These task and subtask fields describe configured rewards. Multipliers, random coins and settlement rules can change the actual award. Use the [EXP and coin journals](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=reward-journals "help topic query/reward-journals") for transaction amounts, and distinguish positive rewards from net changes that include reversals or penalties.

### Implementation references :id=api-task-model-sources

[Task](https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/content/tasks/Task.kt) · [Parser](https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/content/tasks/TasksApi.kt) · [SubTask](https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/content/tasks/SubTask.kt) · [RewardItem](https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/content/common/RewardItem.kt) · [Count progress](https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/content/tasks/TaskCountProgress.kt) · [Repeat rule](https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/content/tasks/TaskRepeatEndCondition.kt).
