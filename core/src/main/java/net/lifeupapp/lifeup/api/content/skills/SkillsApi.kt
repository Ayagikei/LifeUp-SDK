package net.lifeupapp.lifeup.api.content.skills

import android.content.Context
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull
import net.lifeupapp.lifeup.api.utils.getStringOrNull

class SkillsApi(private val context: Context) : ContentProviderApi {

    fun listSkills(): Result<List<Skill>> {
        val skills = mutableListOf<Skill>()
        try {
            context.forEachContent(ContentProviderUrl.SKILLS) {
                val id = it.getLongOrNull("_ID")
                val name = it.getStringOrNull("name")
                val desc = it.getStringOrNull("desc")
                val icon = it.getStringOrNull("icon")
                val order = it.getIntOrNull("order")
                val color = it.getIntOrNull("color")
                val exp = it.getIntOrNull("exp")
                val level = it.getIntOrNull("level")
                val untilNextLevelExp = it.getIntOrNull("until_next_level_exp")
                val currentLevelExp = it.getIntOrNull("current_level_exp")
                val type = it.getIntOrNull("type")

                skills.add(
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

        return Result.success(skills)
    }
}