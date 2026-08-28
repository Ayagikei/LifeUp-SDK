# confirm_dialog

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** confirm_dialog

**Description:** A pop-up selection window pops up. You can customize the title, text, positive button, and negative button. Other interfaces can also be called when the button is clicked.

**Example:**

- [<a href="lifeup://api/confirm_dialog?title=Do you believe in love&positive_action=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dbelieve&negative_action=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Ddo not believe">lifeup://api/confirm_dialog?title=Do you believe in love&positive_action=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dbelieve&negative_action=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Ddo not believe</a>](lifeup://api/confirm_dialog?title=Do you believe in love&positive_action=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dbelieve&negative_action=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Ddo not believe)
- Other usage scenarios:
  - Choice of rewards
  - Event branch selection

| Parameter       | Meaning              | Type     | Example  | Required | Notes |
| --------------- | -------------------- | -------- | -------- | -------- | ----- |
| title           | popup title          | any text | Title    | yes      |       |
| message         | detailed description of the popup window | any text | This is the content of the popup window | no |  |
| positive_text   | positive button text | any text | YES      | no       |       |
| negative_text   | negative button text | any text | NO       | no       |       |
| neutral_text    | neutral button text  | any text | QUESTION | no       |       |
| positive_action | the link response of the positive button | URL (other interface) | lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D You clicked OK | no | It is actually the escaped text of the popup message interface. For escape rules, please refer to `Basic Knowledge - Escape`. |
| negative_action | the link response of the negative button | URL (other interface) | Same as above | no |  |
| neutral_action  | the link response of the neutral button  | URL (other interface) | Same as above | no |  |
| cancel_action   | the link response of the cancel action   | URL (other interface) | Same as above | no |  |

Nested actions: pass plain `lifeup://api/...` strings; MCP encodes once (`help` `basics`).
