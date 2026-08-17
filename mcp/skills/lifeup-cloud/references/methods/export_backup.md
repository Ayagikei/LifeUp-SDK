# export_backup

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**export_backup

**说明：**创建一个备份文件并返回其 URI（仅支持通过 Content Provider 调用）

!> 此 API 只能通过 Content Provider 方式调用，不支持直接使用 URL Scheme 调用

| 参数          | 含义           | 取值            | 示例  | 是否必须 | 备注                                           |
| ------------- | -------------- | --------------- | ----- | -------- | ---------------------------------------------- |
| withMedia     | 是否包含媒体文件 | true 或者 false | true  | 否       | 是否在备份中包含媒体文件（图片、音效等）<br/>默认为 true |
| callingPackage| 调用方包名     | 任意文本        | com.example.app | 否 | Content Provider 调用时的包名标识 |

**返回数据：**

| 字段名          | 类型   | 说明                 | 示例                                          |
| --------------- | ------ | -------------------- | --------------------------------------------- |
| backup_file_uri | 文本   | 备份文件的 URI 地址  | content://net.sarasarasa.lifeup.api/backup/file.zip |

<br/>
