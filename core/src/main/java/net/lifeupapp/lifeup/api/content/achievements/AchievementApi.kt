package net.lifeupapp.lifeup.api.content.achievements

import android.content.Context
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.achievements.category.AchievementCategory
import net.lifeupapp.lifeup.api.content.forEachContent

class AchievementApi(private val context: Context) : ContentProviderApi {

    fun listCategories(): Result<List<AchievementCategory>> {
        val categories = mutableListOf<AchievementCategory>()
        try {
            context.forEachContent(ContentProviderUrl.ACHIEVEMENT_CATEGORIES) {
                val id = it.getLongOrNull(0)
                val name = it.getStringOrNull(1)
                val desc = it.getStringOrNull(2)
                val icon = it.getStringOrNull(3)
                val isAsc = it.getIntOrNull(4)
                val sort = it.getStringOrNull(5)
                val filter = it.getStringOrNull(6)
                val order = it.getIntOrNull(7)
                val type = it.getIntOrNull(8)

                categories.add(AchievementCategory.builder {
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

        return Result.success(categories)
    }


    fun listAchievements(categoryId: Long? = null): Result<List<Achievement>> {
        val uri = buildString {
            append(ContentProviderUrl.ACHIEVEMENTS)
            if (categoryId != null) {
                append("/$categoryId")
            }
        }
        val achievements = mutableListOf<Achievement>()
        try {
            context.forEachContent(uri) {
                val id = it.getLongOrNull(0)
                val name = it.getStringOrNull(1)
                val desc = it.getStringOrNull(2)
                val icon = it.getStringOrNull(3)
                val contentCategoryId = it.getLongOrNull(4)
                val status = it.getIntOrNull(5)
                val exp = it.getIntOrNull(6)
                val coin = it.getLongOrNull(7)
                val coinVariable = it.getLongOrNull(8)
                val type = it.getIntOrNull(9)
                val progress = it.getIntOrNull(10)
                val order = it.getIntOrNull(11)
                val itemId = it.getLongOrNull(12)
                val itemAmount = it.getIntOrNull(13)

                achievements.add(Achievement.builder {
                    setId(id)
                    setName(name ?: "ERROR: name is null")
                    setDesc(desc ?: "")
                    setIconUri(icon ?: "")
                    setCategoryId(contentCategoryId ?: 0)
                    setStatus(status ?: 0)
                    setExp(exp ?: 0)
                    setCoin(coin ?: 0)
                    setCoinVariable(coinVariable ?: 0)
                    setType(type ?: 0)
                    setProgress(progress ?: 0)
                    setOrder(order ?: 0)
                    setItemId(itemId ?: 0)
                    setItemAmount(itemAmount ?: 0)
                })
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(achievements)
    }
}