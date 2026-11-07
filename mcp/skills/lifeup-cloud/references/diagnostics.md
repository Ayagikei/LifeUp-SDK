# Diagnostics

Reference for Cloud HTTP `GET /diagnostics` and MCP `get_diagnostics`. Agent workflow and field meanings only—not performance tuning, not proof of root cause, and not proof of how LifeUp applied a mutation.

## Execution context

Cloud runs blocking ContentProvider query and call work on `Dispatchers.IO` (Kotlin coroutine I/O pool). App-level locks and sequential URLs inside `call_api_batch` still apply. MCP uses one HTTP deadline per request (default 30000 ms); set `LIFEUP_TIMEOUT_MS` to 1–120000 ms. Invalid values fail MCP startup. There is no automatic retry on timeout; after a command timeout the write outcome is unknown—verify state before repeating.

## HTTP: `GET /diagnostics`

Same `Authorization` rules as other Cloud APIs when a token is configured (raw token value, not a `Bearer` prefix).

The response is a safe snapshot of LifeUp Cloud process metadata. It does not read Provider catalog rows, `GET /info`, or app databases.

The body is a bounded snapshot plus active and recent record lists (see below). It must not include tokens, raw URL query strings, business payload fields, exception messages, or stack traces.

## MCP: `get_diagnostics`

Optional arguments: `{ host?, token? }` — use when `connect` or `GET /info` failed or is unavailable. Reuse credentials from the user’s existing MCP setup when that already supplies a host (and token if Cloud requires one). Do not ask for a token to paste into a bug report. If the user already uses explicit auth with Cloud, they may pass `token` only for that same configured setup—not as generic report content.

The tool does not require a prior successful `connect`, does not persist credentials, and does not replace the active session.

Local portion of the report (`local` in JSON) includes MCP package version, Node version, effective `requestTimeoutMs`, connection-related flags, and optionally `cachedInfo` — whitelisted `/info` fields when the client last cached them (object field `local.cachedInfo`, not a separate “cached” boolean). It is followed by either a Cloud diagnostic snapshot or a sanitized `cloudError` (unreachable host, HTTP 401, unsupported or old Cloud, etc.).

### Example tool calls

`get_diagnostics` (host only; session credentials apply when already configured):

```json
{ "host": "192.168.1.8:13276" }
```

`help` with the diagnostics topic (exact topic name):

```json
{ "topic": "diagnostics" }
```

`get_diagnostics` returns JSON text; `help` returns Markdown. Neither uploads data or creates attachments or exports.

## Snapshot shape (`schemaVersion` 1)

Diagnostics history is kept only in process memory and is cleared on Cloud process restart. Collect `GET /diagnostics` or `get_diagnostics` before restarting the phone app or Cloud, and before re-running writes you need to correlate—otherwise earlier rows are gone.

| Field | Role |
| --- | --- |
| `schemaVersion` | `1` for this shape |
| Cloud version fields | Cloud build identity as exposed here (not the full `/info` document) |
| `androidApi` | Device API level of the phone running Cloud |
| `executionMode` | `dispatchers_io` — matches the IO-dispatcher lane described above |
| `slowThresholdMs` | `10000` — threshold for the observational `slow` flag, not a cancellation deadline |
| `activeCount`, `activeTruncatedCount` | Counts for in-flight attempts (truncation means the active list may be capped while counts include evicted rows) |
| `active`, `recent` | Bounded lists: up to 64 rows each |

## Record shape (per row)

| Field | Meaning |
| --- | --- |
| `id` | Opaque id for this record |
| `operation` | High-level operation label (`read`, `command`, or redacted) |
| `route` | Route template (no raw query values) |
| `batchSize` | Batch size when applicable |
| `phase` | One of: `dispatching`, `executing`, `completed`, `failed`, `cancelled_before_start` |
| `elapsedMs`, `dispatchMs`, `executionMs` | Timing buckets (observational) |
| `slow` | `true` when `elapsedMs >= 10000` (includes dispatch wait); observational only, not an SLA |
| `errorType` | Exception class name only when present — no message text |

Phase meanings:

- `dispatching` — queued on the IO path before the synchronous Cloud callback region.
- `executing` — inside the Cloud synchronous callback window (including Binder acquisition or wait where applicable). This does not prove which LifeUp lock ran or that the app started handling the work.
- `completed` — no thrown or `Result`-reported exception was observed in the Cloud synchronous callback. It does not prove business success, HTTP response delivery to the client, or commit of a write.
- `failed` — the synchronous callback ended with an error for this attempt. It does not prove that no partial write occurred elsewhere.
- `cancelled_before_start` — recorded when the attempt was cancelled before entering the executing window (for example cancellation before `begin`).

Recording diagnostics does not add a new business concurrency cap; it observes what Cloud already tracks.

## Agent workflow when something fails

1. Stop blind write retries after timeouts or ambiguous errors. A timeout is not proof that work was cancelled.
2. Call `get_diagnostics` once with `{ "host": "<host:port>" }` when `connect` or `/info` is failing or stale. Add `token` only when the user already uses token auth with that host. Do not loop the tool.
3. Ask the user to review and optionally send a short report through their existing developer feedback channel (see template below). They redact whatever they choose; you do not upload, email, file issues, or export files on their behalf.
4. Read phases and timings as observation windows, not guaranteed explanations.
5. Only if needed to verify an uncertain write outcome, perform one focused read. Do not add catalog or list probes merely to test connectivity, or loop them when `/info` already hangs.
6. If diagnostics are unavailable, empty, or the service is unreachable, rely on local MCP context from `get_diagnostics` and label unknowns explicitly.

### Copyable report template (for the user)

The user fills this in voluntarily after redacting sensitive bits. Do not treat it as a request for tokens or full logs.

```
Time (with timezone):
Failing MCP tool name or HTTP path:
Redacted or summarized error (no raw tokens):
LifeUp app / LifeUp Cloud / @lifeup/mcp versions (known or “unknown”):
Phone locked vs app foreground:
Steps to reproduce:
What get_diagnostics (or GET /diagnostics) returned (paste relevant JSON or summary):
```

**Privacy:** Volunteered error text, screenshots, or logs may contain sensitive data. Do not auto-upload, email, open tracker issues, or invent contact URLs. Do not ask for API tokens, full databases, or unreviewed logcat as report content. The user reviews and sends through a channel they already use.

**Help:** The bundled MCP exposes a `help` topic named exactly `diagnostics`. Use it together with this file; behavior matches the installed package.
