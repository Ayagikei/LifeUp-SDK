package net.lifeupapp.lifeup.api.content.tasks

import org.junit.Assert.assertEquals
import org.junit.Test

class TasksApiParsingTest {

    @Test
    fun parseProviderSubTasks_shouldKeepRowsWhenProviderFieldsAreNullable() {
        val subTasks = parseSubTasksJson(
            """[{"id":null,"gid":null,"todo":"child","status":null,"exp":5,"items":[],"order":1,"autoUseItem":null}]"""
        )

        assertEquals(1, subTasks.size)
        assertEquals(0L, subTasks.first().id)
        assertEquals(0L, subTasks.first().gid)
        assertEquals("child", subTasks.first().todo)
        assertEquals(0, subTasks.first().status)
        assertEquals(5, subTasks.first().exp)
    }

    @Test
    fun taskBuilder_shouldExposeLegacyItemAmount() {
        val task = Task.builder {
            setName("task")
            setItemId(7L)
            setItemAmount(3)
        }

        assertEquals(3, task.itemAmount)
    }
}
