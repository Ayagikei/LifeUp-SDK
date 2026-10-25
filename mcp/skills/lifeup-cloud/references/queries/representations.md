# Transport and representation

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=representations

Help topic: `query/representations`

## Transport and representation :id=representations

| Interface | Representation | Important difference |
| --- | --- | --- |
| Cloud HTTP queries | JSON wrapped as `{code, message, data}` | The endpoint references below describe this public JSON. |
| Android ContentProvider | Cursor columns, such as `_ID` / `_GID`, sometimes containing JSON text | SDK parsing converts these into HTTP fields such as `id` / `gid` and nested arrays. Do not use Cursor column definitions as the HTTP schema. |
| URL Scheme actions | `lifeup://api/{method}` and method-specific parameters/results | Write parameters can use snake_case even when HTTP read fields use camelCase. |
| MCP list tools | Tool-specific envelope and compact or detailed records | MCP pagination and output fields are additional client behavior, not HTTP response fields. |
