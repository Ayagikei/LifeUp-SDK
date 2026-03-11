package net.lifeupapp.lifeup.api.content.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun buildCountProgress_shouldReturnNullWhenColumnsAreIncomplete() {
        assertNull(buildCountProgress(currentCount = null, targetCount = 10))
        assertNull(buildCountProgress(currentCount = 3, targetCount = null))
    }

    @Test
    fun buildCountProgress_shouldCreateObjectWhenColumnsAreComplete() {
        val progress = buildCountProgress(currentCount = 3, targetCount = 10)

        assertEquals(3, progress?.currentCount)
        assertEquals(10, progress?.targetCount)
    }

    @Test
    fun buildRepeatEndCondition_shouldReturnNullWhenModeOrBehaviorIsMissing() {
        assertNull(
            buildRepeatEndCondition(
                mode = null,
                behavior = "TERMINATE",
                targetCycleCount = 7,
                endDateMillis = null,
                inclusive = false
            )
        )
        assertNull(
            buildRepeatEndCondition(
                mode = "COUNT",
                behavior = null,
                targetCycleCount = 7,
                endDateMillis = null,
                inclusive = false
            )
        )
    }

    @Test
    fun buildRepeatEndCondition_shouldReturnNullWhenModePayloadIsMissing() {
        assertNull(
            buildRepeatEndCondition(
                mode = "COUNT",
                behavior = "TERMINATE",
                targetCycleCount = null,
                endDateMillis = null,
                inclusive = false
            )
        )
        assertNull(
            buildRepeatEndCondition(
                mode = "DATE",
                behavior = "FREEZE",
                targetCycleCount = null,
                endDateMillis = null,
                inclusive = true
            )
        )
    }

    @Test
    fun buildRepeatEndCondition_shouldCreateObjectWhenRequiredColumnsExist() {
        val countCondition = buildRepeatEndCondition(
            mode = "COUNT",
            behavior = "TERMINATE",
            targetCycleCount = 7,
            endDateMillis = null,
            inclusive = false
        )
        val dateCondition = buildRepeatEndCondition(
            mode = "DATE",
            behavior = "FREEZE",
            targetCycleCount = null,
            endDateMillis = 1741564800000,
            inclusive = true
        )

        assertEquals("COUNT", countCondition?.mode)
        assertEquals("TERMINATE", countCondition?.behavior)
        assertEquals(7, countCondition?.targetCycleCount)
        assertNull(countCondition?.endDateMillis)
        assertFalse(countCondition?.inclusive ?: true)

        assertEquals("DATE", dateCondition?.mode)
        assertEquals("FREEZE", dateCondition?.behavior)
        assertNull(dateCondition?.targetCycleCount)
        assertEquals(1741564800000, dateCondition?.endDateMillis)
        assertTrue(dateCondition?.inclusive == true)
    }

    @Test
    fun parseSkillIds_shouldReturnEmptyListWhenBlankOrInvalid() {
        assertEquals(emptyList<Long>(), parseSkillIds(null))
        assertEquals(emptyList<Long>(), parseSkillIds(""))
        assertEquals(emptyList<Long>(), parseSkillIds("not-json"))
    }

    @Test
    fun parseSkillIds_shouldParseValidJsonArray() {
        assertEquals(listOf(1L, 2L, 3L), parseSkillIds("[1,2,3]"))
    }

    @Test
    fun taskBuilder_shouldExposeCountProgressAndRepeatEndCondition() {
        val task = Task.builder {
            setName("task")
            setCountProgress(TaskCountProgress(currentCount = 3, targetCount = 10))
            setRepeatEndCondition(
                TaskRepeatEndCondition(
                    mode = "COUNT",
                    behavior = "TERMINATE",
                    targetCycleCount = 7,
                    endDateMillis = null,
                    inclusive = true
                )
            )
        }

        assertEquals(3, task.countProgress?.currentCount)
        assertEquals(10, task.countProgress?.targetCount)
        assertEquals("COUNT", task.repeatEndCondition?.mode)
        assertEquals("TERMINATE", task.repeatEndCondition?.behavior)
        assertEquals(7, task.repeatEndCondition?.targetCycleCount)
        assertTrue(task.repeatEndCondition?.inclusive == true)
    }
}
