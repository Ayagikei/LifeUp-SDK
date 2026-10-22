# Task and history queries

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=task-queries

Help topic: `query/task-queries`

## Task and history queries :id=task-queries

Read [common conventions](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=http-query-reference "help topic query-index") and the [complete Task/History model](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=task-history-models "help topic query/task-history-models"). These endpoints require Cloud's Read LifeUp Data permission. Basic list queries are documented for LifeUp v1.91+ with the latest Cloud; examples describe the current SDK fields. Check `/info` when integrating with older installations.

### GET /tasks and /tasks/{categoryId} :id=get-tasks

Query the current **unfinished** task list, optionally restricted to a category/list. The result is a full array, not a server-paged response. Completed, overdue and given-up records belong to `/history`.

| Parameter | Location | Type | Required | Default / behavior |
| --- | --- | --- | --- | --- |
| `categoryId` | Path | integer | Only for `/tasks/{categoryId}` | Omit the entire path segment for the unrestricted task list. |

There are no supported `offset`, `limit`, `gid` or date-range query parameters on these routes. A non-integer category segment currently parses as an absent category; validate it before requesting so you do not accidentally query all tasks.

```text
GET http://{host:port}/tasks
GET http://{host:port}/tasks/1
```

**Response:** `HttpResponse<Task[]>`. All record fields and nested structures are defined in [Task/History fields](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=task-fields "help topic query/task-history-models"). The following invented record shows the complete top-level HTTP shape; `endTime=0` is a placeholder for this route, not a completion timestamp.

```json
{
  "code": 200,
  "message": "success",
  "data": [{
    "id": 101, "gid": 10, "name": "Read a chapter", "notes": "",
    "status": 0, "startTime": 1792108800000, "deadline": 1792195199999,
    "remindTime": 0, "frequency": 1, "weekdays": "", "importance": 3, "difficulty": 2,
    "exp": 5, "skillIds": [2], "coin": 10, "coinVariable": 0,
    "itemId": 0, "itemAmount": 0, "words": "", "categoryId": 1,
    "order": 0, "nameExtended": "Read a chapter", "endTime": 0,
    "items": [], "subTasks": [], "countProgress": null,
    "repeatEndCondition": null
  }]
}
```

**Behavior and errors:** list selection/order follows LifeUp's task-list logic; this endpoint does not promise ordering by completion time. A successful `data: []` means the selected task list returned no records. Permission/provider failures use the [common error wrapper](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=response-errors "help topic query/response-errors"); they are not empty arrays. For completion reporting, use `/history` below rather than this route's `endTime`.

### GET /history :id=get-history

Query visible ended task records, including completed, overdue and given-up tasks. Deleted history is excluded.

| Parameter | Location | Type | Required | Default / behavior |
| --- | --- | --- | --- | --- |
| `offset` | Query | integer | No | `0`; zero-based number of records to skip. |
| `limit` | Query | integer | No | `100`; request a positive page size, preferably `1..100`. |
| `gid` | Query | integer | No | No group filter; if present, selects that task group across cycles. |

The HTTP parser falls back to `0` / `100` for unparseable `offset` / `limit`, and drops an unparseable `gid`. This route does not clamp parsed numbers. Validate non-negative offsets and positive limits yourself. `time_range_start` / `time_range_end` are **not supported**; supplying them does not filter history.

```text
GET http://{host:port}/history?offset=0&limit=100
GET http://{host:port}/history?offset=0&limit=100&gid=10
```

**Response:** `HttpResponse<HistoryRecord[]>`, using the [Task/History wire fields](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=task-fields "help topic query/task-history-models"). `HistoryRecord` is a documentation name for the history view of the SDK's `Task` model. `repeatEndCondition` is `null` in this view; `countProgress` may be present. The saved `endTime` is interpreted with `status`.

```json
{
  "code": 200,
  "message": "success",
  "data": [{
    "id": 100, "gid": 10, "name": "Read a chapter", "notes": "",
    "status": 1, "startTime": 1792022400000, "deadline": 1792108799999,
    "remindTime": 0, "frequency": 1, "weekdays": "", "importance": null, "difficulty": null,
    "exp": 5, "skillIds": [2], "coin": 10, "coinVariable": 0,
    "itemId": 0, "itemAmount": 0, "words": "", "categoryId": 1,
    "order": 0, "nameExtended": "", "endTime": 1792072800000,
    "items": [], "subTasks": [], "countProgress": null,
    "repeatEndCondition": null
  }]
}
```

**Ordering:** saved end date descending, newest first. There is no documented tie-breaker for equal end dates. Pagination is not a snapshot; avoid editing the data while collecting a report, and deduplicate records by `id` when combining pages.

**Pagination and empty results:** use the [shared paging procedure](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=pagination "help topic query/pagination"). A successful empty array means no visible records matched this page/group, including when `offset` is past the end. It does not certify that no activity ever occurred: deleted records are excluded, and there is no completeness flag. If any request fails, report unavailable/incomplete data instead of zero tasks. [Common errors](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=response-errors "help topic query/response-errors") apply.

### Build a daily report :id=daily-report

1. Choose the report's time zone. Calculate the start of the calendar day (`dayStart`) and the start of the following calendar day (`nextDayStart`). Do not assume every local day is exactly 24 hours.
2. Read `/history` with `offset=0&limit=100`, check each response, and collect all pages. Keep any intended `gid` filter consistent.
3. Select `status == 1` and `dayStart <= endTime < nextDayStart`. Use the [recorded-time limitations](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=task-times "help topic query/task-history-models") when describing results.
4. For actual EXP and coin changes, read the [reward journals](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=reward-journals "help topic query/reward-journals") for the same day. Their date filters include both ends, so use `time_range_start=dayStart` and `time_range_end=nextDayStart-1`.
5. Distinguish “no records found in the available data” from a failed/unfinished read. Decide whether the report shows positive rewards only or net changes including reversals, and apply that choice consistently.

Task/history `exp` and `coin` are configured rewards; they are not a reliable substitute for journaled amounts. A completion time edited to another day does not imply that the reward journal's transaction time moved with it.

### Implementation references :id=api-task-queries-sources

[HTTP routes](https://github.com/Ayagikei/LifeUp-SDK/blob/main/http/src/main/java/net/lifeupapp/lifeup/http/service/KtorService.kt) · [Task and history parsing](https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/content/tasks/TasksApi.kt).
