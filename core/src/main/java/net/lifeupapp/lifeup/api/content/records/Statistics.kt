package net.lifeupapp.lifeup.api.content.records

import kotlinx.serialization.Serializable

@Serializable
data class Statistics(
    val start: Long?,
    val end: Long?,
    val tomatoes: Int,
    val pomodoroMinutes: Int,
    val focusCount: Int,
    val tasksCompleted: Int,
    val tasksTotal: Int,
    val taskCompleteRate: Double,
    val feelingsCount: Int,
    val usingDays: Int,
    val companionDays: Int,
    val interestChanged: Long,
    val coinPurchaseConsumed: Long,
)
