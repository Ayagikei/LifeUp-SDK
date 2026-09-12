package net.lifeupapp.lifeup.api.content.shop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopItemParsingTest {

    @Test
    fun shopItemBuilder_shouldExposeNewProviderFields() {
        val item = ShopItem.builder {
            setId(1L)
            setName("sword")
            setTitleColorString("#FF0000")
            setActionText("Use")
            setUnlist(true)
            setDisableUse(true)
            setPurchaseLimit("[{\"type\":1,\"count\":5}]")
            setLimitScope("shop")
        }

        assertEquals("#FF0000", item.titleColorString)
        assertEquals("Use", item.actionText)
        assertTrue(item.unlist)
        assertTrue(item.disableUse)
        assertEquals("[{\"type\":1,\"count\":5}]", item.purchaseLimit)
        assertEquals("shop", item.limitScope)
    }

    @Test
    fun shopItemBuilder_shouldDefaultNewFieldsToNullOrFalse() {
        val item = ShopItem.builder {
            setId(1L)
            setName("sword")
        }

        assertNull(item.titleColorString)
        assertNull(item.actionText)
        assertFalse(item.unlist)
        assertFalse(item.disableUse)
        assertNull(item.purchaseLimit)
        assertNull(item.limitScope)
    }
}
