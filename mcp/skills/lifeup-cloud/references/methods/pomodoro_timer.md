# pomodoro_timer

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**pomodoro_timer

**说明：**控制人升内真实的番茄倒计时或正计时。该接口与 App UI 启动同一类计时会话，
不会直接增加番茄记录或番茄数量。

**示例：**

- 启动或恢复默认工作番茄：
  [lifeup://api/pomodoro_timer?action=start&mode=countdown](lifeup://api/pomodoro_timer?action=start&mode=countdown)
- 选择任务 101 并开始正计时：
  [lifeup://api/pomodoro_timer?action=start&mode=count_up&task_id=101](lifeup://api/pomodoro_timer?action=start&mode=count_up&task_id=101)
- 暂停倒计时：
  [lifeup://api/pomodoro_timer?action=pause&mode=countdown](lifeup://api/pomodoro_timer?action=pause&mode=countdown)
- 放弃并重置番茄生命周期：
  [lifeup://api/pomodoro_timer?action=abandon&mode=countdown](lifeup://api/pomodoro_timer?action=abandon&mode=countdown)
- 跳过当前番茄周期：
  [lifeup://api/pomodoro_timer?action=skip](lifeup://api/pomodoro_timer?action=skip)
- 结算正计时但不领取番茄奖励：
  [lifeup://api/pomodoro_timer?action=complete&mode=count_up&receive_reward=false](lifeup://api/pomodoro_timer?action=complete&mode=count_up&receive_reward=false)
- 查询两种计时状态：
  [lifeup://api/pomodoro_timer?action=status](lifeup://api/pomodoro_timer?action=status)

**参数：**

| 参数 | 含义 | 取值 | 是否必须 | 备注 |
| ---- | ---- | ---- | -------- | ---- |
| action | 操作 | `start`、`pause`、`abandon`、`skip`、`complete`、`select_task`、`status` | 是 | - |
| mode | 计时模式 | `countdown`、`count_up` | `start`、`pause`、`abandon`、`complete` 必须 | `skip` 固定操作倒计时。 |
| stage | 倒计时阶段 | `work`、`short_break`、`long_break` | 否 | 仅适用于 `mode=countdown`。未传入时使用运行中、暂停中或已进入下一周期的 canonical 阶段；新生命周期从 `work` 开始。 |
| receive_reward | 是否领取番茄奖励 | `true` 或 `false` | `complete` 必须 | 严格布尔值；`complete` 仅支持 `mode=count_up`。 |
| task_id | 任务 ID | 大于 0 的整数 | 否 | 不能与 `task_gid` 或 `task_name` 同时使用。 |
| task_gid | 任务组 ID | 大于 0 的整数 | 否 | 可与 `task_name` 组合以缩小匹配范围。 |
| task_name | 任务名称 | 文本 | 否 | 优先精确匹配，再使用模糊匹配。 |
| clear_task | 清除计时任务 | `true` 或 `false` | 否 | `true` 不能与任务定位参数同时使用。 |

`select_task` 必须提供任务定位参数或 `clear_task=true`；`start` 也可以携带相同的任务选择
参数。不支持传入自定义时长：倒计时使用当前默认时长或所选任务配置的番茄时长。

`abandon&mode=countdown` 等价 App 左下角操作：放弃当前周期、重置番茄生命周期，并回到
停止的工作周期。`skip` 等价右下角操作：工作周期进入短/长休息，休息周期进入工作周期，
但不会自动开始下一周期。每次 `skip` 都是一次真实且非幂等的操作，调用方不得自动重试。

`complete&mode=count_up` 会结算真实正计时。少于 30 秒的会话会被消费，但不会创建记录；
`receive_reward=false` 时，达到记录门槛的会话仍保存为放弃记录，但不奖励番茄。

**任务切换规则：**

- 工作番茄倒计时运行中禁止切换任务。
- 正计时运行中允许切换任务，常驻通知会同步更新。
- 倒计时暂停时允许切换任务；保留已计时部分，并按新任务设置重新计算总时长。

**成功返回值：**

| 参数 | 含义 | 取值 |
| ---- | ---- | ---- |
| api_result | 接口是否成功 | 布尔值 |
| applied | 本次调用是否改变了计时状态 | 布尔值 |
| mode | 目标模式或人升当前选择的模式 | `countdown` 或 `count_up` |
| state | `mode` 对应的状态 | `running`、`paused` 或 `stopped` |
| selected_task_id | 当前计时任务 ID；未选择时为 `0` | 数字 |
| can_start_in_background | Android 当前是否允许从后台启动计时 | 布尔值 |
| countdown_state | 倒计时 canonical 状态 | `running`、`paused` 或 `stopped` |
| countdown_phase | 倒计时生命周期阶段 | `idle`、`running`、`paused`、`completing`、`completed` 或 `cancelled` |
| countdown_stage | 倒计时阶段 | `work`、`short_break` 或 `long_break` |
| countdown_session_id | 倒计时会话 ID | 文本或 null |
| countdown_total_millis | 倒计时总时长 | 毫秒 |
| countdown_remaining_millis | 倒计时剩余时长 | 毫秒 |
| count_up_state | 正计时状态 | `running`、`paused` 或 `stopped` |
| count_up_elapsed_millis | 正计时已计时长 | 毫秒 |
| battery_optimization_ignored | 人升是否已忽略电池优化 | 布尔值 |

成功的 `complete` 还会返回 `record_created`、`reward_tomatoes` 和
`settled_elapsed_millis`。

重复调用已经达到目标状态的 `start`、`pause` 或 `abandon` 会成功返回
`applied=false`。mutation 调用不提供跨进程重试去重。

**错误：**

失败时返回 `api_result=false`、`error_code` 和 `error_message`。计时接口的稳定错误码包括：

- `invalid_parameter`
- `missing_required_parameter`
- `unsupported_action_for_mode`
- `task_not_found`
- `task_change_not_allowed`
- `timer_mode_locked`
- `timer_state_conflict`
- `background_start_not_allowed`
- `timer_start_failed`
- `timer_settlement_failed`

Android 12 及以上版本中，后台 ContentProvider 调用仅在人升已获准忽略电池优化时才能启动
计时；否则会在改变计时状态前返回 `background_start_not_allowed`。通过 URL Scheme Activity
打开时，人升会先进入前台再启动计时。Android 也可能阻止第三方应用从后台拉起该 Activity；
此时人升没有收到 API 调用，因此无法返回错误。

<br/>
