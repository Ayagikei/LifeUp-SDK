# export_backup

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** export_backup

**Description:** Create a backup file and return its URI (Content Provider calls only)

!> This API can only be called through Content Provider, direct URL Scheme calls are not supported

| Parameter      | Meaning        | Values          | Example | Required | Notes                                         |
| ------------- | -------------- | --------------- | ------- | -------- | --------------------------------------------- |
| withMedia     | Include media files | true or false | true    | No       | Whether to include media files (images, sound effects, etc.) in backup<br/>Defaults to true |
| callingPackage| Caller package name | any text      | com.example.app | No | Package identifier for Content Provider calls |

**Response:**

| Field          | Type   | Description          | Example                                       |
| -------------- | ------ | -------------------- | --------------------------------------------- |
| backup_file_uri | Text   | Backup file URI      | content://net.sarasarasa.lifeup.api/backup/file.zip |

<br/>
