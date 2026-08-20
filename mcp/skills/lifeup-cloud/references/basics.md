# Basics

Condensed rules for agents. Do not hand-build `lifeup://` strings.

## Connect

Phone: LifeUp **1.106.0+** + LifeUp Cloud **3.0.0+** + **Read LifeUp Data**. Same LAN. Default port `13276`. Older builds: `status.update` tells the agent to ask the user to update.

1. `discover` — auto-connects when exactly one Cloud is found (mDNS `_lifeup._tcp`, TXT `port` is HTTP)
2. `connect` `{ host }` if 0 or >1 instances
Token only if Cloud set one. Header is the **raw** token, not `Bearer`. `LIFEUP_TOKEN` is process-only. Saved token is bound to `{host,port}` and is not sent to a different discovered host.

## Flaky Cloud

Typical failures:

| Signal | Meaning | What to do |
|---|---|---|
| discover empty | Cloud off / different Wi‑Fi / AP isolation | Ask user to start Cloud; or take `host:port` |
| connect timeout / NetworkError | phone sleep, Wi‑Fi drop | `connect` once more; then ask user |
| `token required or invalid` (HTTP 401) | Cloud has a token, or the token is wrong | ask user; `connect` `{ token }` |
| `LifeUp is not running, or Read LifeUp Data is not granted` (code 10001) | LifeUp closed, or Cloud has no read permission | open LifeUp / grant in Cloud |
| `Cloud timed out (10s)` | phone sleep / Wi‑Fi drop | one `connect`, then ask user |
| Non-JSON response | wrong host or captive portal | check `host:port` |
| later calls fail after success | phone slept | `status` then `connect` again |

Do not tight-loop discover. One retry is enough. Mutations are not idempotent (`complete`, `skip`, `reward`) — do not retry a write unless the user asked.


## Encoding (MCP does it)

Pass **raw** values in tool `params`. `call_api` / typed tools encode query values.

- Do **not** percent-encode yourself (`%20`, `%26`, …). MCP uses `%20` for spaces (not `+`). `#` becomes `%23`.
- Do **not** paste a finished `lifeup://api/...?` string into `call_api` unless the user already gave that exact URL; prefer `{ method, params }`.
- Nested APIs (`random.api`, `confirm_dialog.*_action`): pass the inner URL as a **plain** `lifeup://api/...` string in `params`. MCP encodes it once.
- Wiki examples often show **already-encoded** inner URLs (`lifeup:%2F%2Fapi%2Ftoast%3F...`). Do not copy that form into `params`.
- `#` in colors: pass `#66CCFF` raw.
- Arrays: pass JSON arrays in params (`skills: [1,2]`); MCP repeats the key.

## JSON fields

Wiki JSON blobs (`items`, `effects`, `purchase_limit`, `conditions_json`, `sort_json`, `inputItems`, `outputItems`) are **strings** in the URL. Pass the JSON text as one param value; MCP encodes the string.

Minimal shapes:

```json
[{"item_id":1,"amount":2}]
```

`purchase_limit`: `[{ "limitType": 0, "limitNumber": 5 }]`.  
`effects`: `[{ "type": 2, "info": { "min": 100, "max": 200 } }]`.  
Full type tables: `help` for `add_item` / `achievement` when you actually need them.

`extendInfo` inside `purchase_limit` is itself a JSON string.

## Two ways to run an API

Cloud has two HTTP entry points. MCP `via` picks one.

| via | Cloud | What happens | Result |
|---|---|---|---|
| `contentprovider` (default) | `POST /api/contentprovider` | LifeUp API ContentProvider, stays in background | `{ url, result }` per call. **Use this** when you need ids. |
| `launch` | `POST /api` | Starts a LifeUp Activity (URL scheme) | Transport OK only. **No payload.** |

Use `launch` when you need UI: `goto`, `confirm_dialog`, `toast` with a visible banner, or `pomodoro_timer` start if CP returns `background_start_not_allowed` (Android 12+ needs battery-optimization ignore for CP timer start).

Do not use `launch` if you need `task_id` / `itemId` back.

## Batch

`call_api_batch` sends up to 20 methods in one request (`urls: [...]`). Order is kept. Same `via` for the whole batch. Any destructive method still needs `confirm: true` (on that call or the batch).

Later calls cannot see earlier results inside the same batch. If you need a new `task_id`, add first, read `calls[].data`, then call again.

## Errors vs success

Cloud envelope: `{ code, message, data }`. MCP mutation tools return `{ via, calls: [{ url, method, ok, data, error? }] }`.

- HTTP 200 + `code=200` = transport OK, not “the API did the thing”.
- Always read `ok` and **`data` (full return)**. Not just ids.
- `void` APIs (`complete`, `toast`, `reward`, …): empty bundle is OK; `ok=true`.
- Payload APIs (`add_task`, `query`, `tomato`, …): `data` is the wiki return table (`task_id`, `value`, `tomatoes`, `backup_file_uri`, …). Empty = fail.
- Code APIs: `data.result` is a **status code**. Success sets differ: `purchase_item` 0 or 4; `use_item` / `synthesize` only 0 (`use_item` 4 = countdown conflict, fail).
- `deposit`/`withdraw`: `data.result` is boolean.
- `pomodoro_timer`: `ok` from `api_result`; `data` has timer state (`applied`, `state`, millis, …).
- JSON-looking strings inside `data` are parsed (e.g. `skillIds`).


## IDs and names

Enable LifeUp 实验 → 开发者模式 to see ids.

- Task `id` changes each repeat cycle; `gid` does not.
- Name match: exact case-insensitive first, then substring. Prefer id when names collide.
- Provide exactly one selector when the wiki says `否*` (`id` | `gid` | `name`).
- `order` is only relative; do not change one item’s `order` alone.

