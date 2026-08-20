package net.lifeupapp.lifeup.api.content.records

import kotlinx.serialization.Serializable

@Serializable
data class ExpRecord(
    val id: Long?,
    val time: Long?,
    val value: Int,
    val isDecrease: Boolean,
    val totalValue: Int,
    val resCode: Int,
    val relatedId: Long?,
    val content: String?,
    val skillIds: List<Long>,
    val relatedAttributes: List<String>,
)
