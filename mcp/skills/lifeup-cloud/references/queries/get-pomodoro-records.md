# Pomodoro records

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-pomodoro-records

Help topic: `query/get-pomodoro-records`

## Pomodoro records :id=get-pomodoro-records

`GET /pomodoro_records` returns `PomodoroRecord[]`. `offset` defaults to 0, `limit` to 100; no explicit clamp. Optional `time_range_start` and `time_range_end` are Unix milliseconds. The SDK forwards the range only when both parse as 64-bit integers and start is less than end; otherwise the range is silently omitted.

Without a range, completed, nondeleted records with `startTime != endTime` and positive `endTime` are returned in `endTime DESC, startTime DESC, id DESC` order. A valid range selects `start < endTime <= end` and excludes deleted rows.

> [!WARNING]
> Earlier implementations passed `limit` and `offset` to the DAO in reversed positions for date-range queries and omitted ordering. The local LifeUp source has been corrected to pass named arguments and sort paginated results by `endTime DESC, startTime DESC, id DESC`; this local fix has not been released. Until you confirm that your installed LifeUp build includes it, omit the range and filter returned rows in the client for reliable pagination.

```json
{"code":200,"message":"success","data":[{"id":1,"startTime":1700000000000,"endTime":1700001500000,"duration":1500000,"reward":1.0}]}
```

### PomodoroRecord :id=model-pomodoro-record

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Record ID. |
| `startTime` | integer or null | Start Unix time in milliseconds. |
| `endTime` | integer or null | End Unix time in milliseconds. |
| `duration` | integer | Milliseconds: stored nonzero `lastTime`, otherwise end minus start. |
| `reward` | number | Earned tomato amount: abandoned = 0; half = 0.5 × multiplier; otherwise multiplier. |
