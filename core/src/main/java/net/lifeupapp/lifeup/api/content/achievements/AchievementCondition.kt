package net.lifeupapp.lifeup.api.content.achievements

import kotlinx.serialization.Serializable

@Serializable
data class AchievementCondition(
    val id: Long?,
    val type: Int,
    val relatedId: Long,
    val target: Int,
    val current: Int,
    val progress: Int
)
