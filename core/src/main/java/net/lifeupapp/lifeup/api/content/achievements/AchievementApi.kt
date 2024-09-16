package net.lifeupapp.lifeup.api.content.achievements

import android.content.Context
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.achievements.category.AchievementCategory
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull
import net.lifeupapp.lifeup.api.utils.getStringOrNull

class AchievementApi(private val context: Context) : ContentProviderApi {

    fun listCategories(): Result<List<AchievementCategory>> {
        val categories = mutableListOf<AchievementCategory>()
        try {
            context.forEachContent(ContentProviderUrl.ACHIEVEMENT_CATEGORIES) {
                val id = it.getLongOrNull("_ID")
                val name = it.getStringOrNull("name")
                val desc = it.getStringOrNull("desc")
                val icon = it.getStringOrNull("icon")
                val isAsc = it.getIntOrNull("isAsc")
                val sort = it.getStringOrNull("sort")
                val filter = it.getStringOrNull("filter")
                val order = it.getIntOrNull("order")
                val type = it.getIntOrNull("type")

                categories.add(
                    AchievementCategory.builder {
                        setId(id)
                        setName(name ?: "ERROR: name is null")
                        setDesc(desc ?: "")
                        setIconUri(icon ?: "")
                        setIsAsc(isAsc == 1)
                        setSort(sort ?: "")
                        setFilter(filter ?: "")
                        setOrder(order ?: 0)
                        setType(type ?: 0)
                    }
                )
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
                val id = it.getLongOrNull("_ID")
                val name = it.getStringOrNull("name")
                val desc = it.getStringOrNull("desc")
                val icon = it.getStringOrNull("icon")
                val contentCategoryId = it.getLongOrNull("categoryId")
                val status = it.getIntOrNull("status")
                val exp = it.getIntOrNull("exp")
                val coin = it.getLongOrNull("coin")
                val coinVariable = it.getLongOrNull("coinVariable")
                val type = it.getIntOrNull("type")
                val progress = it.getIntOrNull("progress")
                val order = it.getIntOrNull("order")
                val itemId = it.getLongOrNull("itemId")
                val itemAmount = it.getIntOrNull("itemAmount")
                val unlockedTime = it.getLongOrNull("unlocked_time")

                achievements.add(
                    Achievement.builder {
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
                        setUnlockedTime(unlockedTime ?: 0)
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(achievements)
    }
}