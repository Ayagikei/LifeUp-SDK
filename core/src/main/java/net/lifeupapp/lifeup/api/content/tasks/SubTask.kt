package net.lifeupapp.lifeup.api.content.tasks

import kotlinx.serialization.Serializable
import net.lifeupapp.lifeup.api.content.common.RewardItem

@Serializable
data class SubTask(
    val id: Long,
    val gid: Long,
    val todo: String,
    val status: Int,
    val remindTime: Long? = null,
    val exp: Int,
    val coin: Long? = null,
    val coinVariable: Long? = null,
    val items: List<RewardItem> = emptyList<RewardItem>(),
    val order: Int? = 0,
    val autoUseItem: Boolean? = false
)