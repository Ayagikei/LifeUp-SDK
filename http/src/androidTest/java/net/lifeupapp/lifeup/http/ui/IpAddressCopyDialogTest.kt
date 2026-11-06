package net.lifeupapp.lifeup.http.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.button.MaterialButtonToggleGroup
import net.lifeupapp.lifeup.http.MainActivity
import net.lifeupapp.lifeup.http.R
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IpAddressCopyDialogTest {
    @Test
    fun singleAddressWaitsForSelectionAndCopiesOnlyIp() = withDialog(listOf("192.168.1.8")) { activity, dialog ->
        assertEquals(R.id.format_ip, dialog.findViewById<MaterialButtonToggleGroup>(R.id.copy_format)!!.checkedButtonId)
        assertEquals("unchanged", clipboardText(activity))
        onView(withId(R.id.copy_format)).check(matches(isDisplayed()))

        selectValue("192.168.1.8")

        assertEquals("192.168.1.8", clipboardText(activity))
        onView(withId(R.id.copy_format)).check(doesNotExist())
    }

    @Test
    fun multipleAddressesCopyTheChosenIp() = withDialog(listOf("192.168.1.8", "10.0.0.8")) { activity, _ ->
        selectValue("10.0.0.8")
        assertEquals("10.0.0.8", clipboardText(activity))
    }

    @Test
    fun portFormatHasOneRowAndUsesTheSuppliedServerPort() =
        withDialog(listOf("192.168.1.8", "10.0.0.8")) { activity, dialog ->
            onView(withId(R.id.format_port)).perform(click())
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync {
                assertEquals(1, dialog.findViewById<ListView>(R.id.ip_addresses)!!.adapter.count)
            }
            selectValue("24680")
            assertEquals("24680", clipboardText(activity))
        }

    @Test
    fun combinedFormatUsesTheChosenIpAndServerPort() =
        withDialog(listOf("192.168.1.8", "10.0.0.8")) { activity, _ ->
            onView(withId(R.id.format_ip_port)).perform(click())
            selectValue("10.0.0.8:24680")
            assertEquals("10.0.0.8:24680", clipboardText(activity))
        }

    @Test
    fun combinedIpv6FormatBracketsTheHost() = withDialog(listOf("fec0::8")) { activity, _ ->
        onView(withId(R.id.format_ip_port)).perform(click())
        selectValue("[fec0::8]:24680")
        assertEquals("[fec0::8]:24680", clipboardText(activity))
    }

    @Test
    fun switchingBackToIpRestoresAllAddressesAndReopeningDefaultsToIp() =
        withDialog(listOf("192.168.1.8", "10.0.0.8")) { activity, _ ->
            onView(withId(R.id.format_port)).perform(click())
            onView(withId(R.id.format_ip)).perform(click())
            selectValue("10.0.0.8")
            assertEquals("10.0.0.8", clipboardText(activity))

            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync {
                activity.showIpAddressCopyDialog(listOf("192.168.1.8"), 24680)
            }
            selectValue("192.168.1.8")
            assertEquals("192.168.1.8", clipboardText(activity))
        }

    @Test
    fun cancelDoesNotChangeTheClipboard() = withDialog(listOf("192.168.1.8")) { activity, _ ->
        onView(withId(android.R.id.button2)).perform(click())
        assertEquals("unchanged", clipboardText(activity))
        onView(withId(R.id.copy_format)).check(doesNotExist())
    }

    private fun withDialog(addresses: List<String>, test: (MainActivity, AlertDialog) -> Unit) {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var activity: MainActivity
            lateinit var dialog: AlertDialog
            scenario.onActivity {
                activity = it
                val clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("test", "unchanged"))
                dialog = it.showIpAddressCopyDialog(addresses, 24680)
                assertTrue(dialog.isShowing)
            }
            try {
                test(activity, dialog)
            } finally {
                scenario.onActivity {
                    dialog.dismiss()
                    val clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("test", ""))
                }
            }
        }
    }

    private fun selectValue(value: String) {
        onData(equalTo(value)).inAdapterView(withId(R.id.ip_addresses)).perform(click())
    }

    private fun clipboardText(context: Context): String {
        var text = ""
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            text = clipboard.primaryClip!!.getItemAt(0).text.toString()
        }
        return text
    }
}
