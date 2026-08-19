# app_settings

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** app_settings

**Description:** Adjust app interface settings

**Examples:**

- Enable compact mode: [lifeup://api/app_settings?is_enable_compact_mode=true](lifeup://api/app_settings?is_enable_compact_mode=true)
- Enable Material You theme: [lifeup://api/app_settings?is_enable_material_you=true](lifeup://api/app_settings?is_enable_material_you=true)
- Change settings and restart UI immediately: [lifeup://api/app_settings?is_enable_compact_mode=true&restart_activities=true](lifeup://api/app_settings?is_enable_compact_mode=true&restart_activities=true)

| Parameter              | Meaning           | Values          | Example | Required | Notes                           |
| --------------------- | ----------------- | --------------- | ------- | -------- | ------------------------------- |
| is_enable_compact_mode| Enable compact mode| true or false  | true    | No       | Simplify interface elements     |
| is_enable_material_you| Enable Material You| true or false  | true    | No       | Enable Material You theme       |
| restart_activities    | Restart interface | true or false   | true    | No       | Apply interface changes immediately |

**Response:**

| Field  | Type    | Description  | Example | Notes                    |
| ------ | ------- | ------------ | ------- | ------------------------ |
| result | Integer | Result code  | 0       | 0 indicates success      |

<br/>
