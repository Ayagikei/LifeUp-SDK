# goto

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**goto

**说明：**跳转「人升」中的某个页面

**示例：**[lifeup://api/goto?page=lab](lifeup://api/goto?page=lab)

**解释：**跳转到实验页面

| 参数 | 含义 | 取值                                                         | 示例 | 是否必须 | 备注                                                         |
| ---- | ---- | ------------------------------------------------------------ | ---- | -------- | ------------------------------------------------------------ |
| page | 页面 | 固定以下数值其一：<br/>main<br/>setting<br/>about<br/>pomodoro<br/>feelings<br/>achievement<br/>history<br/>add_task<br/>add_achievement<br/>add_achievement_cate<br/>exp<br/>coin<br/>backup<br/>add_item<br/>lab<br/>custom_attributes<br/>pomodoro_record<br/>dlc<br/>pomodoro_record<br/>synthesis - 合成<br/>pic_manage<br/>purchase_dialog<br/>task_detail<br/>new_default<br/>achievement_list - 成就清单<br/>user_achievement - 具体某个成就清单，见下文<br/> | lab  | 是       | `purchase_dialog`指购买弹窗<br/>`use_item_dialog`指使用商品弹窗<br/>其他的都是具体的大页面 |

#### 1. 跳转商品购买/使用弹窗

> `use_item_dialog`参数引入自v1.94.0版本

当 `page` 参数为 `purchase_dialog`或`use_item_dialog`时，你可以指定商品id：

示例如：`lifeup://api/goto?page=purchase_dialog&id=1`

| 参数 | 含义   | 取值          | 示例 | 是否必须 | 备注   |
| ---- | ------ | ------------- | ---- | -------- | ------ |
| id   | 商品id | 大于 0 的数字 | 1    | 是       | 商品id |

<br/>

#### 2. 跳转首页的子页面

当 `page` 参数为 `main`时，你还可以额外指定跳转的子页面：

示例如，跳转到商店页面：`lifeup://api/goto?page=main&sub_page=shop`

| 参数        | 含义       | 取值                                                         | 示例 | 是否必须 | 备注                                                         |
| ----------- | ---------- | ------------------------------------------------------------ | ---- | -------- | ------------------------------------------------------------ |
| sub_page    | 子页面名称 | 固定以下数值其一：<br/>todo<br/>shop<br/>inventory<br/>achievement<br/>status<br/>me<br/>statistic<br/>pomodoro<br/>feelings<br/>world | shop | 否       |                                                              |
| category_id | 清单id     | 数字                                                         | 0    | 否       | 如果`sub_page`为含清单列表的页面，则可以指定跳转的清单id。<br/>如商店清单、仓库清单、任务清单。 |

#### 3. 跳转任务详情

当 `page` 参数为 `task_detail`时，你还可以额外指定跳转的任务 id：

示例如，跳转到指定任务 id 为 53 的详情页面：`lifeup://api/goto?page=task_detail&task_id=53`

| 参数      | 含义     | 取值     | 示例 | 是否必须 | 备注                                           |
| --------- | -------- | -------- | ---- | -------- | ---------------------------------------------- |
| task_id   | 任务id   | 任务id   | 53   | 否*      | 任务id；如果是重复任务，每次重复，id都会更新。 |
| task_gid  | 任务组id | 任务组id | 3    | 否*      | 任务组id                                       |
| task_name | 任务名称 | 字符串   | 早起 | 否*      | 任务名称，模糊匹配一个。                       |

**注意：**

1. 三个参数只需要提供其中之一。
   - 如果同时提供多个，会有内部的优先级顺序。但这属于未定义行为，APP 不会保证顺序。

#### 4. 跳转新建成就页面

当 `page` 参数为 `add_achievement`时，你还**需要**额外指定跳转的清单 id：

示例如，跳转到指定清单 id 为 1 的新建成就页面：`lifeup://api/goto?page=add_achievement&category_id=1`

| 参数      | 含义     | 取值     | 示例 | 是否必须 | 备注                                           |
| --------- | -------- | -------- | ---- | -------- | ---------------------------------------------- |
| category_id   | 成就清单id   | 成就清单id   | 1   | 是      |  |

#### 5. 跳转具体的成就清单页面

当 `page` 参数为 `user_achievement`时，你还**需要**额外指定跳转的清单 id：

示例如，跳转到指定清单 id 为 1 的成就清单页面：`lifeup://api/goto?page=user_achievement&category_id=1`

| 参数      | 含义     | 取值     | 示例 | 是否必须 | 备注                                           |
| --------- | -------- | -------- | ---- | -------- | ---------------------------------------------- |
| category_id   | 成就清单id   | 成就清单id   | 1   | 是      |  |

#### 6. 跳转具体的合成清单页面

当 `page` 参数为 `synthesis`时，你还**可选**额外指定跳转的清单 id：

示例如，跳转到指定清单 id 为 1 的合成清单页面：`lifeup://api/goto?page=synthesis&category_id=1`

| 参数      | 含义     | 取值     | 示例 | 是否必须 | 备注                                           |
| --------- | -------- | -------- | ---- | -------- | ---------------------------------------------- |
| category_id   | 合成清单id   | 合成清单id   | 1   | 否      |  |

你还可以以筛选模式打开合成页（v1.102.0+）：

示例如，筛选「产物=商品ID 1」：`lifeup://api/goto?page=synthesis&filter_type=product&filter_item_id=1&filter_item_name=示例商品`

| 参数            | 含义       | 取值 | 示例 | 是否必须 | 备注 |
| --------------- | ---------- | ---- | ---- | -------- | ---- |
| filter_type     | 筛选类型   | product / ingredient / related | product | 否* | 需与 filter_item_id 搭配 |
| filter_item_id  | 筛选物品ID | 大于 0 的数字 | 1 | 否* | 需与 filter_type 搭配 |
| filter_item_name| 筛选物品名 | 任意文本 | 示例商品 | 否 | 可选，用于显示 |

<br/>
