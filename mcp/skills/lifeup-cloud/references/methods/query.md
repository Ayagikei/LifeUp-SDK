# query

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** query

**Description:** query parameters

**Example:** - Query the current number of coins: [lifeup://api/query?key=coin](lifeup://api/query?key=coin)

| Parameter   | Meaning              | Type                                                         | Example | Required                                    | Notes                                                        |
| ----------- | -------------------- | ------------------------------------------------------------ | ------- | ------------------------------------------- | ------------------------------------------------------------ |
| key         | type of query        | coin / atm / item / item_id_list / tomato / task / achievement / credit_limit / punishment / broadcast | coin    | yes | `punishment` returns `{exp_factor, coin_factor}`; `broadcast` returns `{enabled}` |
| item_id     | the id of the item   | a number greater than 0                                      | 1       | When the key is `item`, it must be provided |                                                              |
| category_id | the Shop category id | Number greater than or equal to 0                            | 0       | no*                                         | Required only when the key is `item_id_list`, representing the ID of the list to be queried. |
| task_id / taskId | Task ID          | Number greater than 0                                        | 1       | When key is `task`, one of three* is required | Queried task ID |
| task_gid / taskGid / task_group_id / taskGroupId | Task group ID | Number greater than 0 | 1 | When key is `task`, one of three* is required | Queried task group ID |
| task_name / taskName | Task name      | Any text                                                     | Study   | When key is `task`, one of three* is required | Fuzzy-matched task name |
| withSubTasks | Include sub-tasks   | true or false                                                | true    | No                                          | Available only when key is `task`; defaults to true |

**Return Value:**

Only supported since version 1.90.2

When querying coin/atm:

| Parameter | Meaning                             | Type               | Example | Required | Notes |
| --------- | ----------------------------------- | ------------------ | ------- | -------- | ----- |
| value     | Numeric value returned by the query | number             | 1000    | yes      |       |

When querying an item:

| Parameter        | Meaning                         | Type     | Example   | Required | Notes |
| ---------------- | ------------------------------- | -------- | --------- | -------- | ----- |
| item_id          | the id of the item              | number   | 1         | yes      |       |
| name             | the name of the item            | any text | Coffee    | yes      |       |
| desc             | description                     | any text |           | no       |       |
| icon             | icon URL                        | any text | icon.webp | no       | If it is a local file, only the file name is returned |
| category_id      | category data id                | number   | 1         | yes      |       |
| stock_number     | shop stock quantity             | number   | -1        | yes      | `-1` represents infinite shop inventory |
| own_number       | the own number in the Inventory | number   | 10        | yes      |       |
| price            | the price                       | number   | 100       | yes      |       |
| order            | sort by                         | number   | 100       | yes      | Weight value when custom sorting |
| disable_purchase | Whether to disable purchase     | true or false | true | yes |       |
| purchase_limit   | Restriction rules               | JSON text | [{"limitType":0,"limitNumber":5}] | yes | Current restriction list |
| limit_scope      | Restriction scope               | purchase / use / both | use | yes | Returned as API text value |

When querying item_id_list:

| Parameter | Meaning                           | Type   | Example | Required | Notes |
| --------- | --------------------------------- | ------ | ------- | -------- | ----- |
| item_ids  | Comma-separated item ID array     | string | 1,2,3,4 | yes      |       |

When querying tomato:

| Parameter | Meaning                  | Type   | Example | Required | Notes |
| --------- | ------------------------ | ------ | ------- | -------- | ----- |
| total     | Total tomato count       | number | 100     | yes      |       |
| available | Available tomato count   | number | 50      | yes      |       |
| exchanged | Exchanged tomato count   | number | 50      | yes      |       |

When querying task (v1.101.0+):

| Parameter   | Meaning                      | Type        | Example | Required | Notes                           |
| ----------- | ---------------------------- | ----------- | ------- | -------- | ------------------------------- |
| _ID         | Task ID                      | number      | 1       | yes      | -                               |
| _GID        | Task group ID                | number      | 1       | yes      | -                               |
| name        | Task name                    | text        | Study   | yes      | -                               |
| notes       | Notes                        | text        | -       | no       | May be empty                    |
| status      | Task status                  | number      | 0       | yes      | 0=incomplete, 1=completed       |
| startTime   | Start time                   | number      | -       | yes      | Unix timestamp (milliseconds)   |
| deadline    | Deadline time                | number      | -       | no       | Unix timestamp (milliseconds), may be empty |
| remindTime  | Remind time                  | number      | -       | no       | Unix timestamp (milliseconds), may be empty |
| frequency   | Repetition frequency         | number      | -       | yes      | -                               |
| weekdays    | Weekdays                     | text        | 1,3,5   | no       | v1.106.0+; empty when not weekday mode. 1=Monday … 7=Sunday |
| exp         | EXP reward                   | number      | -       | yes      | -                               |
| skillIds    | Skill ID list                | JSON text   | -       | yes      | JSON array format               |
| coin        | Coin reward                  | number      | -       | no       | May be empty                    |
| coinVariable| Random coin reward           | number      | -       | no       | May be empty                    |
| itemId      | First reward item ID         | number      | -       | no       | May be empty                    |
| itemCount   | First reward item count      | number      | -       | no       | Returned when itemId exists     |
| items       | Item reward list             | JSON text   | -       | yes      | JSON array format               |
| words       | Completion incentive words   | text        | -       | no       | May be empty                    |
| categoryId  | Category ID                  | number      | -       | no       | May be empty                    |
| order       | Order                        | number      | -       | yes      | -                               |
| name_extended | Extended name              | text        | -       | yes      | Same as name                    |
| subTasks    | Sub-task list                | JSON text   | -       | yes      | See **Sub-tasks** below |

**Sub-tasks (subTasks) field description:**

The `subTasks` field is a JSON array, each element contains the following fields:

- `id`: Sub-task ID
- `gid`: Sub-task group ID
- `todo`: Sub-task content
- `status`: Sub-task status (0=incomplete, 1=completed)
- `remindTime`: Remind time (Unix timestamp, milliseconds)
- `exp`: EXP reward
- `coin`: Coin reward
- `coinVariable`: Random coin reward
- `items`: Item reward list
- `order`: Order
- `autoUseItem`: Whether to automatically use item

<br/>
