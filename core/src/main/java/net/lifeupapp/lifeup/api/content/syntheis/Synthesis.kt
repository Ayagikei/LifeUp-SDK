package net.lifeupapp.lifeup.api.content.syntheis

import kotlinx.serialization.Serializable

@Serializable
data class Synthesis(
    val id: Long?,
    val name: String,
    val desc: String,
    val input: String,
    val output: String,
    val categoryId: Long?,
    val canSynthesisTimes: Int
) {
    class Builder {
        private var id: Long? = null
        private var name: String = ""
        private var desc: String = ""
        private var input: String = ""
        private var output: String = ""
        private var categoryId: Long? = null
        private var canSynthesisTimes: Int = 0

        fun setId(id: Long?) = apply { this.id = id }
        fun setName(name: String) = apply { this.name = name }
        fun setDesc(desc: String) = apply { this.desc = desc }
        fun setInput(input: String) = apply { this.input = input }
        fun setOutput(output: String) = apply { this.output = output }
        fun setCategoryId(categoryId: Long?) = apply { this.categoryId = categoryId }
        fun setCanSynthesisTimes(canSynthesisTimes: Int) =
            apply { this.canSynthesisTimes = canSynthesisTimes }

        fun build(): Synthesis {
            return Synthesis(
                id = id,
                name = name,
                desc = desc,
                input = input,
                output = output,
                categoryId = categoryId,
                canSynthesisTimes = canSynthesisTimes
            )
        }
    }

    companion object {
        fun builder(block: Builder.() -> Unit): Synthesis {
            return Builder().apply(block).build()
        }
    }
}
