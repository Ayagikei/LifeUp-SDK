# placeholder

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**placeholder

**说明：**该接口自身不处理任何逻辑，但你可以搭配 callback、broadcast 使用。

**示例：**

- [lifeup://api/placeholder?broadcast=app.lifeup.item.rest](lifeup://api/placeholder?broadcast=app.lifeup.item.rest)

#### 变量占位符

「人升」提供了对参数的用户介入处理手段。

| 占位符                              | 含义                                                         | 示例                                                         |
| ----------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| [$text\|标题]                       | 文本占位符                                                   | [$text\|输入任务名称]                                        |
| [$number\|标题]                     | 数字占位符（不含小数点）                                     | [$number\|输入价格]                                          |
| [$number\|标题\|signed]            | 数字占位符（不含小数点），并显示正负符号                     | [$number\|输入价格\|signed]                                  |
| [$decimal\|标题]                    | 数字占位符（含小数点）                                       | [$decimal\|输入ATM利率]                                       |
| [$decimal\|标题\|signed]           | 数字占位符（含小数点），并显示正负符号                       | [$decimal\|输入ATM利率\|signed]                                |
| [$item]                             | 选择商品，将被替换为商品id                                   | [$item]                                                      |
| [$task_category]                    | 选择任务清单，将被替换为任务清单id                           | [$task_category]                                             |
| [$time\|锚定时间\|偏移毫秒（可选）] | 时间占位符<br/><br/>其中锚定时间的取值有：<br/>`current`、`today`、`this_monday`、`last_monday`、`this_month`、`last_month`、`this_year`、`last_year`<br/><br/>偏移毫秒应该为整数，默认为 0 毫秒 | 今天0点：[$time\|today]<br/>明天0点：[$time\|today\|86400000] |
| [$random_number\|最小值\|最大值]    | 随机数字占位符（不含小数点）                                 | [$random_number\|0\|3000]                                    |
| [$random_decimal\|最小值\|最大值]   | 随机数字占位符（含小数点）                                   | [$random_decimal\|1.0\|2.0]                                  |

**示例1：使用时，选择物品降价1金币**

比如当你设置为某个商品降价的 api 后，**可能希望在调用的时候，再允许用户选择指定商品。**而非调用时就指定 id。

以下 api 只能让 id 为 1 的商品降价 1 金币：

```url
lifeup://api/item?id=1&set_price=-1&set_price_type=relative
```

只需要将商品 id 修改为占位符`[$item]`，就可以实现调用的时候，用户能主动选择想要降价的商品：

[lifeup://api/item?id=[$item|请选择你想要降价1金币的商品]&set_price=-1&set_price_type=relative](lifeup://api/item?id=[$item|请选择你想要降价1金币的商品]&set_price=-1&set_price_type=relative)

**示例2：任务模板，只需要输入任务名称和选择清单，即可创建提前设置好的奖励模板**

[lifeup://api/add_task?todo=[$text|输入任务名称]&notes=这是个任务的奖励模板&coin=10&coin_var=10&exp=2048&skills=1&skills=2&skills=3&category=[$task_category]](lifeup://api/add_task?todo=[$text|输入任务名称]&notes=这是个任务的奖励模板&coin=10&coin_var=10&exp=2048&skills=1&skills=2&skills=3&category=[$task_category])

<br/>

#### 结束回调

所有接口你都可以加上`callback`参数，实现调用后回调该`URL`的处理。

这也可以用于拼接多个接口，比如想要实现跳转后提示激励语：

lifeup://api/goto?page=lab + lifeup://api/toast?text=callback

可以使用`callback`参数，参考上文**基础知识-转义**，就可以写出这种的处理：

[lifeup://api/goto?page=lab&callback=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D测试callback](lifeup://api/goto?page=lab&callback=lifeup:%2F%2Fapi%2Ftoast%3Ftext%3D测试callback)

当然，你也完全可以为一个商品添加多个链接来实现该效果。

该回调更多是用于：

A应用 -> 人升 -> A应用

或

A应用 -> 人升 -> B应用

<br/>

#### Broadcast 广播返回值

!> 此处的功能是用于搭配自动化工具/二次开发的，有一定门槛。

增加这个参数，可以将 API 原本的返回值也通过广播发送出去。以便 Tasker 等自动化工具能够接收到。

broadcast 的数值相当于 Tasker 中的「收到的意图」的操作一栏的数值，你可以填写任意文本，只要这两者对应上即可。

**比如 Tasker 使用查询金币的 API（[如果你在使用MacroDroid，请查看这篇教程](https://github.com/Ayagikei/LifeUp/issues/43)）：**

[lifeup://api/query?key=coin](lifeup://api/query?key=coin)

1. 添加 broadcast 参数，让它能够广播返回值到 Tasker，可以是任意文本，比如`app.lifeup.query.coin`。

   [lifeup://api/query?key=coin&broadcast=app.lifeup.query.coin](lifeup://api/query?key=coin&broadcast=app.lifeup.query.coin)

2. 在 Tasker 中添加事件->「收到的意图」，在操作一栏填写「app.lifeup.query.coin」

3. 然后可以在 Tasker 中的任务以`%value`变量的方式接收到`value`的返回值。

4. 然后你可以在 Tasker 中判断金币数实现各种效果了。（比如根据金币数更换桌面壁纸？）

![](_media/api/broadcast_01.png ':size=30%')

![](_media/api/broadcast_02.png ':size=30%')

![](_media/api/broadcast_03.png ':size=30%')

![](_media/api/broadcast_04.png ':size=30%')

<br/>

---

## 广播事件通知

!> 此处的功能是用于搭配自动化工具/二次开发的，有一定使用知识和使用门槛。

> 1.90.2 版本我们会以广播的形式向外部发送人升的各种事件。你可以使用 Tasker 等自动化工具接收这些事件来触发 Tasker 的动作。

### 启用

默认广播事件是关闭的。

你可以在`设置`-`实验`-`开发者模式`-`广播事件`启用它。

### 示例：使用商品更换壁纸

1. 新建一个商品，名称叫做「更换壁纸」。
2. 在 Tasker 中，`配置文件`->`事件`->`系统`->`收到的意图`，在操作一栏输入`app.lifeup.item.use`，返回。
3. 点击新建任务，输入任意名称（比如更换壁纸）。
4. 点击右下角+号添加任务，选择`任务`->`If`
5. 将条件一栏调整为 `%name = 更换壁纸`。
6. 返回，`插入操作`选择`If`。
7. 再次点击右下角+号添加任务，选择`显示`->`设置壁纸`
8. （可选）`类型`更换为`全部`
9. 图像一栏，点击🔍图标，选择你想要的壁纸文件
10. 退出，检查是否已经启用了这个配置。
11. 在`人升`中使用「更换壁纸」商品，你应该能成功见到壁纸更换了

![](_media/api/broadcast_sample_01.png ':size=30%')

![](_media/api/broadcast_sample_02.png ':size=30%')

![](_media/api/broadcast_sample_03.png ':size=30%')

![](_media/api/broadcast_sample_04.png ':size=30%')

使用`空接口`+`Broadcast 广播返回值`可以以更简洁的方式实现这个效果，可以摸索下。

<br/>

### 完成任务

**名称：**app.lifeup.task.complete

**返回值：**

| 参数        | 含义           | 示例         |
| ----------- | -------------- | ------------ |
| task_id     | 任务id         | 1            |
| task_gid    | 任务组id       | 1            |
| name        | 任务名称       | 开始使用人升 |
| category_id | 任务所属清单id | 1            |

### 放弃任务

**名称：**app.lifeup.task.giveup

**返回值：**

| 参数        | 含义           | 示例            |
| ----------- | -------------- | --------------- |
| task_id     | 任务id         | 1               |
| task_gid    | 任务组id       | 1               |
| name        | 事项名称       | Getting started |
| category_id | 任务所属清单id | 1               |

### 逾期任务

**名称：**app.lifeup.task.overdue

**返回值：**

| 参数      | 含义             | 示例                            |
| --------- | ---------------- | ------------------------------- |
| task_ids  | 任务id**数组**   | [1, 2, 3]                       |
| task_gids | 任务组id**数组** | [1, 2, 3]                       |
| names     | 事项名称**数组** | [Getting started, Drink Waters] |
| task_ids_json  | 任务id**Json数组**   | [1, 2, 3]                       |
| task_gids_json | 任务组id**Json数组** | [1, 2, 3]                       |
| names_json     | 事项名称**Json数组** | ["Getting started", "Drink Waters"]                      |

### 解锁成就

**名称：**app.lifeup.achievement.unlock

**返回值：**

| 参数           | 含义     | 示例             |
| -------------- | -------- | ---------------- |
| achievement_id | 成就id   | 1                |
| name           | 成就名称 | 连续使用人升30天 |

### 购买商品

**名称：**app.lifeup.item.purchase

**返回值：**

| 参数    | 含义     | 示例       |
| ------- | -------- | ---------- |
| item_id | 商品id   | 1          |
| name    | 商品名称 | 休息10分支 |
| amount  | 购买数量 | 1          |

### 使用商品

**名称：**app.lifeup.item.use

**说明：**当普通商品使用成功，或通过商品使用流程触发的简易合成成功时发送。

**返回值：**

| 参数    | 含义     | 示例       |
| ------- | -------- | ---------- |
| item_id | 商品id   | 1          |
| name    | 商品名称 | 休息10分支 |
| amount  | 使用数量 | 1          |

### 配方合成完成

> [!NOTE]
> 该广播事件于 v1.102.8 版本发布。

**名称：**app.lifeup.synthesis.complete

**说明：**当一次配方合成成功完成时发送。

**返回值：**

| 参数         | 含义                         | 示例                                                  |
| ------------ | ---------------------------- | ----------------------------------------------------- |
| formula_id   | 配方id                       | 1                                                     |
| formula_name | 配方名称                     | 工具箱配方                                            |
| times        | 本次执行次数                 | 3                                                     |
| input_count  | 输入材料条目数               | 2                                                     |
| output_count | 产物条目数                   | 1                                                     |
| inputs_json  | 本次总消耗材料的 JSON 数组   | [{"item_id":7,"name":"木材","amount":6}]              |
| outputs_json | 本次总产出结果的 JSON 数组   | [{"item_id":9,"name":"工具箱","amount":3}]            |

**注意：**

- 仅在配方合成成功后发送。
- 材料不足、配方不存在、数据库保存失败，或关闭`广播事件`时，都不会发送该事件。
- 一次 API 调用只发送一条事件，即使 `times > 1` 也不会拆分成多条。
- `inputs_json` 和 `outputs_json` 中每个条目都使用如下结构：

```json
{
  "item_id": 7,
  "name": "木材",
  "amount": 6
}
```

- 其中 `amount` 表示**本次执行的总消耗 / 总产出**，不是单次配方量。

### 添加感想

**名称：**app.lifeup.feelings.add

**返回值：**

| 参数              | 含义                     | 示例                  |
| ----------------- | ------------------------ | --------------------- |
| feelings_id       | 感想id                   | 1                     |
| action_type       | 操作类型                 | add 或 update         |
| content           | 感想内容                 | 今天心情不错！         |
| create_time       | 创建时间戳               | 1642060800000         |
| relate_type       | 关联类型                 | 0                     |
| related_id        | 关联对象id               | 1                     |
| attachments_count | 附件数量                 | 2                     |
| attachments       | 附件数组                 | ["/path/1", "/path/2"] |

### 等级提升

**名称：**app.lifeup.level.up

**返回值：**

| 参数     | 含义     | 示例 |
| -------- | -------- | ---- |
| skill_id | 任务id   | 1    |
| name     | 名称     | 力量 |
| level    | 当前等级 | 2    |

### 等级降低

**名称：**app.lifeup.level.down

**返回值：**

| 参数     | 含义     | 示例 |
| -------- | -------- | ---- |
| skill_id | 任务id   | 1    |
| name     | 名称     | 力量 |
| level    | 当前等级 | 2    |

### 商品倒计时

**名称：**

- 开始：app.lifeup.item.countdown.start
- 停止：app.lifeup.item.countdown.stop
- 完成（正常结束）：app.lifeup.item.countdown.complete

**返回值：**

| 参数      | 含义             | 示例       |
| --------- | ---------------- | ---------- |
| item_id   | 商品id           | 1          |
| name      | 商品名称         | 学习30分钟 |
| time_left | 剩余时间（毫秒） | 30000      |

### 番茄钟生命周期

?> 该广播事件于 v1.101.0 版本发布，提供更丰富的事件数据。

**名称：**

- 开始：app.lifeup.pomodoro.start
- 暂停：app.lifeup.pomodoro.pause (v1.101.0 新增)
- 停止：app.lifeup.pomodoro.stop
- 完成（正常结束）：app.lifeup.pomodoro.complete

**说明：**当番茄钟启动、暂停、停止或完成时，系统会发送相应的广播事件，携带任务信息、计时状态等详细数据。

**返回值：**

| 参数               | 含义                        | 示例                | 备注                                                           |
| ------------------ | --------------------------- | ------------------- | -------------------------------------------------------------  |
| task_id            | 任务ID                      | 1                   | 可选，仅当番茄钟关联了任务时存在                                 |
| task_gid           | 任务组ID                    | 1                   | 可选，仅当番茄钟关联了任务时存在                                 |
| name               | 任务名称                    | 学习英语             | 番茄钟关联的任务名称或自定义名称                                |
| service_type       | 服务类型                    | 0                   | 0=专注，1=短休息，2=长休息                                     |
| service_type_label | 服务类型标签                | 专注                | 本地化的服务类型文本                                            |
| duration           | 总时长（毫秒）              | 1500000             | 专注或休息的总时长                                              |
| remaining          | 剩余时长（毫秒）            | 900000              | 当前剩余的时长                                                  |
| elapsed            | 已用时长（毫秒）            | 600000              | 已经过的时长                                                    |
| start              | 开始时间                    | 1639123456789       | Unix 时间戳（毫秒）                                                |
| event_time         | 事件触发时间                | 1639123456789       | Unix 时间戳（毫秒）                                                |
| reason             | 停止原因                    | user                | 仅 stop 事件有此字段，可能值：manual, cancel, complete, auto       |

**停止原因说明：**

`reason` 参数仅在 `app.lifeup.pomodoro.stop` 事件中存在，表示番茄钟停止的原因：

- `manual`: 用户手动停止
- `cancel`: 用户取消
- `complete`: 正常完成（注：完成时也会同时触发 `app.lifeup.pomodoro.complete` 事件）
- `auto`: 自动停止（如任务删除等）

### 正计时生命周期 :id=broadcast_positive_timing

> [!NOTE]
> 该组事件对应“正计时”功能，不同于上面的番茄钟倒计时事件。使用前同样需要先在 `设置`→`实验`→`开发者模式`→`广播事件` 中启用广播事件。

**名称：**

- 开始：app.lifeup.timing.start
- 暂停：app.lifeup.timing.pause
- 完成：app.lifeup.timing.complete
- 放弃：app.lifeup.timing.abandon

**说明：**当正计时开始、暂停、完成或主动放弃时，系统会发送对应广播。`complete` 表示本次正计时正常完成并写入记录，`abandon` 表示本次正计时被主动停止或放弃。

**返回值：**

| 参数    | 含义               | 示例          | 备注                                     |
| ------- | ------------------ | ------------- | ---------------------------------------- |
| task_id | 任务ID             | 1             | 可选，仅当正计时关联了任务时存在         |
| name    | 任务名称           | 学习英语      | 正计时关联的任务名称或自定义名称         |
| time    | 已累计时长（毫秒） | 600000        | 当前正计时已累计的总时长                 |
| start   | 开始时间           | 1639123456789 | Unix 时间戳（毫秒）                      |
| end     | 结束时间           | 1639127056789 | 仅 `complete` / `abandon` 事件中存在     |

---

## 联动

我们非常欢迎其他开发者任何形式的联动。

### 需要更多 API？

API 功能目前仅经过了一个版本的迭代。

未来我们会持续加入更多的 API，以满足更多的使用场景。

如果你有需要的 API 场景，可以在 [Github 留下 Issues](https://github.com/Ayagikei/LifeUp/issues/new/choose) 或者我们的 [QQ 频道](https://ti.qq.com/open_qq/index.html?url=https%3A%2F%2Fqun.qq.com%2Fqqweb%2Fqunpro%2Fshare%3F_wv%3D3%26_wwv%3D128%26appChannel%3Dshare%26inviteCode%3D1W7IRQv%26businessType%3D9%26from%3D246610%26biz%3Dka%23%2Fout)进行留言。

<br/>

### 如何调用

#### Android

##### 使用 SDK

请参考：https://github.com/Ayagikei/LifeUp-SDK 的 `core` 模块。

##### 不使用 SDK

```kotlin
    /**
    * 定义一个方法处理 uri
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
        // 然后在合适的地方调用即可
        call(context, "lifeup://api/toast?text=你学会了调用！&type=1&isLong=true")
        ...
    }
```

<br/>

#### 网页

网页调用的话，能否触发也依赖于浏览器。常规的浏览器如夸克、Chrome、Edge都是可以的。但一些其他的系统内置的浏览器，可能会每次弹出时提醒用户“是否打开人升”。

如果你是自己开发的内嵌 WebView 应用，需要确保 WebView 能够处理 lifeup scheme。

如果你想要保证体验一致的话，可以使用「人升」里的商品链接效果，并勾选“使用内置浏览器”打开。但由于安全设置，这种方式仅支持 HTTPS 链接（不支持 HTTP）

**HTML**

直接超链接跳转即可

```htm
<a href="lifeup://api/toast?text=你学会了调用！&amp;type=1&amp;isLong=true" target="_blank" rel="noopener">点击这里调用</a>
```

**Javascript**

其实也是调用超链接

```javascript
location.href='lifeup://api/reward?type=coin&content=Wordle没猜对，安慰奖&number=1'
```

<br/>

### 应用开发者

如果你是应用开发者，且支持与「人升」联动，实现了有趣的功能和机制。

可以联系我们在应用内互相推荐应用。


<br/>

### 网页开发者

如果你是网页开发者，开发了调用「人升」API 的网页作品，欢迎在应用内通过商品的形式分享你的作品。

也可以联系我们进行互相推荐。

如果你的是静态网页，且不熟悉托管，也可以联系我们协助托管网页。

<br/>
