# freeze

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** freeze

**Description:** Trigger task freeze, only for repeating tasks.

**Example:**

- Search for tasks by name and freeze: [lifeup://api/freeze?name=get up early](lifeup://api/freeze?name=get up early)

**Explanation:**

| Parameter | Meaning       | Type                  | Example | Required | Notes |
| --------- | ------------- | --------------------- | ------- | -------- | ----- |
| id        | task id       | number greater than 0 | 1       | no*      | task id; if it is a repeating task, the id will be updated every time it repeats. |
| gid       | task group id | number greater than 0 | 1       | no*      | task group id; |
| name      | name          | any text              | get up  | no*      | fuzzy search, operate on only one matched task |
| time      | Freeze until  | timestamp             | 1661688800682 | no | - |

**Notice:**

1. In order to be able to match the task, one of id, gid, and name must be provided.

<br/>

#### Unfreeze a task
