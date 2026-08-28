package net.lifeupapp.lifeup.api.content.records

import kotlinx.serialization.Serializable

@Serializable
data class LevelDefine(
    val id: Long? = null,
    val levelStart: Int,
    val levelEnd: Int,
    val perLevelExp: Int,
)

@Serializable
data class LevelDefines(
    val custom: Boolean,
    val levels: List<LevelDefine>,
)
