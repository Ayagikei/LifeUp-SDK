package net.lifeupapp.lifeup.http.service

import net.lifeupapp.lifeup.http.utils.Settings
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorServicePortSelectionTest {

    @Test
    fun resolveStartPort_usesRetryOverrideBeforeDefaultPort() {
        assertEquals(
            13277,
            resolveStartPort(customPort = 0, retryPortOverride = 13277)
        )
    }

    @Test
    fun resolveStartPort_usesCustomPortWhenNoRetryOverride() {
        assertEquals(
            24567,
            resolveStartPort(customPort = 24567, retryPortOverride = null)
        )
    }

    @Test
    fun nextAutoRetryPort_wrapsToMinPortAfterMaxPort() {
        assertEquals(
            Settings.MIN_PORT,
            nextAutoRetryPort(Settings.MAX_PORT)
        )
    }

    @Test
    fun buildServerBaseUrl_formatsHostAndPort() {
        assertEquals(
            "http://10.0.2.15:13276",
            buildServerBaseUrl(host = "10.0.2.15", port = 13276)
        )
    }
}
