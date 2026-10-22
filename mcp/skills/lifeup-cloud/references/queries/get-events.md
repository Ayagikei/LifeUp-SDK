# Events

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-events

Help topic: `query/get-events`

## Events :id=get-events

### GET /events :id=get-events-poll

Returns `EventsPage`. `after` is an exclusive event ID cursor (64-bit integer, default 0). `limit` is an integer defaulting to 50 and clamped to 1–200. Non-integer values use defaults. Events are oldest first within the retained buffer, with `id > after`. At most 200 events are retained in memory; older events are dropped and process restarts reset IDs. `latestId` is the latest available global ID, not necessarily the last event in a limited page. Continue from the last event actually processed to avoid skipping a backlog. No durable history or delivery acknowledgement is provided.

```json
{"code":200,"message":"success","data":{"latestId":2,"eventWs":true,"events":[{"id":1,"time":1700000000000,"action":"app.lifeup.task.complete","extras":{"id":"42"}}],"broadcasts":true}}
```

### EventsPage :id=model-events-page

| Field | JSON type | Meaning |
| --- | --- | --- |
| `latestId` | integer | Current latest event ID. |
| `eventWs` | boolean | Cloud's WebSocket setting. |
| `events` | CloudEvent[] | Retained events after the cursor. |
| `broadcasts` | boolean or null | Whether LifeUp broadcasts are enabled; null means unknown or unsupported. Query status is cached for five seconds. |

### CloudEvent :id=model-cloud-event

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer | In-memory event sequence ID. |
| `time` | integer | Cloud reception Unix time in milliseconds. |
| `action` | string | Android broadcast action. |
| `extras` | object of string values | Broadcast extras converted with `toString()`; null becomes an empty string. Keys depend on the action. |

### WebSocket /events :id=get-events-websocket

When `eventWs` is true, connect to `ws://HOST:PORT/events?after=ID` (and supply the configured token). The server replays retained events after `after` and then sends live events as individual JSON `CloudEvent` text frames, **without** the HTTP response envelope. The replay is limited by the 200-event memory buffer; there is no `limit` WebSocket parameter. The server sends Ping frames every 30 seconds. Reconnect with the last event ID actually processed. When disabled, the WebSocket route is not registered; HTTP polling remains available.

```json
{"id":2,"time":1700000001000,"action":"app.lifeup.item.use","extras":{"id":"3"}}
```

Supported action subscriptions are `app.lifeup.task.complete`, `app.lifeup.task.overdue`, `app.lifeup.task.giveup`, `app.lifeup.achievement.unlock`, `app.lifeup.achievement.condition.unlock`, `app.lifeup.item.purchase`, `app.lifeup.item.reward`, `app.lifeup.item.use`, `app.lifeup.synthesis.complete`, `app.lifeup.pomodoro.start`, `app.lifeup.pomodoro.pause`, `app.lifeup.pomodoro.complete`, `app.lifeup.pomodoro.stop`, `app.lifeup.feelings.add`, `app.lifeup.level.up`, `app.lifeup.level.down`, `app.lifeup.item.countdown.start`, `app.lifeup.item.countdown.complete`, `app.lifeup.item.countdown.stop`, `app.lifeup.timing.start`, `app.lifeup.timing.complete`, `app.lifeup.timing.pause`, and `app.lifeup.timing.abandon`. Event collection requires LifeUp broadcasts to be enabled. Polling `/events` checks that setting; it does not enable it automatically.
