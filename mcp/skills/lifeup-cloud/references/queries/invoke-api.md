# Invoke a URL Scheme API

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=invoke-api

Help topic: `query/invoke-api`

## Invoke a URL Scheme API :id=invoke-api

The [URL Scheme API catalog](https://docs.lifeupapp.fun/en/#/guide/api) defines method names, method parameters, and method-specific results. The HTTP bridge accepts the full `lifeup://api/...` URL.

| `Transport` | Input | Execution and response |
| --- | --- | --- |
| `GET /api` | Repeated URL-encoded query `url` | Starts an Android activity for each URL, in supplied order. Returns the string `success`. Missing URLs also return success. |
| `POST /api` | JSON `RawQueryVo` | Starts every `urls` entry first, then `url` if present. Returns the string `success`. Empty input object also succeeds. |
| `GET /api/contentprovider` | Repeated URL-encoded query `url` | Calls the provider synchronously and returns `CallUrlResult[]` in supplied order. Missing URLs return an error envelope (`No url found`). |
| `POST /api/contentprovider` | JSON `RawQueryVo` | Calls every `urls` entry first, then `url`; returns `CallUrlResult[]`. Empty object returns an empty array. |

Set `Content-Type: application/json` for JSON POSTs. URL-encode the whole URL when embedding it in a GET query, preserving internal `&`, `?`, `+`, and percent encoding. A successful activity bridge response confirms dispatch, not completion of the LifeUp action. ContentProvider methods require Cloud's permission in LifeUp and support depends on the URL Scheme method. Calls are sequential; if a later call throws, earlier effects are not rolled back.

```json
{"urls":["lifeup://api/query?key=coin"],"url":"lifeup://api/query?key=atm"}
```

Activity bridge response:

```json
{"code":200,"message":"success","data":"success"}
```

Provider bridge response (illustrative provider result):

```json
{"code":200,"message":"success","data":[{"url":"lifeup://api/query?key=coin","result":{"value":50,"api_result":true}},{"url":"lifeup://api/query?key=atm","result":{"value":100,"api_result":true}}]}
```

### RawQueryVo :id=model-raw-query

| Field | JSON type | Meaning |
| --- | --- | --- |
| `url` | string or null | Optional single URL. |
| `urls` | string[] or null | Optional URL list; processed before `url`. |

### CallUrlResult :id=model-call-url-result

| Field | JSON type | Meaning |
| --- | --- | --- |
| `url` | string | Submitted URL. |
| `result` | object or null | Converted provider Bundle; null does not itself make the HTTP envelope fail. |

The Bundle conversion preserves Int/Long/Double/String/Boolean values and nested Bundles. Arrays become arrays of strings. Other value types become their `toString()` string (including a Bundle null value becoming the string `"null"`). Method failure fields inside `result` must be checked even when the outer envelope has code 200.

The current LifeUp method handler adds `api_result` (boolean) to completed calls. A caught method failure returns `api_result: false`, `error_code` (string), and `error_message` (string). Error codes include `missing_required_parameter`, `invalid_parameter`, `entity_not_found`, `invalid_format`, `unsupported_api`, `cancelled`, and `internal_error`; individual methods can supply additional codes. During backup restoration the provider can return `error_code: "503"` and `error_message` without `api_result`. Missing API handlers can throw before this method error conversion, producing an outer bridge error instead.
