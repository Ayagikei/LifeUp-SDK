# Query reference index

Catalog only. Do **not** read every section file.
Need HTTP request parameters or a response model? `help` with `topic` = `query/<id>` (e.g. `query/task-history-models`, `query/task-queries`).
This file is `help` topic `query-index`.
Pass the topic cell exactly. Nested anchors are headings inside that file, not separate help topics.
Rewritten links use a public docs URL. The link title is the `help` topic to open.
Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=http-query-reference

Document anchor `http-query-reference` is this file (`query-index`), not `query/http-query-reference`.

# HTTP API reference :id=http-query-reference

Use this reference for LifeUp Cloud request parameters and complete HTTP response models. For installation, authorization and connection steps, start with [LifeUp Cloud](https://docs.lifeupapp.fun/en/#/guide/api_cloud). Newer journal endpoints require LifeUp **1.106.0+** and LifeUp Cloud **3.0.0+**; use `/info` to identify installed versions. This reference describes current source contracts, not proof of a particular release's availability.

| topic | section | nested anchors |
| --- | --- | --- |
| `query/interface-index` | Interface index | - |
| `query/representations` | Transport and representation | - |
| `query/response-errors` | Response and errors | - |
| `query/auth-cache` | Authorization and conditional reads | - |
| `query/data-conventions` | Shared data conventions | - |
| `query/pagination` | Pagination and filtering | - |
| `query/mcp-differences` | MCP differences | - |
| `query/api-common-sources` | Sources and version scope | - |
| `query/task-history-models` | Task and history data models | `task-fields`, `task-times`, `reward-item`, `subtask`, `count-progress`, `repeat-end-condition`, `reward-amounts`, `api-task-model-sources` |
| `query/task-queries` | Task and history queries | `get-tasks`, `get-history`, `daily-report`, `api-task-queries-sources` |
| `query/reward-journals` | EXP and coin journal queries | `journal-parameters`, `get-exp-records`, `get-coin-records`, `journal-totals`, `api-ledgers-sources` |
| `query/get-items` | Items and categories | `model-shop-item`, `get-items-categories`, `model-shop-category`, `get-tasks-categories`, `model-task-category` |
| `query/get-skills` | Skills and skill groups | `model-skill`, `get-skill-groups`, `model-skill-group` |
| `query/get-achievements` | Achievements | `model-achievement`, `get-achievement-categories`, `model-achievement-category`, `get-achievement-conditions`, `model-achievement-condition` |
| `query/get-synthesis` | Synthesis | `model-synthesis`, `model-synthesis-item-info`, `get-synthesis-categories`, `model-synthesis-category` |
| `query/get-feelings` | Feelings | `model-feelings` |
| `query/get-pomodoro-records` | Pomodoro records | `model-pomodoro-record` |
| `query/get-inventory-records` | Inventory records | `model-inventory-record` |
| `query/get-step-records` | Step records | `model-step-record` |
| `query/get-level-defines` | Level definitions | `model-level-defines`, `model-level-define` |
| `query/get-statistics` | Statistics | `model-statistics` |
| `query/get-info` | App information and coin balance | `model-info`, `get-coin` |
| `query/invoke-api` | Invoke a URL Scheme API | `model-raw-query`, `model-call-url-result` |
| `query/files-download` | Read and upload files | `files-get`, `files-upload` |
| `query/files-backup` | Backup export and import | `files-export`, `files-import` |
| `query/get-events` | Events | `get-events-poll`, `model-events-page`, `model-cloud-event`, `get-events-websocket` |
