# add_pomodoro

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** add_pomodoro

**Description:** Add tomato timing record

**Example:**

- Add a timing record with a duration of 25 minutes (1500000 ms) and point to a task whose name contains learning: [lifeup://api/add_pomodoro?task_name=learning&duration=1500000](lifeup://api/add_pomodoro?task_name=learning&duration=1500000)
- Add timing record for `2022-08-01 11:00:00` - `2022-08-01 12:00:00`: [lifeup://api/add_pomodoro?start_time=1659322800000&end_time=1659326400000](lifeup://api/add_pomodoro?start_time=1659322800000&end_time=1659326400000)

**Explanation:**

| Parameter        | Meaning                    | Type                    | Example       | Required | Notes |
| ---------------- | -------------------------- | ----------------------- | ------------- | -------- | ----- |
| start_time       | timing start time          | timestamp               | 1659322800000 | no*      | If you know nothing about Timestamp, google it! |
| duration         | focus duration             | number (in milliseconds) <br/>must be greater than 30000 | 1500000 | no* | |
| end_time         | timing end time            | timestamp               | 1659326400000 | no*      |       |
| reward_tomatoes  | whether to reward tomatoes | true or false           | true          | no       | default is false |
| task_id          | task id                    | a number greater than 0 | 1             | no       |       |
| task_gid         | task group id              | a number greater than 0 | 1             | no       |       |
| task_name        | name                       | any text                | learning      | no       | fuzzy search, only one of the tasks found |
| ui               | Display rewarded tomatoes UI | true or false         | true          | no       | Introduced in v1.94.0, defaults to true |

**Notice:**

1. One of start_time, duration, end_time must be provided.
2. In the case of only duration, the default end_time is the current time.
3. end_time needs to be greater than start_time.
4. duration is at least 30000 milliseconds (30 seconds).
5. If both start_time, duration, end_time are provided, duration should be less than or equal to (end_time - start_time).

<br/>

#### Edit Pomodoro Record

> Introduced in v1.94.0
>
