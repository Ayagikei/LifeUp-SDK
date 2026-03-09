package net.lifeupapp.lifeup.api.content.achievements

import net.lifeupapp.lifeup.api.content.achievements.category.AchievementCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementApiTest {

    @Test
    fun resolveAchievementCategoryIds_shouldExpandAllCategoriesWhenCategoryIdIsNull() {
        val categories = listOf(
            AchievementCategory.builder {
                setId(1L)
                setName("A")
                setDesc("")
                setIconUri("")
                setIsAsc(false)
                setSort("")
                setFilter("")
                setOrder(0)
                setType(0)
            },
            AchievementCategory.builder {
                setId(2L)
                setName("B")
                setDesc("")
                setIconUri("")
                setIsAsc(false)
                setSort("")
                setFilter("")
                setOrder(0)
                setType(0)
            }
        )

        assertEquals(listOf(1L, 2L), resolveAchievementCategoryIds(null, categories))
        assertEquals(listOf(9L), resolveAchievementCategoryIds(9L, categories))
    }
}
