# edit_exp

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** edit_exp

**Description:** This API can batch set the current experience values for attributes. It can directly set a specific experience value or a particular level.

**Example:**

> This API affects data, and to prevent accidental usage, direct clickable links are not provided here.

- Reset the experience values for the attributes [Strength] and [Knowledge] to 0: lifeup://api/edit_exp?skills=1&skills=2&exp=0
- Directly adjust the experience value for [Charm] to level 50: lifeup://api/edit_exp?skills=3&level=50

| Parameter | Meaning              | Type                                      | Example | Required | Notes |
| --------- | -------------------- | ----------------------------------------- | ------- | -------- | ----- |
| skills    | Attribute (Skill) ID | Array of numbers greater than 0           | 1       | No       | Supports arrays (i.e., &skills=1&skills=2&skills=3)<br/>For obtaining the attribute ID, please refer to the "Basic Knowledge - LifeUp Data ID" section |
| exp       | Set experience value | Number greater than or equal to 0 (int32) | 9999    | No, but either exp or level must be provided |                                                               |
| level     | Set level            | Number greater than or equal to 0 (int32) | 50      | No, but either exp or level must be provided | Represents the starting experience value for a particular level<br/>and will be affected by custom level gradients. |

<br/>
