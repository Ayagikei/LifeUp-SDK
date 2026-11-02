package net.lifeupapp.lifeup.http.service

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.lifeupapp.lifeup.api.content.tasks.Task
import net.lifeupapp.lifeup.http.vo.HttpResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskFrozenSerializationTest {

    @Test
    fun cloudJson_exposesFrozenTrueFalseAndUnknownWithoutChangingStatus() {
        val frozen = Task.builder {
            setName("frozen")
            setStatus(0)
            setIsFrozen(true)
        }
        val open = Task.builder {
            setName("open")
            setStatus(0)
            setIsFrozen(false)
        }
        val unknown = Task.builder {
            setName("legacy")
            setStatus(0)
        }

        val encoded = CloudJson.encodeToString(HttpResponse.success(listOf(frozen, open, unknown)))
        val decoded = CloudJson.decodeFromString<HttpResponse<List<Task>>>(encoded)
        val data = requireNotNull(decoded.data)

        assertEquals(HttpResponse.SUCCESS, decoded.code)
        assertEquals(java.lang.Boolean.TRUE, data[0].isFrozen)
        assertEquals(0, data[0].status)
        assertEquals("frozen", data[0].name)
        assertEquals(java.lang.Boolean.FALSE, data[1].isFrozen)
        assertEquals(0, data[1].status)
        assertNull(data[2].isFrozen)
        assertEquals(0, data[2].status)
        assertTrue(encoded.contains("\"isFrozen\":true"))
        assertTrue(encoded.contains("\"isFrozen\":false"))
        assertTrue(encoded.contains("\"isFrozen\":null"))
        assertFalse(encoded.contains("\"isFrozen\":\"true\""))
        assertFalse(encoded.contains("\"isFrozen\":\"false\""))

        val omitted = JsonObject(
            CloudJson.encodeToJsonElement(unknown).jsonObject.filterKeys { it != "isFrozen" }
        )
        assertFalse(omitted.containsKey("isFrozen"))
        val decodedOmitted = CloudJson.decodeFromJsonElement<Task>(omitted)
        assertNull(decodedOmitted.isFrozen)
        assertEquals(0, decodedOmitted.status)
        assertEquals("legacy", decodedOmitted.name)
        assertEquals(unknown.copy(isFrozen = null), decodedOmitted)
    }
}
