package net.lifeupapp.lifeup.api.content.tasks

import android.database.Cursor
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.exception.CloudException
import net.lifeupapp.lifeup.api.exception.ContentProviderException
import net.lifeupapp.lifeup.api.exception.NullCursorException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class SmartListTasksTest {

    @Test
    fun smartListType_isStrictLowercaseDailyWeeklyMonthly() {
        assertEquals(
            listOf("daily", "weekly", "monthly"),
            SmartListType.entries.map { it.pathSegment },
        )
        assertEquals(SmartListType.DAILY, SmartListType.fromPathSegment("daily"))
        assertEquals(SmartListType.WEEKLY, SmartListType.fromPathSegment("weekly"))
        assertEquals(SmartListType.MONTHLY, SmartListType.fromPathSegment("monthly"))

        listOf(null, "", "Daily", "DAILY", "Weekly", "MONTHLY", "yearly", "all", "tasks", "daily ").forEach { raw ->
            assertNull("rejected $raw", SmartListType.fromPathSegment(raw))
        }
    }

    @Test
    fun smartListUri_usesProviderPathWithoutQueryDefaults() {
        assertEquals(
            "content://net.sarasarasa.lifeup.provider.api/tasks",
            ContentProviderUrl.TASK,
        )
        assertEquals(
            "content://net.sarasarasa.lifeup.provider.api/smart_lists",
            ContentProviderUrl.SMART_LISTS,
        )
        assertEquals(
            "content://net.sarasarasa.lifeup.provider.api/smart_lists/daily/tasks",
            smartListTasksContentUri(SmartListType.DAILY),
        )
        assertEquals(
            "content://net.sarasarasa.lifeup.provider.api/smart_lists/weekly/tasks",
            smartListTasksContentUri(SmartListType.WEEKLY),
        )
        assertEquals(
            "content://net.sarasarasa.lifeup.provider.api/smart_lists/monthly/tasks",
            smartListTasksContentUri(SmartListType.MONTHLY),
        )
        SmartListType.entries.forEach { type ->
            val uri = smartListTasksContentUri(type)
            assertFalse(uri.contains("?"))
            assertFalse(uri.contains("withSubTasks"))
            assertFalse(uri.contains(type.name))
            assertEquals(uri, smartListTasksContentUri(type, withSubTasks = true))
            assertEquals("$uri?withSubTasks=false", smartListTasksContentUri(type, withSubTasks = false))
        }
    }

    @Test
    fun tasksContentUri_appendsWithSubTasksOnlyWhenFalse() {
        val all = ContentProviderUrl.TASK
        assertEquals(all, tasksContentUri(categoryId = null, withSubTasks = true))
        assertEquals("$all?withSubTasks=false", tasksContentUri(categoryId = null, withSubTasks = false))
        assertEquals("$all/12", tasksContentUri(categoryId = 12L, withSubTasks = true))
        assertEquals(
            "$all/12?withSubTasks=false",
            tasksContentUri(categoryId = 12L, withSubTasks = false),
        )
        assertFalse(tasksContentUri(7L, true).contains("withSubTasks=true"))
    }

    @Test
    fun listSmartListTasks_keepsEnumSignatureBesideCategoryList() {
        val methods = TasksApi::class.java.declaredMethods
        val smartList = methods.filter { it.name == "listSmartListTasks" || it.name.startsWith("listSmartListTasks-") }
        val categoryList = methods.filter { it.name == "listTasks" || it.name.startsWith("listTasks-") }

        assertEquals(
            setOf(listOf(SmartListType::class.java), listOf(SmartListType::class.java, java.lang.Boolean.TYPE)),
            smartList.map { it.parameterTypes.toList() }.toSet(),
        )
        assertEquals(
            setOf(listOf(java.lang.Long::class.java), listOf(java.lang.Long::class.java, java.lang.Boolean.TYPE)),
            categoryList.map { it.parameterTypes.toList() }.toSet(),
        )
    }
    @Test
    fun querySmartListTasks_parsesCurrentCycleTaskFields() {
        var calls = 0
        val result = querySmartListTasks(SmartListType.DAILY) { uri, onEach ->
            calls += 1
            assertEquals(smartListTasksContentUri(SmartListType.DAILY), uri)
            onEach(
                taskCursor(
                    mapOf(
                        "_ID" to 11L,
                        "_GID" to 22L,
                        "name" to "morning",
                        "notes" to "full",
                        "status" to 1,
                        "frequency" to 2,
                        "weekdays" to "1,3,5",
                        "skillIds" to "[4,9]",
                        "categoryId" to 7L,
                        "endTime" to 1741425600123L,
                        "items" to "[]",
                        "subTasks" to """[{"id":1,"gid":2,"todo":"child","status":0,"exp":5,"items":[],"order":1}]""",
                        "countProgressCurrent" to 3,
                        "countProgressTarget" to 10,
                        "repeatEndMode" to "DATE",
                        "repeatEndBehavior" to "FREEZE",
                        "repeatEndDateMillis" to 1741564800000L,
                        "repeatEndInclusive" to true,
                        "importance" to 4,
                        "difficulty" to 1,
                    ),
                ),
            )
            onEach(taskCursor(mapOf("name" to "second")))
        }
        val tasks = result.getOrThrow()
        assertEquals(1, calls)
        assertEquals(2, tasks.size)

        val first = tasks[0]
        assertEquals(11L, first.id)
        assertEquals(22L, first.gid)
        assertEquals("morning", first.name)
        assertEquals("full", first.notes)
        assertEquals(1, first.status)
        assertEquals(2, first.frequency)
        assertEquals("1,3,5", first.weekdays)
        assertEquals(listOf(4L, 9L), first.skillIds)
        assertEquals(7L, first.categoryId)
        assertEquals(0L, first.endTime)
        assertEquals(emptyList<Any>(), first.items)
        assertEquals(1, first.subTasks.size)
        assertEquals("child", first.subTasks.first().todo)
        assertEquals(3, first.countProgress?.currentCount)
        assertEquals(10, first.countProgress?.targetCount)
        assertEquals("DATE", first.repeatEndCondition?.mode)
        assertEquals("FREEZE", first.repeatEndCondition?.behavior)
        assertEquals(1741564800000L, first.repeatEndCondition?.endDateMillis)
        assertTrue(first.repeatEndCondition?.inclusive == true)
        assertEquals(Integer.valueOf(4), first.importance)
        assertEquals(Integer.valueOf(1), first.difficulty)

        assertEquals("second", tasks[1].name)
        assertEquals(emptyList<Any>(), tasks[1].subTasks)
        assertNull(tasks[1].countProgress)
        assertNull(tasks[1].repeatEndCondition)
    }

    @Test
    fun querySmartListTasks_preservesProviderErrorsWithoutFallback() {
        val missing = NullCursorException()
        var missingCalls = 0
        val missingResult = querySmartListTasks(SmartListType.WEEKLY) { uri, _ ->
            missingCalls += 1
            assertEquals(smartListTasksContentUri(SmartListType.WEEKLY), uri)
            throw missing
        }
        assertEquals(1, missingCalls)
        assertTrue(missingResult.isFailure)
        assertSame(missing, missingResult.exceptionOrNull())
        assertEquals(
            CloudException.ERROR_NULL_CURSOR,
            (missingResult.exceptionOrNull() as CloudException).errorCode,
        )

        val rejected = ContentProviderException("unknown smart list uri")
        var rejectedCalls = 0
        val rejectedResult = querySmartListTasks(SmartListType.MONTHLY) { _, _ ->
            rejectedCalls += 1
            throw rejected
        }
        assertEquals(1, rejectedCalls)
        assertTrue(rejectedResult.isFailure)
        assertSame(rejected, rejectedResult.exceptionOrNull())
        assertEquals(
            CloudException.ERROR_CONTENT_PROVIDER,
            (rejectedResult.exceptionOrNull() as CloudException).errorCode,
        )
        assertEquals(emptyList<Task>(), querySmartListTasks(SmartListType.DAILY) { _, _ -> }.getOrThrow())
    }

    private fun taskCursor(values: Map<String, Any?>): Cursor {
        val names = values.keys.toList()
        return Proxy.newProxyInstance(
            Cursor::class.java.classLoader,
            arrayOf(Cursor::class.java),
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
