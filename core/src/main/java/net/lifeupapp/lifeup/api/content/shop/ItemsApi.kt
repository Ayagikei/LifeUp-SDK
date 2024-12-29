package net.lifeupapp.lifeup.api.content.shop

import android.content.Context
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.content.shop.category.ShopCategory
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull
import net.lifeupapp.lifeup.api.utils.getStringOrNull

class ItemsApi(private val context: Context) : ContentProviderApi {

    fun listCategories(): Result<List<ShopCategory>> {
        val categories = mutableListOf<ShopCategory>()
        try {
            context.forEachContent(ContentProviderUrl.SHOP_CATEGORIES) {
                val id = it.getLongOrNull("_ID")
                val name = it.getStringOrNull("name")
                val isAsc = it.getIntOrNull("isAsc")
                val sort = it.getStringOrNull("sort")
                val order = it.getIntOrNull("order")

                categories.add(
                    ShopCategory.builder {
                        setId(id)
                        setName(name ?: "ERROR: name is null")
                        setIsAsc(isAsc == 1)
                        setSort(sort ?: "")
                        setOrder(order ?: 0)
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(categories)
    }

    fun listItems(categoryId: Long?): Result<List<ShopItem>> {
        val items = mutableListOf<ShopItem>()
        try {
            val uri = buildString {
                append(ContentProviderUrl.ITEMS)
                if (categoryId != null) {
                    append("/$categoryId")
                }
            }
            context.forEachContent(uri) {
                val id = it.getLongOrNull("_ID")
                val name = it.getStringOrNull("name")
                val desc = it.getStringOrNull("desc")
                val icon = it.getStringOrNull("icon")
                val itemCategoryId = it.getLongOrNull("categoryId")
                val stockNumber = it.getIntOrNull("stockNumber")
                val ownNumber = it.getIntOrNull("ownNumber")
                val price = it.getLongOrNull("price")
                val order = it.getIntOrNull("order")
                val disablePurchase = it.getIntOrNull("disablePurchase")
                val maxPurchaseNumber = it.getIntOrNull("maxPurchaseNumber")

                items.add(
                    ShopItem.builder {
                        setId(id)
                        setName(name ?: "ERROR: name is null")
                        setDesc(desc ?: "")
                        setIconUri(icon ?: "")
                        setCategoryId(itemCategoryId)
                        setStockNumber(stockNumber ?: 0)
                        setOwnNumber(ownNumber ?: 0)
                        setPrice(price ?: 0)
                        setOrder(order ?: 0)
                        setDisablePurchase(disablePurchase == 1)
                        setMaxPurchaseNumber(maxPurchaseNumber)
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(items)
    }

    fun listItemsByIds(ids: List<Long>): Result<List<ShopItem>> {
        val items = mutableListOf<ShopItem>()
        try {
            val uri = buildString {
                append(ContentProviderUrl.ITEMS)
                if (ids.isNotEmpty()) {
                    append("?${ids.joinToString("&") { "id=$it" }}")
                }
            }
            context.forEachContent(uri) {
                val id = it.getLongOrNull("_ID")
                val name = it.getStringOrNull("name")
                val desc = it.getStringOrNull("desc")
                val icon = it.getStringOrNull("icon")
                val itemCategoryId = it.getLongOrNull("categoryId")
                val stockNumber = it.getIntOrNull("stockNumber")
                val ownNumber = it.getIntOrNull("ownNumber")
                val price = it.getLongOrNull("price")
                val order = it.getIntOrNull("order")
                val disablePurchase = it.getIntOrNull("disablePurchase")
                val maxPurchaseNumber = it.getIntOrNull("maxPurchaseNumber")

                items.add(
                    ShopItem.builder {
                        setId(id)
                        setName(name ?: "ERROR: name is null")
                        setDesc(desc ?: "")
                        setIconUri(icon ?: "")
                        setCategoryId(itemCategoryId)
                        setStockNumber(stockNumber ?: 0)
                        setOwnNumber(ownNumber ?: 0)
                        setPrice(price ?: 0)
                        setOrder(order ?: 0)
                        setDisablePurchase(disablePurchase == 1)
                        setMaxPurchaseNumber(maxPurchaseNumber)
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(items)
    }
}
