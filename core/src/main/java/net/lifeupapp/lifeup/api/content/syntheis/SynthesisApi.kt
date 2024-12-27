package net.lifeupapp.lifeup.api.content.syntheis

import android.content.Context
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.content.syntheis.category.SynthesisCategory
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull
import net.lifeupapp.lifeup.api.utils.getStringOrNull

class SynthesisApi(private val context: Context) : ContentProviderApi {

    fun listCategories(categoryId: Long? = null): Result<List<SynthesisCategory>> {
        val categories = mutableListOf<SynthesisCategory>()
        try {
            val uri = buildString {
                append(ContentProviderUrl.SYNTHESIS_CATEGORIES)
                if (categoryId != null) {
                    append("/$categoryId")
                }
            }
            context.forEachContent(uri) {
                val id = it.getLongOrNull("_ID")
                val name = it.getStringOrNull("name")
                val isAsc = it.getIntOrNull("isAsc")
                val sort = it.getStringOrNull("sort")
                val order = it.getIntOrNull("order")

                categories.add(
                    SynthesisCategory.builder {
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

    fun listSynthesis(categoryId: Long? = null): Result<List<Synthesis>> {
        val synthesisList = mutableListOf<Synthesis>()
        try {
            val uri = buildString {
                append(ContentProviderUrl.SYNTHESIS)
                if (categoryId != null) {
                    append("/$categoryId")
                }
            }
            context.forEachContent(uri) {
                val id = it.getLongOrNull("_ID")
                val name = it.getStringOrNull("name")
                val desc = it.getStringOrNull("desc")
                val inputJson = it.getStringOrNull("inputItemsJson")
                val outputJson = it.getStringOrNull("outputItemsJson")
                val itemCategoryId = it.getLongOrNull("categoryId")
                val canSynthesisTimes = it.getIntOrNull("canSynthesisTimes")

                synthesisList.add(
                    Synthesis.builder {
                        setId(id)
                        setName(name ?: "ERROR: name is null")
                        setDesc(desc ?: "")
                        setInput(inputJson ?: "[]")
                        setOutput(outputJson ?: "[]")
                        setCategoryId(itemCategoryId)
                        setCanSynthesisTimes(canSynthesisTimes ?: 0)
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(synthesisList)
    }
}