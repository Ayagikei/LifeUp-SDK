# step

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** step

**Description:** Set the number of steps on the specified date, for example, it can be used to enter the number of steps with a wristband + automation tool. And can be used to modify historical records.

**Example:**

- Adjust the number of steps for 2022-10-21 in GMT+8 time zone to 9999 steps: [lifeup://api/step?count=9999&time=1666282995643](lifeup://api/step?count=9999&time=1666282995643)

| Parameter | Meaning                         | Type                                | Example       | Required | Notes |
| --------- | ------------------------------- | ----------------------------------- | ------------- | -------- | ----- |
| count     | number of steps                 | a number greater than or equal to 0 | 9999          | yes      |       |
| time      | arbitrary timestamp of the date | timestamp (ms)                      | 1666282995643 | yes      |       |

<br/>
