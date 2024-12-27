package net.lifeupapp.lifeup.api.content.tasks

import android.content.Context
import android.net.Uri
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.common.RewardItem
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.content.tasks.category.TaskCategory
import net.lifeupapp.lifeup.api.utils.decodeFromStringOrNull
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
                val isAsc = it.getIntOrNull("isAsc")
                val sort = it.getStringOrNull("sort")
                val filter = it.getStringOrNull("filter")
                val order = it.getIntOrNull("order")
                val status = it.getIntOrNull("status")
                val type = it.getIntOrNull("type")

                categories.add(
                    TaskCategory.builder {
                        setId(id)
                        setName(name ?: "ERROR: name is null")
                        setIsAsc(isAsc == 1)
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
                val id = it.getLongOrNull("_ID")
                val gid = it.getLongOrNull("_GID")
                val name = it.getStringOrNull("name")
                val notes = it.getStringOrNull("notes")
                val status = it.getIntOrNull("status")
                val startTime = it.getLongOrNull("startTime")
                val deadline = it.getLongOrNull("deadline")
                val remindTime = it.getLongOrNull("remindTime")
                val frequency = it.getIntOrNull("frequency")
                val exp = it.getIntOrNull("exp")
                val skillIds = it.getStringOrNull("skillIds")
                val coin = it.getLongOrNull("coin")
                val coinVariable = it.getLongOrNull("coinVariable")
                val itemId = it.getLongOrNull("itemId")
                val itemAmount = it.getIntOrNull("itemCount")
                val words = it.getStringOrNull("words")
                val itemCategoryId = it.getLongOrNull("categoryId")
                val order = it.getIntOrNull("order")
                val nameExtended = it.getStringOrNull("name_extended")
                val itemsJson = it.getStringOrNull("items")
                val subTasksJson = it.getStringOrNull("subTasks")

                tasks.add(
                    Task.builder {
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
                        json.decodeFromString<List<Long>?>(skillIds ?: "[]")?.let {
                            setSkillIds(it)
                        }
                        setCoin(coin ?: 0L)
                        setCoinVariable(coinVariable ?: 0L)
                        setItemId(itemId ?: 0)
                        setItemAmount(itemAmount ?: 0)
                        setWords(words ?: "")
                        setCategoryId(itemCategoryId)
                        setOrder(order ?: 0)
                        setNameExtended(nameExtended ?: "")
                        setItems(
                            itemsJson?.decodeFromStringOrNull<List<RewardItem>>() ?: emptyList()
                        )
                        setSubTasks(
                            subTasksJson?.decodeFromStringOrNull<List<SubTask>>() ?: emptyList()
                        )
                    }
                )
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
                val id = it.getLongOrNull("_ID")
                val gid = it.getLongOrNull("_GID")
                val name = it.getStringOrNull("name")
                val notes = it.getStringOrNull("notes")
                val status = it.getIntOrNull("status")
                val startTime = it.getLongOrNull("startTime")
                val deadline = it.getLongOrNull("deadline")
                val remindTime = it.getLongOrNull("remindTime")
                val frequency = it.getIntOrNull("frequency")
                val exp = it.getIntOrNull("exp")
                val skillIds = it.getStringOrNull("skillIds")
                val coin = it.getLongOrNull("coin")
                val coinVariable = it.getLongOrNull("coinVariable")
                val itemId = it.getLongOrNull("itemId")
                val itemAmount = it.getIntOrNull("itemCount")
                val words = it.getStringOrNull("words")
                val categoryId = it.getLongOrNull("categoryId")
                val endTime = it.getLongOrNull("endTime")
                val itemsJson = it.getStringOrNull("items")
                val subTasksJson = it.getStringOrNull("subTasks")

                tasks.add(
                    Task.builder {
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
                        Json.decodeFromString<List<Long>?>(skillIds ?: "[]")?.let {
                            setSkillIds(it)
                        }
                        setCoin(coin ?: 0L)
                        setCoinVariable(coinVariable ?: 0L)
                        setItemId(itemId ?: 0)
                        setItemAmount(itemAmount ?: 0)
                        setWords(words ?: "")
                        setCategoryId(categoryId)
                        setEndTime(endTime ?: 0)
                        setItems(
                            itemsJson?.decodeFromStringOrNull<List<RewardItem>>() ?: emptyList()
                        )
                        setSubTasks(
                            subTasksJson?.decodeFromStringOrNull<List<SubTask>>() ?: emptyList()
                        )
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(tasks)
    }
}
