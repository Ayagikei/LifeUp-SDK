# EXP and coin journal queries

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=reward-journals

Help topic: `query/reward-journals`

## EXP and coin journal queries :id=reward-journals

These read-only HTTP endpoints require Read LifeUp Data permission, LifeUp **1.106.0+** and LifeUp Cloud **3.0.0+**. They return recorded changes, including decreases and reversals, rather than task reward configuration. Read [common conventions](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=http-query-reference "help topic query-index") first.

### Request parameters for both endpoints :id=journal-parameters

The following contract applies specifically to **both** `GET /exp_records` and `GET /coin_records`.

| Parameter | Location | Type | Required | Default / actual behavior |
| --- | --- | --- | --- | --- |
| `offset` | Query | integer | No | HTTP default `0`; provider clamps negative values to `0`. |
| `limit` | Query | integer | No | HTTP default `100`; provider clamps to `1..200`. Use `1..100` for the shared paging procedure. |
| `time_range_start` | Query | integer (Unix milliseconds) | No, or paired | Inclusive lower bound of the transaction's recorded time. |
| `time_range_end` | Query | integer (Unix milliseconds) | No, or paired | Inclusive upper bound; valid filtering requires `start < end`. |

Unparseable or out-of-range pagination values use HTTP defaults. Unparseable date values become absent. The current SDK forwards a date range only when both bounds exist and `start < end`; otherwise it queries **without a date filter**. Direct raw ContentProvider calls have different invalid-range validation. Validate bounds in the HTTP caller.

Both endpoints sort by recorded transaction time descending, newest first, and exclude deleted journal entries. They do not promise an ID tie-breaker or a multi-page snapshot. Follow [pagination](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=pagination "help topic query/pagination"), taking the provider's effective limit into account. A successful empty array means no entries matched the selected range/page; a failed read is not zero activity. Neither endpoint provides a completeness flag for deleted or unavailable data.

### GET /exp_records :id=get-exp-records

Query EXP changes. Parameters are exactly the [four journal parameters above](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=journal-parameters "help topic query/reward-journals"); there is no `gid` filter.

```text
GET http://{host:port}/exp_records?offset=0&limit=100&time_range_start=1792022400000&time_range_end=1792108799999
```

**Response:** `HttpResponse<ExpRecord[]>`. Current serialization includes all listed fields, including nullable values.

| Field | JSON type | Meaning / parser fallback |
| --- | --- | --- |
| `id` | integer or `null` | Journal entry ID. |
| `time` | integer or `null` | Transaction time in Unix milliseconds, distinct from an editable task completion time. |
| `value` | integer | EXP change per associated attribute; fallback `0`. Use `isDecrease` for direction. |
| `isDecrease` | boolean | `true` for a decrease, `false` for an increase; parser treats provider integer `1` as true. |
| `totalValue` | integer | Recorded aggregate EXP change, **not an EXP balance**; fallback `0`. Multi-attribute batches can overlap in this stored total; see [report totals](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=journal-totals "help topic query/reward-journals"). |
| `resCode` | integer | EXP-specific reason code; fallback `0`. |
| `relatedId` | integer or `null` | Related source-object ID. For normal task completion this is the task record `id`, not its `gid`. Other reasons can refer to other resources. |
| `content` | string or `null` | Change description. |
| `skillIds` | integer array | Associated attribute/skill IDs; fallback empty array. |
| `relatedAttributes` | string array | Associated legacy attribute strings; fallback empty array. |

Invented example: 10 EXP applied to each of two attributes, 20 EXP in aggregate.

```json
{
  "code":200,"message":"success","data":[{
    "id":301,"time":1792072800000,"value":10,"isDecrease":false,
    "totalValue":20,"resCode":1,"relatedId":100,"content":"Task completed",
    "skillIds":[2,3],"relatedAttributes":[]
  }]
}
```

**Errors:** [common HTTP/provider errors](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=response-errors "help topic query/response-errors"). Invalid HTTP date bounds may silently remove filtering as explained above, rather than produce an error.

