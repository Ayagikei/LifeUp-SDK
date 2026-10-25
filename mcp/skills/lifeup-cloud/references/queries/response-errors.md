# Response and errors

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=response-errors

Help topic: `query/response-errors`

## Response and errors :id=response-errors

The common HTTP JSON wrapper is `HttpResponse<T>`:

| Field | Type | Meaning |
| --- | --- | --- |
| `code` | integer | `200` for a successful query; a non-`200` value indicates an error. This is a body field, separate from HTTP status. |
| `message` | string | Usually `success` on success; an error description otherwise. |
| `data` | `T` or `null` | An endpoint-specific value (array, object or string) on success; `null` on error. |

Example of a successful empty list:

```json
{"code":200,"message":"success","data":[]}
```

Illustrative permission/provider-access error:

```json
{"code":10001,"message":"Cursor is null, check if LifeUp is installed and running and you have granted the permission","data":null}
```

| Signal | How to handle it |
| --- | --- |
| HTTP `401` | Supply the configured Cloud token as the raw `Authorization` header value. Do not add `Bearer`. |
| Body `code = 10001` | Check that LifeUp is installed/running and Cloud has Read LifeUp Data permission. |
| Body `code = 10002` | ContentProvider query failed; inspect the error message. |
| Other non-success status/code | Treat the query as failed, not as an empty result. |

Check **both** HTTP status and body `code`. Some route failures still use HTTP `200` with an error code in the body. Actions through `/api/contentprovider` additionally require checking each `data[].result.api_result`; the wrapper's `code=200` does not prove that every action succeeded.
