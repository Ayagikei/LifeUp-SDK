# Broadcasts

LifeUp app Intents (`app.lifeup.*`) are received by **Cloud** (same phone) and exposed two ways:

| Transport | When | MCP tool |
|---|---|---|
| `GET /events` | Always, while Cloud is running | `list_events` (default) |
| `WS /events` | Cloud setting **WebSocket event push** on | `subscribe_events` |

MCP cannot receive Android broadcasts itself.

## Prerequisites

1. LifeUp: `Settings` → `Labs` → `Developer mode` → **Broadcast events**
2. Cloud running. HTTP pull works with no extra Cloud toggle.
3. Optional: Cloud advanced → **WebSocket event push** for `subscribe_events`

## HTTP

`list_events` `{ after, limit }` → `{ latestId, eventWs, events: [{ id, time, action, extras }] }`

`after` is the last `id` you saw. Process restart resets ids.

## WebSocket

`subscribe_events` `{ after }` opens `ws://host:port/events?after=&token=`. Fails with a clear error if `eventWs` is false. `list_events` still works at the same time.

`subscribe_events` `{ on: false }` closes the socket.

## Event names

Same as the LifeUp wiki (`app.lifeup.task.complete`, `feelings.add`, …).
