package net.lifeupapp.lifeup.api.content.skills

import android.content.Context
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent

class SkillsApi(private val context: Context) : ContentProviderApi {

    fun listSkills(): Result<List<Skill>> {
        val categories = mutableListOf<Skill>()
        try {
            context.forEachContent(ContentProviderUrl.SKILLS) {
                val id = it.getLongOrNull(0)
                val name = it.getStringOrNull(1)
                val desc = it.getStringOrNull(2)
                val icon = it.getStringOrNull(3)
                val order = it.getIntOrNull(4)
                val color = it.getIntOrNull(5)
                val exp = it.getIntOrNull(6)
                val level = it.getIntOrNull(7)
                val untilNextLevelExp = it.getIntOrNull(8)
                val currentLevelExp = it.getIntOrNull(9)
                val type = it.getIntOrNull(10)

                categories.add(
                    Skill.builder {
                        setId(id)
                        setName(name ?: "ERROR: name is null")
                        setDesc(desc ?: "")
                        setIconUri(icon ?: "")
                        setOrder(order ?: 0)
                        setColorInt(color ?: 0)
                        setExp(exp ?: 0)
                        setLevel(level ?: 1)
                        setUntilNextLevelExp(untilNextLevelExp ?: 0)
                        setCurrentLevelExp(currentLevelExp ?: 0)
                        setType(type ?: 0)
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(categories)
    }
}
