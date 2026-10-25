# Authorization and conditional reads

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=auth-cache

Help topic: `query/auth-cache`

## Authorization and conditional reads :id=auth-cache

When a Cloud token is configured, send its exact raw value in `Authorization`; do not prepend `Bearer`. The server also accepts the `token` query parameter, including for clients that cannot set a WebSocket header. A missing or incorrect token produces HTTP `401` and body `code=401`. This check covers the landing page, resources, bridge, files, backup transfer and events.

Successful JSON resource reads attach a strong SHA-256 `ETag` and `Cache-Control: private, no-cache`. This applies to task/history, item/category, skill/group, achievement/condition, feelings, synthesis/category, journal, levels, statistics, info and coin resources. It does not apply to the invocation bridge, file transfer, backup transfer or event streams/polling.

| Request header | Current response behavior |
| --- | --- |
| `If-Match` | A nonmatching version fails with HTTP `412`; evaluated before `If-None-Match`. |
| `If-None-Match` | A matching version returns HTTP `304`, with no JSON body to parse. Reuse the cached successful representation. |

Error envelopes have no resource-version header. Conditional reads are per resource request, not a snapshot or transaction spanning pages or routes.

Cross-origin browser requests can use `If-None-Match`. The current CORS configuration does not allow `If-Match` in preflight, so do not depend on cross-origin browser `If-Match` requests without changing that implementation. Native clients are not subject to browser CORS.
