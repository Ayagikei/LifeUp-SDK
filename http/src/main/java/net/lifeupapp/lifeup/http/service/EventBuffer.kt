package net.lifeupapp.lifeup.http.service

import kotlinx.serialization.Serializable

@Serializable
data class CloudEvent(
    val id: Long,
    val time: Long,
    val action: String,
    val extras: Map<String, String>,
)

@Serializable
data class EventsPage(
    val latestId: Long,
    val eventWs: Boolean,
    val events: List<CloudEvent>,
)

class EventBuffer(private val capacity: Int = CAPACITY) {
    companion object {
        const val CAPACITY = 200
    }
    private val lock = Any()
    private val items = ArrayDeque<CloudEvent>()
    private var nextId = 1L

    val latestId: Long
        get() = synchronized(lock) { nextId - 1 }

    fun add(action: String, extras: Map<String, String>, time: Long = System.currentTimeMillis()): CloudEvent {
        synchronized(lock) {
            val event = CloudEvent(id = nextId++, time = time, action = action, extras = extras)
            items.addLast(event)
            while (items.size > capacity) items.removeFirst()
            return event
        }
    }

    fun since(after: Long, limit: Int = capacity): List<CloudEvent> {
        synchronized(lock) {
            return items.filter { it.id > after }.take(limit.coerceIn(1, capacity))
        }
    }
}
