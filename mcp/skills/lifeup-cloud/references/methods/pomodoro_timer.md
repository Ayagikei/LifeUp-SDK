# pomodoro_timer

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** pomodoro_timer

**Description:** Control the real Pomodoro countdown or count-up timer in LifeUp. This API starts
the same timer session as the app UI; it does not directly add Pomodoro records or tomatoes.

**Examples:**

- Start or resume the default work countdown:
  [lifeup://api/pomodoro_timer?action=start&mode=countdown](lifeup://api/pomodoro_timer?action=start&mode=countdown)
- Start the count-up timer and select task 101:
  [lifeup://api/pomodoro_timer?action=start&mode=count_up&task_id=101](lifeup://api/pomodoro_timer?action=start&mode=count_up&task_id=101)
- Pause the active countdown:
  [lifeup://api/pomodoro_timer?action=pause&mode=countdown](lifeup://api/pomodoro_timer?action=pause&mode=countdown)
- Abandon and reset the Pomodoro lifecycle:
  [lifeup://api/pomodoro_timer?action=abandon&mode=countdown](lifeup://api/pomodoro_timer?action=abandon&mode=countdown)
- Skip the current Pomodoro stage:
  [lifeup://api/pomodoro_timer?action=skip](lifeup://api/pomodoro_timer?action=skip)
- Settle a count-up timer without receiving tomato rewards:
  [lifeup://api/pomodoro_timer?action=complete&mode=count_up&receive_reward=false](lifeup://api/pomodoro_timer?action=complete&mode=count_up&receive_reward=false)
- Query both timer modes:
  [lifeup://api/pomodoro_timer?action=status](lifeup://api/pomodoro_timer?action=status)

**Parameters:**

| Parameter | Meaning | Type / values | Required | Notes |
| --------- | ------- | ------------- | -------- | ----- |
| action | Operation | `start`, `pause`, `abandon`, `skip`, `complete`, `select_task`, `status` | yes | - |
| mode | Timer mode | `countdown`, `count_up` | for `start`, `pause`, `abandon`, and `complete` | `skip` always targets the countdown. |
| stage | Countdown stage | `work`, `short_break`, `long_break` | no | Only valid with `mode=countdown`. If omitted, the active, paused, or staged-next canonical stage is used; a new lifecycle starts with `work`. |
| receive_reward | Whether to receive tomato rewards | `true` or `false` | for `complete` | Strict boolean. `complete` only supports `mode=count_up`. |
| task_id | Task ID | positive integer | no | Cannot be combined with `task_gid` or `task_name`. |
| task_gid | Task group ID | positive integer | no | Can be combined with `task_name` to narrow the match. |
| task_name | Task name | text | no | Exact match is preferred, with fuzzy matching as fallback. |
| clear_task | Clear the timer task | `true` or `false` | no | `true` cannot be combined with a task locator. |

`select_task` requires either a task locator or `clear_task=true`. `start` may include the same
task-selection parameters. Custom duration parameters are not supported: countdowns use the
current default duration or the selected task's Pomodoro duration.

`abandon&mode=countdown` is equivalent to the app's left action: it gives up the current stage,
resets the Pomodoro lifecycle, and returns to a stopped work stage. `skip` is equivalent to the
right action: it advances work to a short/long break, or a break to work, without automatically
starting the next stage. Each `skip` call is a real, non-idempotent action; callers must not retry
it automatically.

`complete&mode=count_up` settles the real count-up session. Sessions shorter than 30 seconds are
consumed without creating a record. With `receive_reward=false`, a record that meets the threshold
is still saved as abandoned but awards no tomatoes.

**Task switching rules:**

- A running work countdown rejects task changes.
- A running count-up timer allows task changes and updates its notification.
- A paused countdown allows task changes and preserves elapsed time while recalculating its total
  duration from the new task settings.

**Successful return values:**

| Parameter | Meaning | Type |
| --------- | ------- | ---- |
| api_result | Whether the API call succeeded | boolean |
| applied | Whether this call changed timer state | boolean |
| mode | Target or currently selected timer mode | `countdown` or `count_up` |
| state | State of `mode` | `running`, `paused`, or `stopped` |
| selected_task_id | Current timer task ID, or `0` | number |
| can_start_in_background | Whether Android currently allows a background timer start | boolean |
| countdown_state | Canonical countdown state | `running`, `paused`, or `stopped` |
| countdown_phase | Countdown lifecycle phase | `idle`, `running`, `paused`, `completing`, `completed`, or `cancelled` |
| countdown_stage | Canonical countdown stage | `work`, `short_break`, or `long_break` |
| countdown_session_id | Canonical countdown session ID | text or null |
| countdown_total_millis | Countdown total duration | milliseconds |
| countdown_remaining_millis | Countdown remaining duration | milliseconds |
| count_up_state | Canonical count-up state | `running`, `paused`, or `stopped` |
| count_up_elapsed_millis | Count-up elapsed duration | milliseconds |
| battery_optimization_ignored | Whether LifeUp is exempt from battery optimization | boolean |

Successful `complete` responses additionally contain `record_created`, `reward_tomatoes`, and
`settled_elapsed_millis`.

Repeated `start`, `pause`, or `abandon` calls that already match the requested state succeed with
`applied=false`. Mutating calls do not provide cross-process retry deduplication.

**Errors:**

Failures return `api_result=false`, `error_code`, and `error_message`. Timer-specific stable error
codes are:

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

On Android 12 and later, a background ContentProvider call can start a timer only when LifeUp is
allowed to ignore battery optimization. Otherwise it returns `background_start_not_allowed`
before changing timer state. Opening the URL Scheme through its Activity brings LifeUp to the
foreground before starting. Android may block a third-party app from launching that Activity from
the background; when that happens, LifeUp receives no API call and cannot return an error.

<br/>
