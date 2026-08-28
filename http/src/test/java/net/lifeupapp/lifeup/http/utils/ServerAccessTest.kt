package net.lifeupapp.lifeup.http.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerAccessTest {

    @Test
    fun parse_startAndStopHosts() {
        assertEquals(CloudControl.Action.START, CloudControl.parse("lifeupcloud", "start"))
        assertEquals(CloudControl.Action.STOP, CloudControl.parse("lifeupcloud", "stop"))
        assertEquals(CloudControl.Action.START, CloudControl.parse("LifeUpCloud", "START"))
    }

    @Test
    fun parse_rejectsUnknown() {
        assertNull(CloudControl.parse("lifeup", "start"))
        assertNull(CloudControl.parse("lifeupcloud", "toggle"))
        assertNull(CloudControl.parse(null, "start"))
    }
}
