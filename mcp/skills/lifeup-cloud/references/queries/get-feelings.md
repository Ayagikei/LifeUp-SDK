# Feelings

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=get-feelings

Help topic: `query/get-feelings`

## Feelings :id=get-feelings

`GET /feelings` returns `Feelings[]`. Query `offset` defaults to 0 and `limit` to 100; missing or non-integer values use those defaults. This endpoint has no explicit clamp. Use nonnegative offsets and positive limits. Deleted feelings are excluded. Sorting follows the app's feelings order preference (`createTime` ascending or descending), not a request parameter. No time filter is exposed.

```json
{"code":200,"message":"success","data":[{"id":1,"content":"A good day","isFav":false,"title":"Read","time":1700000000000,"attachments":[],"type":0}]}
```

### Feelings :id=model-feelings

| Field | JSON type | Meaning |
| --- | --- | --- |
| `id` | integer or null | Feeling ID. |
| `content` | string | Text. |
| `isFav` | boolean | Favorite flag. |
| `title` | string | Related title. |
| `time` | integer | Creation Unix time in milliseconds. |
| `attachments` | string[] | Readable attachment URIs for existing nonempty files. |
| `type` | integer | 0 task; 1 achievement; 2 independent entry. |
