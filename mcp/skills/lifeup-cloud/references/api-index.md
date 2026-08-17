# API index

Catalog only. Do **not** read every method file.
Need params? `lifeup_help` with `topic` = the method name (e.g. `add_task`).
Wiki may lag: https://docs.lifeupapp.fun/zh-cn/#/guide/api

| method | 功能 |
| --- | --- |
| `toast` | 弹出各种样式的消息 |
| `reward` | 直接提供奖励，可定制奖励理由 |
| `penalty` | 直接提供惩罚，可定制惩罚理由 |
| `edit_coin` | 直接编辑用户的金币余额。金币数量将被设置为指定的值。变更原因可自定义，并将显示在金币历史记录中。 |
| `add_task` | 直接添加一个任务 |
| `complete` | 触发任务完成，只会搜索到未完成的任务 |
| `give_up` | 触发任务放弃 |
| `freeze` | 触发任务冻结，只适用于重复任务 |
| `unfreeze` | 触发任务解冻 |
| `delete_task` | 触发任务删除 |
| `edit_task` | 编辑已有任务的内容和属性 |
| `task_template` | 任务模板的 CRUD（列出/获取/创建/更新/删除）。 |
| `history_operation` | 对已完成/已放弃/已过期的任务进行操作 |
| `shop_settings` | 调整各种商店设置 |
| `goto` | 跳转「人升」中的某个页面 |
| `add_item` | 创建商品，包含自定义购买限制和使用效果等功能 |
| `item` | 对现有商品进行修改，包括价格、库存、效果等各项属性 |
| `loot_box` | 修改指定箱子的开箱效果，支持调整概率、奖励数和增加内容物。（暂不支持删除） |
| `loot_box/v2` | loot_box API 的改进版本，修改指定箱子的开箱效果，支持调整概率、奖励数、增加内容物和**删除内容物**。 |
| `use_item` | 使用指定商品。 |
| `deposit` | 存款，会进行合法性校验（金币余额是否充足）。 |
| `withdraw` | 取款，会进行合法性校验（ATM 余额是否充足）。 |
| `pomodoro_timer` | 控制人升内真实的番茄倒计时或正计时。该接口与 App UI 启动同一类计时会话， |
| `add_pomodoro` | 添加番茄计时记录 |
| `edit_pomodoro` | 编辑现有的番茄计时记录或添加新的记录，如果提供有效的 `edit_item_id`。 |
| `unlock_condition` | 解锁成就条件：需要外部API调用以解锁 |
| `step` | 设置指定日期的步数，比如可以用于搭配手环+自动化工具录入步数。并且可以用于修改历史纪录。 |
| `edit_exp` | 该 API 能批量设置属性的当前经验值，能直接设置某个经验值或者某个等级。 |
| `feeling` | 创建或更新感想。 |
| `tomato` | 调整番茄数量（增加、减少或设置指定数量） |
| `purchase_item` | 购买指定的商品 |
| `synthesize` | 使用已有的合成配方合成物品 |
| `synthesis_formula` | 新建、修改或删除合成配方 |
| `subtask` | 新建或编辑子任务 |
| `subtask_operation` | 对子任务进行完成、撤销完成或删除操作 |
| `category` | 添加或编辑各类清单（任务清单、成就清单、商店清单、合成清单） |
| `achievement` | 添加或编辑自定义成就和成就子分类 |
| `skill` | 新建或编辑自定义技能（属性） |
| `skill_group` | 创建、编辑、删除技能组，也支持一次性提交技能组与技能的混排排序结果。 |
| `export_backup` | 创建一个备份文件并返回其 URI（仅支持通过 Content Provider 调用） |
| `app_settings` | 调整应用的界面设置 |
| `query` | 查询参数 |
| `query_skill` | 查询指定技能的基础信息、原始排序字段，以及等级/经验值数据。 |
| `query_skill_group` | 查询单个技能组，并返回原始排序值与折叠状态。 |
| `random` | 简单的随机接口，可以随机触发多个 API 中的其中一个。 |
| `confirm_dialog` | 弹出一个选择弹窗，可以自定义标题、文本、积极按钮、消极按钮。点击按钮时也可以调用其他接口。 |
| `placeholder` | 该接口自身不处理任何逻辑，但你可以搭配 callback、broadcast 使用。 |