### GET /coin_records :id=get-coin-records

Query coin changes. Parameters are exactly the [four journal parameters above](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=journal-parameters "help topic query/reward-journals"); there is no `gid` filter.

```text
GET http://{host:port}/coin_records?offset=0&limit=100&time_range_start=1792022400000&time_range_end=1792108799999
```

**Response:** `HttpResponse<CoinRecord[]>`. Current serialization includes all listed fields, including nullable values.

| Field | JSON type | Meaning / parser fallback |
| --- | --- | --- |
| `id` | integer or `null` | Journal entry ID. |
| `time` | integer or `null` | Transaction time in Unix milliseconds. |
| `value` | integer | Coin change amount; fallback `0`. Use `isDecrease` for direction. |
| `isDecrease` | boolean | `true` for a decrease, `false` for an increase; parser treats provider integer `1` as true. |
| `totalValue` | integer | Coin balance after the operation; fallback `0`. Unlike EXP `totalValue`, this is a balance. |
| `resCode` | integer | Coin-specific reason code; fallback `0`. |
| `relatedId` | integer or `null` | Related source-object ID; normal task completion uses the task record `id`. Interpret other sources using their reason code. |
| `content` | string or `null` | Change description. |
| `savingBalance` | integer or `null` | Savings balance recorded for the operation. |

Invented example: a decrease of 5 coins, leaving a coin balance of 95.

```json
{
  "code":200,"message":"success","data":[{
    "id":401,"time":1792072800000,"value":5,"isDecrease":true,
    "totalValue":95,"resCode":0,"relatedId":null,"content":"Example purchase",
    "savingBalance":0
  }]
}
```

**Errors:** [common HTTP/provider errors](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=response-errors "help topic query/response-errors"). Invalid HTTP date bounds may silently remove filtering as explained above, rather than produce an error.

### Reason codes and report totals :id=journal-totals

Reason-code domains differ. Selected examples:

| Reason | EXP `resCode` | Coin `resCode` |
| --- | --- | --- |
| Finish task | `1` | `2` |
| Undo finish task | `101` | `3` |
| Finish subtask | `12` | `11` |
| Undo subtask | `106` | `12` |
| API change | `200` | `28` |

Full EXP codes: `0` unknown; `1` task completion; `2` achievement; `3` like exchange; `4` usage-day streak; `5` step exchange; `6` set to completed; `7` undo give-up; `8` undo overdue; `9` eat tomato; `10` use item; `11` user achievement unlock; `12` subtask completion; `200` API; `101` undo completion; `102` give-up; `103` overdue; `104` item use decreases EXP; `105` user achievement relock; `106` undo subtask completion. Coin reason codes use the same domain as [inventory records](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-inventory-records "help topic query/get-inventory-records"). Preserve unknown future codes. For a net change, apply direction with `isDecrease` (`true` subtracts the recorded amount). Do not assume a decrease is encoded as a negative `value`; the numeric types are signed and no unsigned/absolute-value guarantee is declared.

EXP `value` is per attribute. Do not blindly sum EXP `totalValue` across records: the current multi-attribute batching logic can store overlapping totals when more than three attributes are involved. For example, a four-attribute change of 10 EXP each can produce stored totals of 40 and 10, rather than a safe sum of 40. Reconcile per-attribute `value` with the attributes listed on each record for precise reporting; missing associations limit what can be established. Do not add `value` and `totalValue` together, and do not describe EXP `totalValue` as a balance.

For one calendar day, calculate local `dayStart` and `nextDayStart`, then request `[dayStart, nextDayStart - 1]` because both bounds are inclusive. Select reason codes when reporting task rewards only; leave them unfiltered when reporting all recorded changes. [Daily-report procedure](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=daily-report "help topic query/task-queries").

### Implementation references :id=api-ledgers-sources

[CoinRecord](https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/content/records/CoinRecord.kt) · [ExpRecord](https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/content/records/ExpRecord.kt) · [Date-parameter forwarding](https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/content/records/LedgerUri.kt).
