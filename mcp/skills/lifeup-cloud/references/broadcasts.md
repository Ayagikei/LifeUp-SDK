# Broadcasts

LifeUp app Intents (`app.lifeup.*`) are received by **Cloud** (same phone) and exposed two ways:

| Transport | When | MCP tool |
|---|---|---|
| `GET /events` | Always, while Cloud is running | `list_events` (default) |
| `WS /events` | Cloud **WebSocket event push** (on by default) | `subscribe_events` |

MCP cannot receive Android broadcasts itself.

## Prerequisites

1. LifeUp: `Settings` → `Labs` → `Developer mode` → **Broadcast events** (default off). Cloud Advanced can show the status and enable it in one tap.
2. Cloud running. HTTP pull always works.
3. Cloud **WebSocket event push** is on by default (3.0.0+). That switch is transport only; it does not turn on LifeUp broadcasts.

## HTTP

`list_events` `{ after, limit }` → `{ latestId, eventWs, broadcasts?, events: [{ id, time, action, extras }] }`. `broadcasts` is LifeUp's source switch; omitted if Cloud cannot read it (old LifeUp / query failed).

`after` is the last `id` you saw. Process restart resets ids.

## WebSocket

`subscribe_events` `{ after }` opens `ws://host:port/events?after=&token=`. Fails with a clear error if `eventWs` is false. `list_events` still works at the same time.

`subscribe_events` `{ on: false }` closes the socket.

## Event names

Same as the LifeUp wiki (`app.lifeup.task.complete`, `feelings.add`, …).
