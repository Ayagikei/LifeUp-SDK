# Statistics

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-statistics

Help topic: `query/get-statistics`

## Statistics :id=get-statistics

`GET /statistics` returns one `Statistics` object. Optional `time_range_start` and `time_range_end` use Unix milliseconds and the same SDK forwarding rule: both valid and start < end, otherwise no range is sent. Without a range, the app uses its first recorded timestamp through now; if none exists, it uses the last 30 days. `usingDays` and `companionDays` are lifetime values even when a range is provided. The other metrics are computed by their respective app statistics methods; do not assume that all underlying metric queries share identical endpoint inclusion rules.

```json
{"code":200,"message":"success","data":{"start":1700000000000,"end":1700086400000,"tomatoes":4,"pomodoroMinutes":100,"focusCount":4,"tasksCompleted":3,"tasksTotal":4,"taskCompleteRate":0.75,"feelingsCount":1,"usingDays":30,"companionDays":32,"interestChanged":2,"coinPurchaseConsumed":10}}
```

### Statistics :id=model-statistics

| Field | JSON type | Meaning |
| --- | --- | --- |
| `start` | integer or null | Effective range start in Unix milliseconds. |
| `end` | integer or null | Effective range end in Unix milliseconds. |
| `tomatoes` | integer | Earned tomato statistic. |
| `pomodoroMinutes` | integer | Focus duration in minutes. |
| `focusCount` | integer | Focus session count. |
| `tasksCompleted` | integer | Completed task count used by the completion statistic. |
| `tasksTotal` | integer | Total task count used by that statistic. |
| `taskCompleteRate` | number | Completion ratio. |
| `feelingsCount` | integer | Feeling count. |
| `usingDays` | integer | Lifetime usage days. |
| `companionDays` | integer | Lifetime companion days. |
| `interestChanged` | integer | Interest coin change statistic. |
| `coinPurchaseConsumed` | integer | Purchase coin consumption statistic. |
