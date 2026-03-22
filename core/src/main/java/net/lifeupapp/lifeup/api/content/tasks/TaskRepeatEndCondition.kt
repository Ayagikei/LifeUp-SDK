package net.lifeupapp.lifeup.api.content.tasks

import kotlinx.serialization.Serializable

@Serializable
data class TaskRepeatEndCondition(
    val mode: String,
    val behavior: String,
    val targetCycleCount: Int? = null,
    val endDateMillis: Long? = null,
    val inclusive: Boolean = false
)
