package net.lifeupapp.lifeup.api.content

import android.database.MatrixCursor
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.lifeupapp.lifeup.api.utils.getBooleanOrNull
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentProviderApiCompatibilityTest {

    @Test
    fun forEachRow_emptyCursor_shouldNotInvokeCallback() {
        val cursor = MatrixCursor(arrayOf("_ID"))
        var count = 0

        cursor.forEachRow {
            count += 1
        }

        assertEquals(0, count)
    }

    @Test
    fun getBooleanOrNull_shouldSupportBooleanAndNumericRepresentations() {
        val cursor = MatrixCursor(
            arrayOf("boolTrue", "boolFalse", "intTrue", "intFalse", "stringTrue", "stringFalse")
        ).apply {
            addRow(arrayOf(true, false, 1, 0, "true", "false"))
            moveToFirst()
        }

        assertEquals(true, cursor.getBooleanOrNull("boolTrue"))
        assertEquals(false, cursor.getBooleanOrNull("boolFalse"))
        assertEquals(true, cursor.getBooleanOrNull("intTrue"))
        assertEquals(false, cursor.getBooleanOrNull("intFalse"))
        assertEquals(true, cursor.getBooleanOrNull("stringTrue"))
        assertEquals(false, cursor.getBooleanOrNull("stringFalse"))
    }
}
