# App information and coin balance

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-info

Help topic: `query/get-info`

## App information and coin balance :id=get-info

`GET /info` returns `Info`. LifeUp Cloud adds its own installed version to the app/provider information. No parameters.

```json
{"code":200,"message":"success","data":{"appVersion":1,"appVersionName":"example","apiVersion":1,"cloudVersion":1,"cloudVersionName":"example"}}
```

### Info :id=model-info

| Field | JSON type | Meaning |
| --- | --- | --- |
| `appVersion` | integer | Installed LifeUp version code. |
| `appVersionName` | string | Installed LifeUp version name. |
| `apiVersion` | integer | LifeUp API version. |
| `cloudVersion` | integer | Installed LifeUp Cloud version code. |
| `cloudVersionName` | string | Installed LifeUp Cloud version name. |

### GET /coin :id=get-coin

Invokes `lifeup://api/query?key=coin` through the ContentProvider and returns its result object. No parameters. A null provider result becomes an empty object. Fields are provider-defined, rather than a typed `CoinData` response. A successful query supplies `value` (signed 64-bit integer coin balance) and `api_result: true`; failures can supply the provider status/error fields described below.

```json
{"code":200,"message":"success","data":{"value":50,"api_result":true}}
```
