package net.lifeupapp.lifeup.api.content.tasks

import android.content.Context
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.queryContent

class TasksApi(private val context: Context) {
    fun getTasks(): Result<List<Task>> {
        val tasks = mutableListOf<Task>()
        try {
            context.queryContent(ContentProviderUrl.TASK) {
                val id = it.getLong(0)
                val content = it.getString(1)
                val notes = it.getString(2)
                tasks.add(Task.builder {
                    setId(id)
                    setName(content)
                    setNotes(notes)
                })
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(tasks)
    }
}