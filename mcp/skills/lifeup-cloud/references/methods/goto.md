# goto

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** goto

**Description:** Jump to a page in `LifeUp`

**Example:** [lifeup://api/goto?page=lab](lifeup://api/goto?page=lab)

**Explanation:** Jump to the Labs page

| Parameter | Meaning | Value | Example | Required | Notes |
| --------- | ------- | ----- | ------- | -------- | ----- |
| page | page | One of the following values:<br/>main<br/>setting<br/>about<br/>pomodoro<br/>feelings<br/>achievement<br/>history<br/>add_task<br/>add_achievement<br/>add_achievement_cate<br/>exp<br/>coin<br/>backup<br/>add_item<br/>lab<br/>custom_attributes<br/>pomodoro_record<br/>synthesis<br/>pic_manage<br/>purchase_dialog<br/>task_detail<br/>dlc<br/>new_default<br/>use_item_dialog<br/>achievement_list<br/>user_achievement | lab | yes | `purchase_dialog` refers to the purchase popup<br/> `use_item_dialog` refers to the use item popup<br/>Other entries refer to specific major pages |

#### 1. Jump to the item purchase/use pop-up window

When the `page` parameter is set to `purchase_dialog` or `use_item_dialog`, you can specify the item ID:

For example: `lifeup://api/goto?page=purchase_dialog&id=1`

| Parameter | Meaning | Value            | Example | Required | Notes   |
| --------- | ------- | ---------------- | ------- | -------- | ------- |
| id        | Item ID | Positive integer | 1       | Yes      | Item ID |

<br/>

#### 2. Jump to the subpage of the home page

When the `page` parameter is `main`, you can additionally specify the subpage to jump to:

For example, jump to the store page: `lifeup://api/goto?page=main&sub_page=shop`

| Parameter   | Meaning       | Value | Example | Required | Notes  |
| ----------- | ------------- | ----- | ------- | -------- | ------ |
| sub_page    | sub page name | One of:<br/>todo<br/>shop<br/>inventory<br/>achievement<br/>status<br/>me<br/>statistic<br/>pomodoro<br/>feelings<br/>world | shop    | no       |      |
| category_id | list id       | number | 0      | no       | If `sub_page` is a list page, you can specify the list id to jump to. <br/>Such as shop item list, inventory list, task list. |

<br/>

#### 3. Jump to task details

When the `page` parameter is `task_detail`, you can additionally specify the task id to jump to:

For example, jump to the details page of the specified task id 53: `lifeup://api/goto?page=task_detail&task_id=53`

| Parameter | Meaning       | Value         | Example      | Required | Notes |
| --------- | ------------- | ------------- | ------------ | -------- | ----- |
| task_id   | task id       | task id       | 53           | No*      | task id; if it is a repeating task, the id will be updated every time it is repeated. |
| task_gid  | task group id | task group id | 3            | No*      | task group id |
| task_name | task name     | string        | get up early | No*      | task name, fuzzy match one. |

**Notice:**

1. Only one of the three parameters needs to be provided.
    - If multiple are provided at the same time, there will be an internal priority order. But this is undefined behavior, APP will not guarantee the order.

<br/>

#### 4. Jump to new achievement page

When the `page` parameter is `add_achievement`, you **must** additionally specify the category id:

For example, jump to new achievement page with category id 1: `lifeup://api/goto?page=add_achievement&category_id=1`

| Parameter    | Meaning         | Value         | Example | Required | Notes  |
| ------------ | --------------- | ------------- | ------- | -------- | ------ |
| category_id  | Achievement category id | Achievement category id | 1       | Yes      |        |

#### 5. Jump to specific achievement category page

When the `page` parameter is `user_achievement`, you **must** additionally specify the category id:

For example, jump to achievement category page with id 1: `lifeup://api/goto?page=user_achievement&category_id=1`

| Parameter    | Meaning         | Value         | Example | Required | Notes  |
| ------------ | --------------- | ------------- | ------- | -------- | ------ |
| category_id  | Achievement category id | Achievement category id | 1       | Yes      |        |

#### 6. Jump to specific synthesis category page

When the `page` parameter is `synthesis`, you can optionally specify the category id:

For example, jump to synthesis category page with id 1: `lifeup://api/goto?page=synthesis&category_id=1`

| Parameter    | Meaning         | Value         | Example | Required | Notes  |
| ------------ | --------------- | ------------- | ------- | -------- | ------ |
| category_id  | Synthesis category id | Synthesis category id | 1       | No       |        |

You can also open synthesis page with a filter (v1.102.0+):

For example, filter by product item id 1: `lifeup://api/goto?page=synthesis&filter_type=product&filter_item_id=1&filter_item_name=Gem`

| Parameter        | Meaning           | Value | Example | Required | Notes |
| --------------- | ----------------- | ----- | ------- | -------- | ----- |
| filter_type     | Filter type       | product / ingredient / related | product | No* | Requires filter_item_id |
| filter_item_id  | Filter item id    | number > 0 | 1 | No* | Requires filter_type |
| filter_item_name| Filter item name  | text | Gem | No | Optional, used for display |
