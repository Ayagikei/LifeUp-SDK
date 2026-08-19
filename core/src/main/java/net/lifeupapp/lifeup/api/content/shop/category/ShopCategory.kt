package net.lifeupapp.lifeup.api.content.shop.category

import kotlinx.serialization.Serializable


@Serializable
data class ShopCategory(
    val id: Long?,
    val name: String,
    val isAsc: Boolean,
    val sort: String,
    val order: Int,
    val hidden: Boolean = false,
    val inventoryHidden: Boolean = false
) {
    class Builder {
        private var id: Long? = null
        private var name: String = ""
        private var isAsc: Boolean = false
        private var sort: String = ""
        private var order: Int = 0
        private var hidden: Boolean = false
        private var inventoryHidden: Boolean = false

        fun setId(id: Long?) = apply { this.id = id }
        fun setName(name: String) = apply { this.name = name }
        fun setIsAsc(isAsc: Boolean) = apply { this.isAsc = isAsc }
        fun setSort(sort: String) = apply { this.sort = sort }
        fun setOrder(order: Int) = apply { this.order = order }
        fun setHidden(hidden: Boolean) = apply { this.hidden = hidden }
        fun setInventoryHidden(inventoryHidden: Boolean) = apply { this.inventoryHidden = inventoryHidden }

        fun build(): ShopCategory {
            return ShopCategory(
                id = id,
                name = name,
                isAsc = isAsc,
                sort = sort,
                order = order,
                hidden = hidden,
                inventoryHidden = inventoryHidden,
            )
        }
    }

    companion object {
        fun builder(block: Builder.() -> Unit): ShopCategory {
            return Builder().apply(block).build()
        }
    }
}
