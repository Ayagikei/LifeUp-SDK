# penalty

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** penalty

**Description:** Provide a penalty directly. The reason for the penalty can be customized.

**Example:** *Basically the same as the reward interface

- Penalize 1 coin, the reason for obtaining it is "sleep in". And the reason will be displayed on the coin details page:

  <a href="lifeup://api/penalty?type=coin&content=sleep in&number=1">lifeup://api/penalty?type=coin&content=sleep in&number=1</a>

- Penalize 300 "Strength" experience points for "sleep in". And the reason will be displayed on the EXP details page:

  <a href="lifeup://api/penalty?type=exp&content=sleep in&number=300&skills=1">lifeup://api/penalty?type=exp&content=sleep in&number=300&skills=1</a>

- Penalize 1 fuzzy matching "treasure" item for "sleep in". And the reason will be displayed on the inventory history page:

  <a href="lifeup://api/penalty?type=item&content=sleep in&number=1&item_name=treasure">lifeup://api/penalty?type=item&content=sleep in&number=1&item_name=treasure</a>

| Parameter | Meaning                       | Type                                                 | Example  | Required | Notes            |
| --------- | ----------------------------- | ---------------------------------------------------- | -------- | -------- | ---------------- |
| type      | penalty type                  | Currently only supported: <br/>coin<br/>exp<br/>item | coin     | yes      | coin - coins<br/>exp - experience points<br/>item - shop items |
| content   | reason for penalty            | any text                                             | Sleep In | Yes      |                  |
| skills    | skills (attributes)           | array of numbers greater than 0                      | 1        | No       | Available only when type is exp<br/>Supported arrays (eg &skills=1&skills=2&skills=3)<br/>For how to obtain, see above The article "Basic Knowledge - LifeUp Data ID" |
| number    | number of rewards             | a number greater than 0                              | 1        | Yes      | If it is a coin, the maximum value is 999999<br/>If it is an experience value, the maximum value is 99999<br/>If it is a item, the maximum value is 999 |
| item_id   | item id                       | number greater than 0                                | 1        | no*      | only available when type is item |
| item_name | item name                     | any text                                             | treasure | no*      | only available when type is item, fuzzy matching with item names |
| silent    | whether to disable UI prompts | true or false                                        | false    | no       | default is false |

<br/>
