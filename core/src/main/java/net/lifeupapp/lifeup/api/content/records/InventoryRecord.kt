package net.lifeupapp.lifeup.api.content.records

import kotlinx.serialization.Serializable

@Serializable
data class InventoryRecord(
    val id: Long?,
    val time: Long?,
    val itemId: Long?,
    val itemName: String?,
    val changeNumber: Int,
    val isDecrease: Boolean,
    val resCode: Int,
    val desc: String?,
)
