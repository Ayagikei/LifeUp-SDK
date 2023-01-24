package net.lifeupapp.lifeup.api.content.skills

import kotlinx.serialization.Serializable


@Serializable
data class Skill(
    val id: Long?,
    val name: String,
    val desc: String,
    val icon: String,
    val order: Int,
    val color: Int
) {
    class Builder {
        private var id: Long? = null
        private var name: String = ""
        private var desc: String = ""
        private var icon: String = ""
        private var order: Int = 0
        private var color: Int = 0

        fun setId(id: Long?) = apply { this.id = id }
        fun setName(name: String) = apply { this.name = name }
        fun setDesc(desc: String) = apply { this.desc = desc }
        fun setIconUri(icon: String) = apply { this.icon = icon }
        fun setOrder(order: Int) = apply { this.order = order }
        fun setColorInt(color: Int) =
            apply { this.color = color }

        fun build(): Skill {
            return Skill(
                id = id,
                name = name,
                desc = desc,
                icon = icon,
                order = order,
                color = color
            )
        }
    }

    companion object {
        fun builder(block: Builder.() -> Unit): Skill {
            return Builder().apply(block).build()
        }
    }
}