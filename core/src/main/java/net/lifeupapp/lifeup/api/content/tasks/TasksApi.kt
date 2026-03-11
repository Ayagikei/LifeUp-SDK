package net.lifeupapp.lifeup.api.content.tasks

import android.content.Context
import android.database.Cursor
import android.net.Uri
import kotlinx.serialization.Serializable
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.common.RewardItem
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.content.tasks.category.TaskCategory
import net.lifeupapp.lifeup.api.utils.decodeFromStringOrNull
import net.lifeupapp.lifeup.api.utils.getBooleanOrNull
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull
import net.lifeupapp.lifeup.api.utils.getStringOrNull
import net.lifeupapp.lifeup.api.utils.json

class TasksApi(private val context: Context) : ContentProviderApi {

    fun listCategories(): Result<List<TaskCategory>> {
        val categories = mutableListOf<TaskCategory>()
        try {
            context.forEachContent(ContentProviderUrl.TASKS_CATEGORIES) {
                val id = it.getLongOrNull("_ID")
                val name = it.getStringOrNull("name")
                val isAsc = it.getBooleanOrNull("isAsc")
                val sort = it.getStringOrNull("sort")
                val filter = it.getStringOrNull("filter")
                val order = it.getIntOrNull("order")
                val status = it.getIntOrNull("status")
                val type = it.getIntOrNull("type")

                categories.add(
                    TaskCategory.builder {
                        setId(id)
                        setName(name ?: "ERROR: name is null")
                        setIsAsc(isAsc ?: false)
                        setSort(sort ?: "")
                        setFilter(filter ?: "")
                        setOrder(order ?: 0)
                        setStatus(status ?: 0)
                        setType(type ?: 0)
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(categories)
    }

    fun listTasks(categoryId: Long?): Result<List<Task>> {
        val tasks = mutableListOf<Task>()
        try {
            val uri = buildString {
                append(ContentProviderUrl.TASK)
                if (categoryId != null) {
                    append("/$categoryId")
                }
            }
            context.forEachContent(uri) {
                tasks.add(it.toTask(includeEndTime = false, includeRepeatEndCondition = true))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(tasks)
    }

    fun listHistory(
        offset: Int = 0,
        limit: Int = 100,
        filterGid: Long? = null
    ): Result<List<Task>> {
        val tasks = mutableListOf<Task>()
        try {
            val uri = Uri.parse(ContentProviderUrl.HISTORY).buildUpon()
                .appendQueryParameter("offset", offset.toString())
                .appendQueryParameter("limit", limit.toString())
                .apply {
                    if (filterGid != null) {
                        appendQueryParameter("gid", filterGid.toString())
                    }
                }
                .build()

            context.forEachContent(uri.toString()) {
                tasks.add(it.toTask(includeEndTime = true, includeRepeatEndCondition = false))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(tasks)
    }
}

private fun Cursor.toTask(includeEndTime: Boolean, includeRepeatEndCondition: Boolean): Task {
    val id = getLongOrNull("_ID")
    val gid = getLongOrNull("_GID")
    val name = getStringOrNull("name")
    val notes = getStringOrNull("notes")
    val status = getIntOrNull("status")
    val startTime = getLongOrNull("startTime")
    val deadline = getLongOrNull("deadline")
    val remindTime = getLongOrNull("remindTime")
    val frequency = getIntOrNull("frequency")
    val exp = getIntOrNull("exp")
    val skillIds = getStringOrNull("skillIds")
    val coin = getLongOrNull("coin")
    val coinVariable = getLongOrNull("coinVariable")
    val itemId = getLongOrNull("itemId")
    val itemAmount = getIntOrNull("itemCount")
    val words = getStringOrNull("words")
    val categoryId = getLongOrNull("categoryId")
    val order = getIntOrNull("order")
    val nameExtended = getStringOrNull("name_extended")
    val endTime = if (includeEndTime) getLongOrNull("endTime") else null
    val itemsJson = getStringOrNull("items")
    val subTasksJson = getStringOrNull("subTasks")
    val countProgress = buildCountProgress(
        currentCount = getIntOrNull("countProgressCurrent"),
        targetCount = getIntOrNull("countProgressTarget")
    )
    val repeatEndCondition = if (includeRepeatEndCondition) {
        buildRepeatEndCondition(
            mode = getStringOrNull("repeatEndMode"),
            behavior = getStringOrNull("repeatEndBehavior"),
            targetCycleCount = getIntOrNull("repeatEndTargetCycleCount"),
            endDateMillis = getLongOrNull("repeatEndDateMillis"),
            inclusive = getBooleanOrNull("repeatEndInclusive")
        )
    } else {
        null
    }

    return Task.builder {
        setId(id)
        setGid(gid)
        setName(name ?: "ERROR: name is null")
        setNotes(notes ?: "")
        setStatus(status ?: 0)
        setStartTime(startTime ?: 0)
        setDeadline(deadline ?: 0)
        setRemindTime(remindTime ?: 0)
        setFrequency(frequency ?: 0)
        setExp(exp ?: 0)
        setSkillIds(parseSkillIds(skillIds))
        setCoin(coin ?: 0L)
        setCoinVariable(coinVariable ?: 0L)
        setItemId(itemId ?: 0)
        setItemAmount(itemAmount ?: 0)
        setWords(words ?: "")
        setCategoryId(categoryId)
        setOrder(order ?: 0)
        setNameExtended(nameExtended ?: "")
        setEndTime(endTime ?: 0)
        setItems(itemsJson?.decodeFromStringOrNull<List<RewardItem>>() ?: emptyList())
        setSubTasks(parseSubTasksJson(subTasksJson))
        setCountProgress(countProgress)
        setRepeatEndCondition(repeatEndCondition)
    }
}

internal fun buildCountProgress(currentCount: Int?, targetCount: Int?): TaskCountProgress? {
    if (currentCount == null || targetCount == null) {
        return null
    }
    return TaskCountProgress(
        currentCount = currentCount,
        targetCount = targetCount
    )
}

internal fun buildRepeatEndCondition(
    mode: String?,
    behavior: String?,
    targetCycleCount: Int?,
    endDateMillis: Long?,
    inclusive: Boolean?
): TaskRepeatEndCondition? {
    val normalizedMode = mode?.trim()?.takeUnless { it.isEmpty() } ?: return null
    val normalizedBehavior = behavior?.trim()?.takeUnless { it.isEmpty() } ?: return null

    when (normalizedMode) {
        "COUNT" -> if (targetCycleCount == null || targetCycleCount <= 0) {
            return null
        }

        "DATE" -> if (endDateMillis == null) {
            return null
        }
    }

    return TaskRepeatEndCondition(
        mode = normalizedMode,
        behavior = normalizedBehavior,
        targetCycleCount = targetCycleCount,
        endDateMillis = endDateMillis,
        inclusive = inclusive ?: false
    )
}

internal fun parseSkillIds(skillIds: String?): List<Long> {
    if (skillIds.isNullOrBlank()) {
        return emptyList()
    }
    return runCatching {
        json.decodeFromString<List<Long>>(skillIds)
    }.getOrDefault(emptyList())
}

@Serializable
private data class ProviderSubTask(
    val id: Long? = null,
    val gid: Long? = null,
    val todo: String = "",
    val status: Int? = null,
    val remindTime: Long? = null,
    val exp: Int = 0,
    val coin: Long? = null,
    val coinVariable: Long? = null,
    val items: List<RewardItem> = emptyList(),
    val order: Int? = 0,
    val autoUseItem: Boolean? = false
)

internal fun parseSubTasksJson(subTasksJson: String?): List<SubTask> {
    if (subTasksJson.isNullOrBlank()) {
        return emptyList()
    }

    return subTasksJson.decodeFromStringOrNull<List<ProviderSubTask>>()
        ?.map {
            SubTask(
                id = it.id ?: 0L,
                gid = it.gid ?: 0L,
                todo = it.todo,
                status = it.status ?: 0,
                remindTime = it.remindTime,
                exp = it.exp,
                coin = it.coin,
                coinVariable = it.coinVariable,
                items = it.items,
                order = it.order,
                autoUseItem = it.autoUseItem
            )
        }
        ?: emptyList()
}
