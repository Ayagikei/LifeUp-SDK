package net.lifeupapp.lifeup.api.content.records

import kotlinx.serialization.Serializable

@Serializable
data class CoinRecord(
    val id: Long?,
    val time: Long?,
    val value: Long,
    val isDecrease: Boolean,
    val totalValue: Long,
    val resCode: Int,
    val relatedId: Long?,
    val content: String?,
    val savingBalance: Long?,
)
