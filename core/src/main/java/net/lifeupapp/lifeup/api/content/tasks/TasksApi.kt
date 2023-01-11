package net.lifeupapp.lifeup.api.content.tasks

import android.content.Context
import android.net.Uri
import net.lifeupapp.lifeup.api.content.ContentProviderUrl

class TasksApi(private val context: Context) {
    fun getTasks(): List<Task> {
        context.contentResolver.query(
            Uri.parse(ContentProviderUrl.TASK), null, null, null, null
        ).use {
            it ?: return@use
            it.moveToFirst()

            val tasks = mutableListOf<Task>()

            while (it.isAfterLast.not()) {
                val id = it.getLong(0)
                val content = it.getString(1)
                val notes = it.getString(2)
                tasks.add(Task.builder {
                    setId(id)
                    setName(content)
                    setNotes(notes)
                })
                it.moveToNext()
            }
        }
        return emptyList()
    }
}