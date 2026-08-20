# placeholder

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** placeholder

**Note:** This interface itself does not handle any logic, but you can use it with callback and broadcast.

**Example:**

- [lifeup://api/placeholder?broadcast=app.lifeup.item.rest](lifeup://api/placeholder?broadcast=app.lifeup.item.rest)

<br/>

#### Variable Placeholder

`LifeUp` provides user intervention processing methods for parameters.

| Placeholder                          | Meaning                                                      | Example                                                      |
|--------------------------------------|--------------------------------------------------------------|--------------------------------------------------------------|
| [$text\|title]                       | Text placeholder                                             | [$text\|Enter task name]                                     |
| [$number\|Title]                     | Number placeholder (without decimal point)                   | [$number\|Enter price]                                       |
| [$number\|Title\|signed]             | Number placeholder (without decimal point), show sign        | [$number\|Enter price\|signed]                               |
| [$decimal\|title]                    | Number placeholder (with decimal point)                      | [$decimal\|Enter ATM rate]                                   |
| [$decimal\|title\|signed]            | Number placeholder (with decimal point), show sign           | [$decimal\|Enter ATM rate\|signed]                           |
| [$item]                              | Select an item, it will be replaced with item id              | [$item]                                                      |
| [$task_category]                     | Select task list, which will be replaced with task list id    | [$task_category]                                             |
| [$time\|Anchor Time\|Offset in Milliseconds(optional)] | Time Placeholder<br/><br/>Possible values for Anchor Time:<br/>`current`, `today`, `this_monday`, `last_monday`, `this_month`, `last_month`, `this_year`, `last_year` <br/><br/>Offset in milliseconds should be an integer, default is 0 milliseconds | Midnight today: [$time\|today]<br/>Midnight tomorrow: [$time\|today\|86400000] |
| [$random_number\|Min\|Max]           | Random number placeholder (without decimal point)             | [$random_number\|0\|3000]                                    |
| [$random_decimal\|Min\|Max]          | Random number placeholder (with decimal point)                | [$random_decimal\|1.0\|2.0]                                  |

**Example 1: When using, select an item to reduce the price by 1 coin**

For instance, after setting the API for price reduction of a specific shop item, you may want to allow the user to select the specified item when calling instead of preset the id.

The following API can only reduce the price of the shop item with id 1 by 1 coin:

````url
lifeup://api/item?id=1&set_price=-1&set_price_type=relative
````

You only need to modify the item id to a placeholder [$item], and when the call is made, the user can actively select the item that they want to reduce the price:

<a href="lifeup://api/item?id=[$item]&set_price=-1&set_price_type=relative">lifeup://api/item?id=[$item]&set_price=-1&set_price_type=relative</a>

**Example 2: Task template, just enter the task name and selection list to create a pre-set reward template**

<a href="lifeup://api/add_task?todo=[$text|Enter a task name]&notes=This is a reward template for a task&coin=10&coin_var=10&exp=2048&skills=1&skills=2&skills=3&category=[$task_category]]">lifeup://api/add_task?todo=[$text|Enter a task name]&notes=This is a reward template for a task&coin=10&coin_var=10&exp=2048&skills=1&skills=2&skills=3&category=[$task_category]]</a>

<br/>

#### End Callback

You can add the callback parameter to all interfaces to implement the processing of calling back the URL after the call.

This can also be used to splice multiple interfaces, for example, if you want to implement a prompt after the jump:

lifeup://api/goto?page=lab + lifeup://api/toast?text=callback

You can use the callback parameter. Please also refer to the above **Basics - Escaping**. You can write this kind of processing:

<a href="lifeup://api/goto?page=lab&callback=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dtest callback">lifeup://api/goto?page=lab&callback=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3Dtest callback</a>

Of course, you can add multiple links to a shop item to achieve this effect.

Besides, this callback is more used for:

X application -> LifeUp -> X application

or

X application -> LifeUp -> Y application

<br/>

#### Broadcast return value

!> The functions here are used with automated tools/secondary development, and there are certain thresholds.

By adding this parameter, the original return value of the API can also be sent by broadcasting. so that automated tools such as Tasker can receive it.

The value of broadcast is equivalent to the value of the operation column of "Intentions Received" in Tasker. You can fill in any text, as long as the two correspond.

**For example, using the API for querying gold coins with Tasker ([If you're using MacroDroid, please check this link.](https://github.com/Ayagikei/LifeUp/issues/43)):**

[lifeup://api/query?key=coin](lifeup://api/query?key=coin)

1. Add the broadcast parameter to enable it to broadcast the return value to the Tasker, which can be any text, such as `app.lifeup.query.coin`.

   [lifeup://api/query?key=coin&broadcast=app.lifeup.query.coin](lifeup://api/query?key=coin&broadcast=app.lifeup.query.coin)

2. Add event in Tasker -> "Intent Received", fill in "app.lifeup.query.coin" in the action column

3. The task in Tasker can then receive the return value of `value` in the form of a `%value` variable.

4. Then you can judge the number of gold coins in Tasker to achieve various effects. (For example, change the desktop wallpaper according to the number of gold coins?)

![](_media/api/broadcast_01.png ':size=30%')

![](_media/api/broadcast_02.png ':size=30%')

![](_media/api/broadcast_03.png ':size=30%')

![](_media/api/broadcast_04.png ':size=30%')

<br/>

---

## Broadcast Event Notification

!> The functions here are used with automated tools/secondary development.

> In version 1.90.2, we will broadcast various events to the outside world. You can use automation tools such as Tasker to receive these events to trigger Tasker actions.
>
### Enable

**By default, broadcast events are turned off.**

You can enable it in `Settings`-`Labs`-`Developer mode`-`Broadcast events`.

### Example: Change wallpaper with an item

1. Create a new item called "Change Wallpaper".
2. In Tasker, go to `Configuration file`->`Event`->`System`->`Intent Received`, enter `app.lifeup.item.use` in the operation column, and return.
3. Click New Task and enter any name (for example, change wallpaper).
4. Click the + sign in the lower right corner to add a task, select `Task`->`If`
5. Adjust the condition column to `%name eq change wallpaper`.
6. Go back, `Insert Action` select `If`.
7. Click the + sign in the lower right corner again to add a task, select `Display` -> `Set Wallpaper`
8. (Optional) Replace `Type` with `All`
9. In the image column, click the 🔍 icon and select the wallpaper file you want
10. Exit and check if this configuration is enabled.
11. Use the "Wallpaper Change" item in `LifeUp`, you should be able to see the wallpaper change successfully

![](_media/api/broadcast_sample_01.png ':size=30%')

![](_media/api/broadcast_sample_02.png ':size=30%')

![](_media/api/broadcast_sample_03.png ':size=30%')

![](_media/api/broadcast_sample_04.png ':size=30%')

Using `No Action`+`Broadcast return value` can achieve this effect in a more concise way, you can explore it.

<br/>

### Task completed

**Name:** app.lifeup.task.complete

**Return value:**

| Parameters  | Meaning          | Examples        |
| ----------- | ---------------- | --------------- |
| task_id     | task id          | 1               |
| task_gid    | task group id    | 1               |
| name        | task name        | Getting started |
| category_id | task category id | 1               |

### Task given up

**Name:** app.lifeup.task.giveup

**Return value:**

| Parameters  | Meaning          | Examples        |
| ----------- | ---------------- | --------------- |
| task_id     | task id          | 1               |
| task_gid    | task group id    | 1               |
| name        | task name        | Getting started |
| category_id | task category id | 1               |

### Task overdue

**Name:** app.lifeup.task.overdue

**Return value:**

| Parameters | Meaning                 | Examples                        |
| ---------- | ----------------------- | ------------------------------- |
| task_ids   | task id **array**       | [1, 2, 3]                       |
| task_gids  | task group id **array** | [1, 2, 3]                       |
| names      | task name **array**     | [Getting started, Drink Waters] |
| task_ids_json  | task id **Json array**   | [1, 2, 3]                       |
| task_gids_json | task group id **Json array** | [1, 2, 3]                       |
| names_json     | task name **Json array** | ["Getting started", "Drink Waters"]                      |

### Achievement unlocked

**Name:** app.lifeup.achievement.unlock

**Return value:**

| Parameters     | Meaning          | Examples                 |
| -------------- | ---------------- | ------------------------ |
| achievement_id | achievement id   | 1                        |
| name           | achievement name | Using LifeUp for 30 days |

### Items purchased

**Name:** app.lifeup.item.purchase

**Return value:**

| Parameters | Meaning           | Examples          |
| ---------- | ----------------- | ----------------- |
| item_id    | item id           | 1                 |
| name       | item name         | Break 10 branches |
| amount     | purchase quantity | 1                 |

### Item used

**Name:** app.lifeup.item.use

**Description:** Sent when a normal item use or simple synthesis use-flow succeeds.

**Return value:**

| Parameters | Meaning      | Examples          |
| ---------- | ------------ | ----------------- |
| item_id    | item id      | 1                 |
| name       | item name    | Break 10 branches |
| amount     | use quantity | 1                 |

### Synthesis complete

> [!NOTE]
> This broadcast event was released in v1.102.8.

**Name:** app.lifeup.synthesis.complete

**Description:** Sent when a recipe synthesis completes successfully.

**Return value:**

| Parameters   | Meaning                            | Examples                                              |
| ------------ | ---------------------------------- | ----------------------------------------------------- |
| formula_id   | formula id                         | 1                                                     |
| formula_name | formula name                       | Toolbox Recipe                                        |
| times        | execution times                    | 3                                                     |
| input_count  | number of input item entries       | 2                                                     |
| output_count | number of output item entries      | 1                                                     |
| inputs_json  | JSON array of all consumed inputs  | [{"item_id":7,"name":"Wood","amount":6}]              |
| outputs_json | JSON array of all produced outputs | [{"item_id":9,"name":"Toolbox","amount":3}]           |

**Notes:**

- This event is only sent after the synthesis succeeds.
- If materials are insufficient, the formula does not exist, saving fails, or `Broadcast events` is disabled, this event is not sent.
- A single API call sends only one event even if `times > 1`.
- In `inputs_json` and `outputs_json`, each item uses the following structure:

```json
{
  "item_id": 7,
  "name": "Wood",
  "amount": 6
}
```

- `amount` is the **total consumed / total produced** in this execution, not the per-formula amount.

### Feelings added / updated

**Name:** app.lifeup.feelings.add

**Return value:**

| Parameters | Meaning | Examples |
| --- | --- | --- |
| feelings_id | feeling id | 1 |
| action_type | `add` or `update` | add |
| content | feeling text | Feeling good today! |
| create_time | created-at timestamp (ms) | 1642060800000 |
| relate_type | related object type | 0 |
| related_id | related object id | 1 |
| attachments_count | attachment count | 2 |
| attachments | attachment path array | ["/path/1", "/path/2"] |

### Level up

**Name:** app.lifeup.level.up

**Return value:**

| Parameters | Meaning       | Examples |
| ---------- | ------------- | -------- |
| skill_id   | task id       | 1        |
| name       | name          | strength |
| level      | current level | 2        |

### Level down

**Name:** app.lifeup.level.down

**Return value:**

| Parameters | Meaning       | Examples |
| ---------- | ------------- | -------- |
| skill_id   | task id       | 1        |
| name       | name          | strength |
| level      | current level | 2        |

### Shop item countdown

**Name:**

- Start: app.lifeup.item.countdown.start
- Stop: app.lifeup.item.countdown.stop
- Complete: app.lifeup.item.countdown.complete

**Return value:**

| Parameters | Meaning                       | Examples                  |
| ---------- | ----------------------------- | ------------------------- |
| item_id    | item id                       | 1                         |
| name       | item name                     | play games for 30 minutes |
| time_left  | time remaining (milliseconds) | 30000                     |

### Pomodoro Lifecycle

?> This broadcast event was released in v1.101.0, providing richer event data.

**Name:**

- Start: app.lifeup.pomodoro.start
- Pause: app.lifeup.pomodoro.pause (new in v1.101.0)
- Stop: app.lifeup.pomodoro.stop
- Complete: app.lifeup.pomodoro.complete

**Description:** When the Pomodoro timer starts, pauses, stops, or completes, the system sends corresponding broadcast events carrying task information, timing status, and other detailed data.

**Return value:**

| Parameter          | Meaning                       | Example        | Notes                                                      |
| ------------------ | ----------------------------- | -------------- | ---------------------------------------------------------- |
| task_id            | Task ID                       | 1              | Optional, only exists when Pomodoro is associated with a task |
| task_gid           | Task group ID                 | 1              | Optional, only exists when Pomodoro is associated with a task |
| name               | Task name                     | Study English  | Task name associated with Pomodoro or custom name         |
| service_type       | Service type                  | 0              | 0=focus, 1=short break, 2=long break                      |
| service_type_label | Service type label            | Focus          | Localized service type text                                |
| duration           | Total duration (milliseconds) | 1500000        | Total duration of focus or break                           |
| remaining          | Remaining duration (milliseconds) | 900000     | Current remaining duration                                 |
| elapsed            | Elapsed duration (milliseconds) | 600000       | Duration elapsed                                           |
| start              | Start time                    | 1639123456789  | Unix timestamp (milliseconds)                              |
| event_time         | Event trigger time            | 1639123456789  | Unix timestamp (milliseconds)                              |
| reason             | Stop reason                   | user           | Only for stop event, possible values: manual, cancel, complete, auto |

**Stop reason description:**

The `reason` parameter only exists in the `app.lifeup.pomodoro.stop` event, indicating the reason for the Pomodoro stop:

- `manual`: User manually stopped
- `cancel`: User canceled
- `complete`: Completed normally (Note: When completed, `app.lifeup.pomodoro.complete` event is also triggered)
- `auto`: Automatically stopped (e.g., task deleted)

### Positive Timer Lifecycle :id=broadcast_positive_timing

> [!NOTE]
> These events are for the positive timer feature, not the Pomodoro countdown events above. Before using them, make sure `Settings` → `Labs` → `Developer mode` → `Broadcast events` is enabled.

**Name:**

- Start: app.lifeup.timing.start
- Pause: app.lifeup.timing.pause
- Complete: app.lifeup.timing.complete
- Abandon: app.lifeup.timing.abandon

**Description:** When a positive timer starts, pauses, completes, or is abandoned manually, LifeUp sends the corresponding broadcast. `complete` means the session finished normally and was recorded. `abandon` means the current session was stopped or discarded manually.

**Return value:**

| Parameters | Meaning                        | Examples      | Notes                                            |
| ---------- | ------------------------------ | ------------- | ------------------------------------------------ |
| task_id    | Task ID                        | 1             | Optional, only present when the timer is linked to a task |
| name       | Task name                      | Study English | Linked task name or a custom timer name          |
| time       | Accumulated duration (ms)      | 600000        | Total elapsed duration of the current positive timer |
| start      | Start time                     | 1639123456789 | Unix timestamp (milliseconds)                    |
| end        | End time                       | 1639127056789 | Only present in `complete` / `abandon` events    |

---

## Integration

We very much welcome any form of integration from other developers.

> More details will be provided soon...

### Need more APIs?

The API functionality is currently only in one version iteration.

In the future, we will continue to add more APIs to meet more usage scenarios.

If you need more APIs, you can leave Issues on [Github](https://github.com/Ayagikei/LifeUp/issues/new/choose).

<br/>

### How to call

#### Android

##### Using the SDK

Please refer to the `core` module at: https://github.com/Ayagikei/LifeUp-SDK.

##### Without Using the SDK

```kotlin
    /**
    * Define a method to handle the uri
    */
    private fun call(context: Context, uriString: String){
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(uriString)
            }
            context.startActivity(intent)
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

fun xxx() {
        ...
        // Then call it where appropriate
        call(context, "lifeup://api/toast?text=You+learned+to+call!&type=1&isLong=true")
        ...
    }
````

<br/>

#### Web page

If the webpage is called, whether it can be triggered depends on the browser. Regular browsers such as Quark, Chrome, and Edge are fine. But some other built-in browsers in the system may remind the user "whether to open Rensheng" every time it pops up.

If you are developing your own embedded WebView application, you must ensure that the WebView can handle the lifeup scheme.

To ensure a consistent experience, you can use the product link effect in `LifeUp` and check "Use built-in browser" to open it. But due to security settings, this way only supports HTTPS links (not HTTP)

**HTML**

Jump directly to the hyperlink

````htm
<a href="lifeup://api/toast?text=You learned to call!&type=1&isLong=true" target="_blank" rel="noopener">Click here to call</a>
````

**Javascript**

In fact, it is also called a hyperlink

````javascript
location.href='lifeup://api/reward?type=coin&content=consolation+prize&number=1'
````

<br/>

### Application/Web/Automation Developer

Let us know if you've developed anything related to LifeUp!

<br/>
