# category

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** category

**Description:** Add or edit categories (task lists, achievement lists, shop lists, synthesis lists)

**Examples:**

- Create a task list: [lifeup://api/category?type=tasks&name=Study List](lifeup://api/category?type=tasks&name=Study List)
- Edit a shop list: [lifeup://api/category?type=shop&edit_id=1&name=Equipment Shop&order=1](lifeup://api/category?type=shop&edit_id=1&name=Equipment Shop&order=1)

| Parameter        | Meaning           | Values               | Example    | Required | Notes                           |
| --------------- | ----------------- | -------------------- | ---------- | -------- | ------------------------------- |
| type            | Category type     | One of:<br/>tasks<br/>achievements<br/>shop<br/>synthesis | tasks | Yes | tasks - Task lists<br/>achievements - Achievement lists<br/>shop - Shop lists<br/>synthesis - Synthesis lists |
| edit_id         | Category ID to edit| number greater than 0| 1         | No       | Required when editing           |
| name            | Category name     | any text             | Study List | No       | Required for new categories; optional when editing |
| order           | Sort order        | integer              | 1         | No       | Position in the list            |
| hidden          | Hide category     | true or false        | false     | No       | Only supported for task and shop lists |
| inventory_hidden| Hide in inventory | true or false        | false     | No       | Only supported for shop lists   |
| icon_uri        | Icon URI          | URI text             | content://... | No  | Only supported for achievement lists |
| desc            | Description       | any text             | This is a description | No | Only supported for achievement lists |
| color           | Tag color         | color string         | #66CCFF   | No       | Only supported for task lists; # must be escaped as %23 |

**Response:**

| Field | Type    | Description    | Example | Notes                    |
| ----- | ------- | -------------- | ------- | ------------------------ |
| id    | Number  | Category ID    | 1000    | ID of new or edited category |

<br/>
