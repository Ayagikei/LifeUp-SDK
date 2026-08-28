# give_up

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** give_up

**Description:** Trigger the task to give up.

**Example:**

- Search for tasks by name and give up: [lifeup://api/give_up?name=get up early](lifeup://api/give_up?name=get up early)

**Explanation:**

| Parameter | Meaning       | Type                  | Example | Required | Notes |
| --------- | ------------- | --------------------- | ------- | -------- | ----- |
| id        | task id       | number greater than 0 | 1       | no*      | task id; if it is a repeating task, the id will be updated every time it repeats. |
| gid       | task group id | number greater than 0 | 1       | no*      | task group id; |
| name      | name          | any text              | get up  | no*      | fuzzy search, operate on only one matched task |

**Notice:**

1. In order to be able to match the task, one of id, gid, and name must be provided.
