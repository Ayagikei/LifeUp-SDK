package net.lifeupapp.lifeup.api.content.tasks

import kotlinx.serialization.Serializable

@Serializable
data class TaskCountProgress(
    val currentCount: Int,
    val targetCount: Int
)
