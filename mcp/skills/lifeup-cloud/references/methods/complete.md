# complete

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** complete

**Description:** Trigger task completion. Only unfinished tasks will be searched.

**Example:**

- Complete the task with id 1: [lifeup://api/complete?id=1](lifeup://api/complete?id=1)
- Complete the task with "task group id" of 1: [lifeup://api/complete?gid=1](lifeup://api/complete?gid=1)
- Search for tasks by name and complete them: <a href="lifeup://api/complete?name=Start using&ui=true">lifeup://api/complete?name=Start using&ui=true</a>

**Explanation:**

Each task has an id.

For repeating tasks, the id will be refreshed every time, but the "task group id" will remain the same.

The method of obtaining the id is to open the "Developer Mode" on the "Labs" page and then view it on the "Task Details" page.

| Parameter | Meaning                         | Type                  | Example | Required | Notes |
| --------- | ------------------------------- | --------------------- | ------- | -------- | ----- |
| id        | task id                         | number greater than 0 | 1       | no*      | task id; if it is a repeating task, the id will be updated every time it repeats. |
| gid       | task group id                   | number greater than 0 | 1       | no*      | task group id; |
| name      | name                            | any text              | get up  | no*      | fuzzy search, only one of the tasks found |
| ui        | Whether to display the popup UI | true or false         | true    | no       | the default is false, only a message is displayed in the background |
| count                    | Count value                        | Number                                                 | 1         | No       | Only applicable to count tasks, please use in conjunction with the `count_set_type` parameter     |
| count_set_type           | How to set the count value         | One of the following:<br/>absolute<br/>relative        | absolute  | No       | Default is relative<br/>absolute - Set the target to the value directly<br/>relative - Add or subtract based on the original value |
| count_force_sum_up       | Force tally of count task rewards  | true or false                                          | true      | No       |                                                                                                    |
| reward_factor            | Reward factor                      | Floating point number greater than 0                   | 1.1       | No       | Not applicable to count tasks<br/>Reward factor affects the amount of experience and coins (not the quantity of goods) |

**Notice:**

1. In order to be able to match the task, one of id, gid, and name must be provided.
2. Timed tasks cannot be completed manually via this API (v1.102.0+).
