package net.lifeupapp.lifeup.http.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventBufferTest {
    @Test
    fun dropsOldestWhenFull() {
        val buffer = EventBuffer(capacity = 2)
        buffer.add("a", emptyMap(), time = 1)
        buffer.add("b", emptyMap(), time = 2)
        buffer.add("c", emptyMap(), time = 3)
        val events = buffer.since(0)
        assertEquals(listOf("b", "c"), events.map { it.action })
        assertEquals(3, buffer.latestId)
    }

    @Test
    fun sinceFiltersById() {
        val buffer = EventBuffer()
        buffer.add("a", mapOf("k" to "1"), time = 1)
        buffer.add("b", emptyMap(), time = 2)
        val events = buffer.since(1)
        assertEquals(1, events.size)
        assertEquals("b", events[0].action)
        assertTrue(buffer.since(99).isEmpty())
    }
}
