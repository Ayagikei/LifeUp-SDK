package net.lifeupapp.lifeup.api.content.common

import kotlinx.serialization.Serializable

@Serializable
data class RewardItem(
    val itemId: Long,
    val itemCount: Int
)
