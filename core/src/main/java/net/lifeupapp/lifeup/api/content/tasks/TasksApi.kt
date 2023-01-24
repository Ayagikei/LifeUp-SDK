package net.lifeupapp.lifeup.api.content.tasks

import android.content.Context
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.queryContent

class TasksApi(private val context: Context) {

    fun getTasks(categoryId: Long?): Result<List<Task>> {
        val tasks = mutableListOf<Task>()
        try {
            val uri = buildString {
                append(ContentProviderUrl.TASK)
                if (categoryId != null) {
                    append("/$categoryId")
                }
            }
            context.queryContent(uri) {
                val id = it.getLongOrNull(0)
                val gid = it.getLongOrNull(1)
                val name = it.getStringOrNull(2)
                val notes = it.getStringOrNull(3)
                val status = it.getIntOrNull(4)
                val startTime = it.getLongOrNull(5)
                val deadline = it.getLongOrNull(6)
                val remindTime = it.getLongOrNull(7)
                val frequency = it.getIntOrNull(8)
                val exp = it.getIntOrNull(9)
                val skillIds = it.getStringOrNull(10)
                val coin = it.getLongOrNull(11)
                val coinVariable = it.getLongOrNull(12)
                val itemId = it.getLongOrNull(13)
                val itemAmount = it.getIntOrNull(14)
                val words = it.getStringOrNull(15)
                val itemCategoryId = it.getLongOrNull(16)

                tasks.add(Task.builder {
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
                    // skillIds JSON -> List<Long>
                    Json.decodeFromString<List<Long>?>(skillIds ?: "[]")?.let {
                        setSkillIds(it)
                    }
                    setCoin(coin ?: 0L)
                    setCoinVariable(coinVariable ?: 0L)
                    setItemId(itemId ?: 0)
                    setItemAmount(itemAmount ?: 0)
                    setWords(words ?: "")
                    setCategoryId(itemCategoryId)
                })
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(tasks)
    }
}