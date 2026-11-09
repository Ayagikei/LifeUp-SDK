package net.lifeupapp.lifeup.http.service

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import net.lifeupapp.lifeup.api.content.tasks.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskListRouteTest {

    @Test
    fun withSubTasks_parsesOnlyExactFalse() {
        assertTrue(includeSubTasks(null))
        assertTrue(includeSubTasks("true"))
        assertTrue(includeSubTasks("TRUE"))
        assertTrue(includeSubTasks("0"))
        assertTrue(includeSubTasks(""))
        assertTrue(includeSubTasks("false "))
        assertFalse(includeSubTasks("false"))
        assertFalse(includeSubTasks("FALSE"))
        assertFalse(includeSubTasks("False"))
    }

    @Test
    fun taskRoutes_passCategoryAndSubTaskFlagWithoutChangingEnvelope() = testApplication {
        val seen = mutableListOf<Pair<Long?, Boolean>>()
        application {
            install(ContentNegotiation) { json(CloudJson) }
            installResourceConditionalGet()
            routing {
                registerTaskListRoutes { categoryId, withSubTasks ->
                    seen += categoryId to withSubTasks
                    Result.success(listOf(Task.builder { setName("kept") }))
                }
            }
        }

        val all = client.get("/tasks")
        val disabled = client.get("/tasks?withSubTasks=false")
        val upper = client.get("/tasks?withSubTasks=False")
        val enabled = client.get("/tasks?withSubTasks=true")
        val category = client.get("/tasks/15?withSubTasks=false")
        val notANumber = client.get("/tasks/nope?withSubTasks=false")

        assertEquals(HttpStatusCode.OK, all.status)
        assertEquals(HttpStatusCode.OK, disabled.status)
        assertTrue(all.bodyAsText().contains("\"name\":\"kept\""))
        assertTrue(disabled.bodyAsText().contains("\"code\":200"))
        assertEquals(
            listOf(
                null to true,
                null to false,
                null to false,
                null to true,
                15L to false,
                null to false,
            ),
            seen,
        )
        assertEquals(HttpStatusCode.OK, upper.status)
        assertEquals(HttpStatusCode.OK, enabled.status)
        assertEquals(HttpStatusCode.OK, category.status)
        assertEquals(HttpStatusCode.OK, notANumber.status)
    }
}
