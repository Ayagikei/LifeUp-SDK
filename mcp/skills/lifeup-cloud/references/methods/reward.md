# reward

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** reward

**Description:** Provide the reward directly. The reason for the reward can be customized.

**Example:**

- Get 1 coin, and the reason for getting it is "Learn API Calls". And the reason will be displayed on the gold coin details page:

  <a href="lifeup://api/reward?type=coin&content=Learn API Calls&number=1">lifeup://api/reward?type=coin&content=Learn API Calls&number=1</a>

- Get 300 experience points for "Learning, Creativity" , and the reason for obtaining them is "Learn API Calls". And the reason will be displayed on the EXP details page:

  <a href="lifeup://api/reward?type=exp&content=Learn API Calls&number=300&skills=2&skills=6">lifeup://api/reward?type=exp&content=Learn API Calls&number=300&skills=2&skills=6</a>

- Obtained 1 fuzzy matching "treasure" item, and the reason for getting it is "Learn API Calls". And the reason will be displayed on the inventory history page:

  <a href="lifeup://api/reward?type=item&content=Learn API Calls&number=1&item_name=treasure">lifeup://api/reward?type=item&content=Learn API Calls&number=1&item_name=treasure</a>

| Parameter | Meaning                       | Type                            | Example            | Required | Notes                            |
| --------- | ----------------------------- | ------------------------------- | ------------------ | -------- | -------------------------------- |
| type      | reward type                   | currently only supported following values: <br/>coin<br/>exp<br/>item | coin | yes | coin - coins<br/>exp - experience points<br/>item - shop items |
| content   | reward reason                 | any text                        | Learning API Calls | Yes      |                                  |
| skills    | skills (attributes)           | array of numbers greater than 0 | 1                  | No       | Available only when type is exp<br/>Supported arrays (eg &skills=1&skills=2&skills=3)<br/>For how to obtain, see above The article "Basic Knowledge - LifeUp Data ID" |
| number    | number of rewards             | a number greater than 0         | 1                  | Yes      | If it is a gold coin, the maximum value is 999999<br/>If it is an experience value, the maximum value is 99999<br/>If it is a item, the maximum value is 999 |
| item_id   | item id                       | number greater than 0           | 1                  | no*      | only available when type is item |
| item_name | item name                     | any text                        | treasure           | no*      | only available when type is item, fuzzy matching with item names |
| silent    | whether to disable UI prompts | true or false                   | false              | no       | default is false                 |

<br/>
