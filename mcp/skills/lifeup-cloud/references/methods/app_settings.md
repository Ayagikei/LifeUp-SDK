# app_settings

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**app_settings

**说明：**调整应用的界面设置

**示例：**

- 启用简洁模式：[lifeup://api/app_settings?is_enable_compact_mode=true](lifeup://api/app_settings?is_enable_compact_mode=true)
- 启用 Material You 主题：[lifeup://api/app_settings?is_enable_material_you=true](lifeup://api/app_settings?is_enable_material_you=true)
- 更改设置并立即重启界面：[lifeup://api/app_settings?is_enable_compact_mode=true&restart_activities=true](lifeup://api/app_settings?is_enable_compact_mode=true&restart_activities=true)

| 参数                    | 含义              | 取值            | 示例  | 是否必须 | 备注                           |
| ---------------------- | ----------------- | --------------- | ----- | -------- | ------------------------------ |
| is_enable_compact_mode | 是否启用简洁模式   | true 或者 false | true  | 否       | 精简界面元素                    |
| is_enable_material_you | 是否启用Material You| true 或者 false | true  | 否       | 启用 Material You 主题          |
| restart_activities     | 是否重启界面       | true 或者 false | true  | 否       | 立即应用界面更改                |

**返回数据：**

| 字段名 | 类型   | 说明     | 示例 | 备注             |
| ------ | ------ | -------- | ---- | ---------------- |
| result | 整数   | 结果代码 | 0    | 0 表示设置成功   |

<br/>
