# Pagination and filtering

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=pagination

Help topic: `query/pagination`

## Pagination and filtering :id=pagination

Pagination is an endpoint capability, not a property of every list. Raw HTTP list responses contain `data: [...]`; they do **not** add `total`, `count` or `hasMore`.

| HTTP endpoint | Pagination | Date range | Other filtering |
| --- | --- | --- | --- |
| `/tasks`, `/tasks/{categoryId}` | No; full array | No | Optional category in the path |
| `/history` | Server-side `offset` / `limit` | No | Optional `gid` |
| `/feelings` | Server-side `offset` / `limit` | No | See [feelings](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-feelings "help topic query/get-feelings") |
| `/pomodoro_records`, `/coin_records`, `/inventory_records`, `/exp_records`, `/step_records` | Server-side `offset` / `limit` | `time_range_start` / `time_range_end` | See the specific endpoint sections below |
| `/statistics` | No; one object | `time_range_start` / `time_range_end` | Aggregate query |
| `/coin`, `/info` | No; one object | No | Singleton query |

For the paged endpoints, HTTP defaults are `offset=0` and `limit=100`. Use non-negative offsets and page sizes in `1..100` for a shared paging loop. The HTTP bridge does not apply MCP's `1..100` clamp, and downstream providers may enforce other limits; consult each endpoint's effective page size and validation notes.

Except for the [time-filtered Pomodoro limitation](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-pomodoro-records "help topic query/get-pomodoro-records"), after each successful page, advance `offset` by the number of records returned. Stop when that number is smaller than the effective page size (the requested `limit` when using `1..100`, excluding time-filtered Pomodoro). Do not mistake a provider-clamped page for the last page. An empty page beyond the end is normal. Fetching multiple pages does not create a snapshot: records can change while the query is in progress.

For the current HTTP SDK, date bounds are forwarded only when **both** parse as integers and `start < end`. A single, unparseable or reversed bound results in an unfiltered query; do not rely on an error to catch this. Validate both bounds before sending. The journal reference states the inclusive boundary behavior of its endpoints.
