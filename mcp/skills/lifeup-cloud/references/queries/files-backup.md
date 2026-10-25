# Backup export and import

Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=files-backup

Help topic: `query/files-backup`

## Backup export and import :id=files-backup

### GET /data/export :id=files-export

Triggers backup creation through the provider. Optional query `withMedia` defaults to `true`; when present, case-insensitive `true` means true and any other value means false. Returns the converted provider Bundle, currently containing `backup_file_uri`; use `/files/{url}` to download that URI. This endpoint creates an export even though its HTTP method is GET. Provider API status/error fields can also be present and must be checked. An empty `backup_file_uri` indicates export did not produce a usable file. A null result becomes an error.

```json
{"code":200,"message":"success","data":{"backup_file_uri":"content://example.provider/backup/LifeUpExportBackup.zip","api_result":true}}
```

### POST /data/import :id=files-import

Send `multipart/form-data` with a backup file. The last file part wins if multiple are supplied; its field name is not checked. Filename defaults to `backup.lfbak`. Optional form field `description` is read but has no effect. Cloud caches the file and launches LifeUp's backup activity; the response confirms upload and activity launch, **not completed restoration**. No file returns `No file received`.

```bash
curl -X POST "$BASE/data/import" -F 'file=@backup.lfbak'
```

```json
{"code":200,"message":"success","data":"File uploaded and BackupActivity launched"}
```
