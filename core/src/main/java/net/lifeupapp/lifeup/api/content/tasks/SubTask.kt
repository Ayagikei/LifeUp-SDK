package net.lifeupapp.lifeup.api.content.tasks

import kotlinx.serialization.Serializable
import net.lifeupapp.lifeup.api.content.common.RewardItem

@Serializable
data class SubTask(
    val id: Long,
    val gid: Long,
    val todo: String,
    val status: Int,
    val remindTime: Long?,
    val exp: Int,
    val coin: Long?,
    val coinVariable: Long?,
    val items: List<RewardItem>,
    val order: Int,
    val autoUseItem: Boolean?,
)