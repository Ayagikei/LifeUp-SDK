package net.lifeupapp.lifeup.api.content.achievements

import android.content.Context
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.achievements.category.AchievementCategory
import net.lifeupapp.lifeup.api.content.queryContent

class AchievementApi(private val context: Context) {

    fun getCategories(): Result<List<AchievementCategory>> {
        val tasks = mutableListOf<AchievementCategory>()
        try {
            context.queryContent(ContentProviderUrl.ACHIEVEMENT_CATEGORIES) {
                val id = it.getLongOrNull(0)
                val name = it.getStringOrNull(1)
                val desc = it.getStringOrNull(2)
                val icon = it.getStringOrNull(3)
                val isAsc = it.getIntOrNull(4)
                val sort = it.getStringOrNull(5)
                val filter = it.getStringOrNull(6)
                val order = it.getIntOrNull(7)
                val type = it.getIntOrNull(8)

                tasks.add(AchievementCategory.builder {
                    setId(id)
                    setName(name ?: "ERROR: name is null")
                    setDesc(desc ?: "")
                    setIconUri(icon ?: "")
                    setIsAsc(isAsc == 1)
                    setSort(sort ?: "")
                    setFilter(filter ?: "")
                    setOrder(order ?: 0)
                    setType(type ?: 0)
                })
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(tasks)
    }
}