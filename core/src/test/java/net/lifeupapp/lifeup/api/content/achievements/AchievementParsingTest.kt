package net.lifeupapp.lifeup.api.content.achievements

import net.lifeupapp.lifeup.api.content.common.RewardItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AchievementParsingTest {

    @Test
    fun achievementBuilder_shouldExposeColor() {
        val achievement = Achievement.builder {
            setId(1L)
            setName("test")
            setColor("#00FF00")
        }

        assertEquals("#00FF00", achievement.color)
    }

    @Test
    fun achievementBuilder_shouldDefaultColorToNull() {
        val achievement = Achievement.builder {
            setId(1L)
            setName("test")
        }

        assertNull(achievement.color)
    }

    @Test
    fun achievementBuilder_shouldExposeItems() {
        val items = listOf(RewardItem(itemId = 1L, itemCount = 2))
        val achievement = Achievement.builder {
            setId(1L)
            setName("test")
            setItems(items)
        }

        assertEquals(1, achievement.items?.size)
        assertEquals(1L, achievement.items?.first()?.itemId)
        assertEquals(2, achievement.items?.first()?.itemCount)
    }
}
