package net.lifeupapp.lifeup.api.content.shop

import android.content.Context
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.content.shop.category.ShopCategory

class ItemsApi(private val context: Context) : ContentProviderApi {

    fun listCategories(): Result<List<ShopCategory>> {
        val categories = mutableListOf<ShopCategory>()
        try {
            context.forEachContent(ContentProviderUrl.SHOP_CATEGORIES) {
                val id = it.getLongOrNull(0)
                val name = it.getStringOrNull(1)
                val isAsc = it.getIntOrNull(2)
                val sort = it.getStringOrNull(3)
                val order = it.getIntOrNull(4)

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
                val id = it.getLongOrNull(0)
                val name = it.getStringOrNull(1)
                val desc = it.getStringOrNull(2)
                val icon = it.getStringOrNull(3)
                val itemCategoryId = it.getLongOrNull(4)
                val stockNumber = it.getIntOrNull(5)
                val ownNumber = it.getIntOrNull(6)
                val price = it.getLongOrNull(7)
                val order = it.getIntOrNull(8)
                val disablePurchase = it.getIntOrNull(9)

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
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(items)
    }
}
