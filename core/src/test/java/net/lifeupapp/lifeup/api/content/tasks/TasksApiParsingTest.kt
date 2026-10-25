package net.lifeupapp.lifeup.api.content.tasks

import android.database.Cursor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

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
    fun parseProviderSubTasks_shouldExposeEndTimeWhenPresent() {
        val subTasks = parseSubTasksJson(
            """[{"id":1,"gid":2,"todo":"child","status":1,"endTime":1741425600123,"exp":5,"items":[],"order":1}]"""
        )

        assertEquals(1, subTasks.size)
        assertEquals(1741425600123L, subTasks.first().endTime)
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

    @Test
    fun taskBuilder_shouldExposeWeekdays() {
        val task = Task.builder {
            setName("task")
            setFrequency(1)
            setWeekdays("1,3,5")
        }

        assertEquals(1, task.frequency)
        assertEquals("1,3,5", task.weekdays)
    }

    @Test
    fun taskBuilder_shouldDefaultImportanceAndDifficultyToNull() {
        val task = Task.builder {
            setName("task")
        }

        assertNull(task.importance)
        assertNull(task.difficulty)
    }

    @Test
    fun taskBuilder_shouldExposeDistinctImportanceAndDifficulty() {
        val task = Task.builder {
            setName("task")
            setImportance(1)
            setDifficulty(4)
        }

        assertEquals(Integer.valueOf(1), task.importance)
        assertEquals(Integer.valueOf(4), task.difficulty)
    }

    @Test
    fun taskSerialization_shouldRoundTripDegreesOnThePublicHttpShape() {
        // Ktor DefaultJson encodes defaults and explicit nulls, which is the Cloud HTTP body.
        val publicHttpJson = Json {
            encodeDefaults = true
            isLenient = true
            allowSpecialFloatingPointValues = true
            allowStructuredMapKeys = true
        }
        val task = Task.builder {
            setName("task")
            setWeekdays("1,3,5")
            setCountProgress(TaskCountProgress(currentCount = 3, targetCount = 10))
            setImportance(4)
            setDifficulty(1)
        }

        val encoded = publicHttpJson.encodeToString(task)
        val weekdaysAt = encoded.indexOf("\"weekdays\"")
        val importanceAt = encoded.indexOf("\"importance\"")
        val difficultyAt = encoded.indexOf("\"difficulty\"")

        assertTrue(weekdaysAt >= 0 && importanceAt > weekdaysAt && difficultyAt > importanceAt)
        assertTrue(encoded.contains("\"importance\":4"))
        assertTrue(encoded.contains("\"difficulty\":1"))
        assertEquals(task, publicHttpJson.decodeFromString<Task>(encoded))

        val unset = publicHttpJson.encodeToString(Task.builder { setName("task") })
        assertTrue(unset.contains("\"importance\":null"))
        assertTrue(unset.contains("\"difficulty\":null"))

        val legacyJson = Json { encodeDefaults = false }
        val legacy = legacyJson.encodeToString(
            Task.builder {
                setName("task")
                setWeekdays("1,3,5")
                setCountProgress(TaskCountProgress(currentCount = 3, targetCount = 10))
            }
        )
        assertFalse(legacy.contains("importance"))
        assertFalse(legacy.contains("difficulty"))

        val decodedLegacy = publicHttpJson.decodeFromString<Task>(legacy)
        assertNull(decodedLegacy.importance)
        assertNull(decodedLegacy.difficulty)
        assertEquals("1,3,5", decodedLegacy.weekdays)
        assertEquals(3, decodedLegacy.countProgress?.currentCount)
        assertEquals(10, decodedLegacy.countProgress?.targetCount)
        assertNull(decodedLegacy.repeatEndCondition)
    }

    @Test
    fun cursorToTask_shouldParseDistinctImportanceAndDifficultyBounds() {
        val lowHigh = taskCursor(
            mapOf(
                "name" to "bounds-low-high",
                "endTime" to 99L,
                "importance" to 1,
                "difficulty" to 4,
                "countProgressCurrent" to 3,
                "countProgressTarget" to 10,
                "repeatEndMode" to "DATE",
                "repeatEndBehavior" to "FREEZE",
                "repeatEndDateMillis" to 1741564800000L,
                "repeatEndInclusive" to true
            )
        ).toTask(includeEndTime = false, includeRepeatEndCondition = true)

        assertEquals(Integer.valueOf(1), lowHigh.importance)
        assertEquals(Integer.valueOf(4), lowHigh.difficulty)
        assertEquals(0L, lowHigh.endTime)
        assertEquals(3, lowHigh.countProgress?.currentCount)
        assertEquals(10, lowHigh.countProgress?.targetCount)
        assertEquals("DATE", lowHigh.repeatEndCondition?.mode)
        assertEquals("FREEZE", lowHigh.repeatEndCondition?.behavior)
        assertEquals(1741564800000L, lowHigh.repeatEndCondition?.endDateMillis)
        assertTrue(lowHigh.repeatEndCondition?.inclusive == true)

        val highLow = taskCursor(
            mapOf(
                "name" to "bounds-high-low",
                "importance" to 4,
                "difficulty" to 1
            )
        ).toTask(includeEndTime = false, includeRepeatEndCondition = true)

        assertEquals(Integer.valueOf(4), highLow.importance)
        assertEquals(Integer.valueOf(1), highLow.difficulty)
        assertNull(highLow.countProgress)
        assertNull(highLow.repeatEndCondition)
    }

    @Test
    fun cursorToTask_shouldTreatNullDegreeColumnsAsNull() {
        val task = taskCursor(
            mapOf(
                "name" to "null-degrees",
                "importance" to null,
                "difficulty" to null
            )
        ).toTask(includeEndTime = false, includeRepeatEndCondition = true)

        assertEquals("null-degrees", task.name)
        assertNull(task.importance)
        assertNull(task.difficulty)

        val partial = taskCursor(
            mapOf(
                "name" to "partial-null",
                "importance" to null,
                "difficulty" to 2
            )
        ).toTask(includeEndTime = false, includeRepeatEndCondition = true)

        assertNull(partial.importance)
        assertEquals(Integer.valueOf(2), partial.difficulty)
    }

    @Test
    fun cursorToTask_shouldTreatMissingDegreeColumnsAsNull() {
        val task = taskCursor(
            mapOf(
                "name" to "missing-degrees",
                "countProgressCurrent" to 1,
                "countProgressTarget" to 2
            )
        ).toTask(includeEndTime = false, includeRepeatEndCondition = true)

        assertEquals(1, task.countProgress?.currentCount)
        assertEquals(2, task.countProgress?.targetCount)
        assertNull(task.importance)
        assertNull(task.difficulty)
    }

    @Test
    fun cursorToTask_shouldKeepHistorySemanticsWhenDegreeColumnsAreAbsent() {
        val task = taskCursor(
            mapOf(
                "name" to "history",
                "endTime" to 1741425600123L,
                "countProgressCurrent" to 2,
                "countProgressTarget" to 8,
                "repeatEndMode" to "COUNT",
                "repeatEndBehavior" to "TERMINATE",
                "repeatEndTargetCycleCount" to 7,
                "repeatEndInclusive" to false
            )
        ).toTask(includeEndTime = true, includeRepeatEndCondition = false)

        assertEquals(1741425600123L, task.endTime)
        assertEquals(2, task.countProgress?.currentCount)
        assertEquals(8, task.countProgress?.targetCount)
        assertNull(task.repeatEndCondition)
        assertNull(task.importance)
        assertNull(task.difficulty)
    }

    private fun taskCursor(values: Map<String, Any?>): Cursor {
        val names = values.keys.toList()
        return Proxy.newProxyInstance(
            Cursor::class.java.classLoader,
            arrayOf(Cursor::class.java)
        ) { _, method, args ->
            val index = args?.getOrNull(0) as? Int
            when (method.name) {
                "getColumnIndex" -> names.indexOf(args?.get(0) as String)
                "isNull" -> values[names[index ?: error("missing cursor index")]] == null
                "getInt" -> (values[names[index ?: error("missing cursor index")]] as Number).toInt()
                "getLong" -> (values[names[index ?: error("missing cursor index")]] as Number).toLong()
                "getString" -> values[names[index ?: error("missing cursor index")]]?.toString()
                else -> defaultCursorReturn(method.returnType)
            }
        } as Cursor
    }

    private fun defaultCursorReturn(returnType: Class<*>): Any? = when (returnType) {
        java.lang.Boolean.TYPE -> false
        java.lang.Integer.TYPE -> 0
        java.lang.Long.TYPE -> 0L
        java.lang.Float.TYPE -> 0f
        java.lang.Double.TYPE -> 0.0
        java.lang.Short.TYPE -> 0.toShort()
        Void.TYPE -> null
        else -> null
    }
}
