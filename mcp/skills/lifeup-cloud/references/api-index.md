# API index

Catalog only. Do **not** read every method file.
Need params? `help` with `topic` = the method name (e.g. `add_task`).
Wiki may lag: https://docs.lifeupapp.fun/en/#/guide/api

| method | purpose |
| --- | --- |
| `toast` | Various styles of messages pop up |
| `reward` | Provide the reward directly. The reason for the reward can be customized. |
| `penalty` | Provide a penalty directly. The reason for the penalty can be customized. |
| `edit_coin` | Edit the user's coin balance directly. The current coin amount will be set to the specified value. The reason for the change can be customized and will be displayed in the coin history. |
| `add_task` | Create a task directly |
| `complete` | Trigger task completion. Only unfinished tasks will be searched. |
| `give_up` | Trigger the task to give up. |
| `freeze` | Trigger task freeze, only for repeating tasks. |
| `unfreeze` | Trigger task unfreeze. |
| `delete_task` | Delete a task. |
| `edit_task` | Edit content and properties of an existing task |
| `task_template` | CRUD for task templates. |
| `history_operation` | Operate on completed/abandoned/expired tasks |
| `shop_settings` | shop_settings |
| `goto` | Jump to a page in `LifeUp` |
| `add_item` | Create a shop item with customizable properties including purchase limits and use effects. |
| `item` | Modify existing items, including price, stock, effects, and other properties |
| `loot_box` | Modify the loot box effect of the specified box item, support adjustment of probability, number of rewards and increase content. (Delete is not supported for now) |
| `loot_box/v2` | An improved version of the loot_box API. Modify the loot box effect of the specified box item, support adjustment of probability, number of rewards, adding content, and **deleting content**. |
| `use_item` | Use a specified item. |
| `deposit` | The deposit will be checked for legality (whether the coin balance is sufficient). |
| `withdraw` | Withdrawals will be checked for legality (whether the ATM balance is sufficient). |
| `pomodoro_timer` | Control the real Pomodoro countdown or count-up timer in LifeUp. This API starts |
| `add_pomodoro` | Add tomato timing record |
| `edit_pomodoro` | Edit an existing Pomodoro timing record or add a new record if a valid `edit_item_id` is provided. |
| `unlock_condition` | Unlock achievement condition: requires an external API call to unlock |
| `step` | Set the number of steps on the specified date, for example, it can be used to enter the number of steps with a wristband + automation tool. And can be used to modify historical records. |
| `edit_exp` | This API can batch set the current experience values for attributes. It can directly set a specific experience value or a particular level. |
| `feeling` | It is used to create or update records of feelings. |
| `tomato` | Adjust the number of tomatoes (increase, decrease, or set to a specific amount) |
| `purchase_item` | Purchase a specific item |
| `synthesize` | Synthesize items using an existing formula |
| `synthesis_formula` | Create, modify, or delete synthesis formulas |
| `subtask` | Create or edit subtasks |
| `category` | Add or edit categories (task lists, achievement lists, shop lists, synthesis lists) |
| `export_backup` | Create a backup file and return its URI (Content Provider calls only) |
| `subtask_operation` | Complete, undo completion, or delete subtasks |
| `achievement` | Add or edit custom achievements and achievement subcategories |
| `skill` | Create or edit custom skills (attributes) |
| `skill_group` | Create, edit, delete, or reorder skill groups. The sort API also supports mixed ordering of groups and skills. |
| `app_settings` | Adjust app interface settings |
| `query` | query parameters |
| `query_skill` | Query the basic information, raw sort fields, and level/experience data of a specified skill. |
| `query_skill_group` | Query a single skill group and return its raw sort and collapsed state. |
| `random` | A simple random interface that can trigger one of multiple APIs at random. |
| `confirm_dialog` | A pop-up selection window pops up. You can customize the title, text, positive button, and negative button. Other interfaces can also be called when the button is clicked. |
| `placeholder` | placeholder |

