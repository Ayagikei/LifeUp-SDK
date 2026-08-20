package net.lifeupapp.lifeup.api.content.records

import kotlinx.serialization.Serializable

@Serializable
data class StepRecord(
    val id: Long?,
    val date: Long?,
    val dailyStepCount: Long,
    val totalStepCount: Long,
    val isGotReward: Boolean,
    val isUserInput: Boolean,
    val rewardLevel: Int?,
)
