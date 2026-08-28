package net.lifeupapp.lifeup.api.content.skills

import kotlinx.serialization.Serializable

@Serializable
data class SkillGroup(
    val id: Long?,
    val content: String,
    val order: Int,
    val collapsed: Boolean
)
