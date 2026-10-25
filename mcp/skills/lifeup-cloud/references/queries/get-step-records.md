# Step records

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-step-records

Help topic: `query/get-step-records`

## Step records :id=get-step-records

`GET /step_records` returns `StepRecord[]`. Pagination defaults/clamps and SDK range omission rules match `/inventory_records`. The valid time filter is inclusive on `date`; sorting is `date DESC` without an explicit secondary key.

```json
{"code":200,"message":"success","data":[{"id":1,"date":1700000000000,"dailyStepCount":5000,"totalStepCount":120000,"isGotReward":true,"isUserInput":false,"rewardLevel":1}]}
```

### StepRecord :id=model-step-record

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Record ID. |
| `date` | integer or null | Recorded date as Unix milliseconds. |
| `dailyStepCount` | integer | Daily step count. |
| `totalStepCount` | integer | Stored cumulative step count. |
| `isGotReward` | boolean | Reward claimed flag. |
| `isUserInput` | boolean | User-entered flag. |
| `rewardLevel` | integer or null | Stored reward level. |
