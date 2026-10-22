# Read and upload files

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=files-download

Help topic: `query/files-download`

## Read and upload files :id=files-download

### GET /files/{url} :id=files-get

URL-encode the complete readable URI as one path segment. This reads bytes through Android's content resolver, typically for icons, feeling attachments, backup exports, or uploaded files. The result is a byte stream, not a JSON success envelope. The route does not specify a filename or MIME type. Empty/unreadable resolver streams are not converted into a structured not-found result; thrown failures use the global HTTP 500 handler. A blank matched parameter produces an error envelope (`url is required`).

```bash
curl "$BASE/files/content%3A%2F%2Fexample.provider%2Fimages%2F1" -o image.bin
```

### POST /files/upload :id=files-upload

Send `multipart/form-data`. Every file part is accepted regardless of field name; nonfile form parts are ignored. Each original filename is used for a cache file; without one Cloud chooses `uploaded_image_<time>.jpg`. Returns an array of readable content URI strings. Uploading supplies a file URI for later API calls; it does not attach it to an item or task automatically. No files returns an error envelope with `No files received`.

```bash
curl -X POST "$BASE/files/upload" -F 'file=@image.png'
```

```json
{"code":200,"message":"success","data":["content://example.fileprovider/cache/image.png"]}
```
