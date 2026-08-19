# toast

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** toast

**Description:** Various styles of messages pop up

**Example:** <a href="lifeup://api/toast?text=Live well, eat well!&type=1&isLong=true">lifeup://api/toast?text=Live well, eat well!&type=1&isLong=true</a>

**Explanation:** The prompt "Live well, eat well!" pops up in a bonus style and displays it for a longer time.

> Click on the link of the example to test the effect

| Parameter | Meaning                | Type               | Example              | Required | Notes |
| --------- | ---------------------- | ------------------ | -------------------- | -------- | ----- |
| text      | Text message to prompt | Any text           | You learned to call! | yes      |       |
| type      | Text style type        | Number from 0 to 6 | 1                    | no       | 0 - Normal style<br/>1 - Bonus style<br/>2 - Tomato style<br/>3 - Success style<br/>4 - Prompt style<br/>5 - Warning style<br/>6 - Error style |
| isLong    | Display duration       | true or false      | true                 | no       | true - long<br/>false - short |

<br/>
