package org.hogwarts.android.core.push

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPolicyTest {
    private val now = 1_800_000_000_000L
    private val day = 24L * 60 * 60 * 1000

    @Test fun `first time on Android 13 shows the prompt`() {
        assertTrue(NotificationPolicy.shouldShowPrompt(33, permissionGranted = false, lastDeclinedAtMs = null, nowMs = now))
    }

    @Test fun `declined two days ago stays quiet`() {
        assertFalse(NotificationPolicy.shouldShowPrompt(34, false, now - 2 * day, now))
    }

    @Test fun `declined just under a week ago stays quiet`() {
        assertFalse(NotificationPolicy.shouldShowPrompt(34, false, now - 7 * day + 1, now))
    }

    @Test fun `declined a week ago asks again`() {
        assertTrue(NotificationPolicy.shouldShowPrompt(34, false, now - 7 * day, now))
    }

    @Test fun `granted never prompts`() {
        assertFalse(NotificationPolicy.shouldShowPrompt(35, permissionGranted = true, lastDeclinedAtMs = null, nowMs = now))
    }

    @Test fun `before Android 13 there is no runtime prompt`() {
        assertFalse(NotificationPolicy.shouldShowPrompt(32, false, null, now))
        assertFalse(NotificationPolicy.shouldShowPrompt(26, false, now - 30 * day, now))
    }

    @Test fun `status reads app switch and the live channel`() {
        assertEquals(NotificationStatus.AppOff, NotificationPolicy.status(appEnabled = false, liveChannelImportance = 4))
        assertEquals(NotificationStatus.AppOff, NotificationPolicy.status(false, null))
        assertEquals(NotificationStatus.LiveChannelOff, NotificationPolicy.status(true, 0))
        assertEquals(NotificationStatus.On, NotificationPolicy.status(true, 4))
        assertEquals(NotificationStatus.On, NotificationPolicy.status(true, null))
    }

    @Test fun `registers at start when the version changed or never registered`() {
        assertTrue(NotificationPolicy.shouldRegisterOnStart(true, registeredVersionCode = null, currentVersionCode = 14))
        assertTrue(NotificationPolicy.shouldRegisterOnStart(true, 13, 14))
    }

    @Test fun `does not register at start when this version already did, or signed out`() {
        assertFalse(NotificationPolicy.shouldRegisterOnStart(true, 14, 14))
        assertFalse(NotificationPolicy.shouldRegisterOnStart(false, null, 14))
    }

    @Test fun `type to channel mapping`() {
        fun ch(t: String) = NotificationHandler.channelForType(t)
        assertEquals("hogwarts_live", NotificationHandler.CHANNEL_LIVE_CLASS)
        listOf("live_class_starting_soon", "live_class_makeup_booked", "live_class_no_teacher")
            .forEach { assertEquals("hogwarts_live", ch(it)) }
        assertEquals("hogwarts_attendance", ch("attendance_marked"))
        assertEquals("hogwarts_attendance", ch("absence_alert"))
        assertEquals("hogwarts_grades", ch("grade_posted"))
        assertEquals("hogwarts_grades", ch("assignment_graded"))
        assertEquals("hogwarts_fees", ch("fee_due"))
        assertEquals("hogwarts_messages", ch("message_received"))
        assertEquals("hogwarts_announcements", ch("announcement"))
        assertEquals("hogwarts_general", ch("class_cancelled"))
        assertEquals("hogwarts_general", ch("whatever"))
    }
}

class PushDeepLinkTest {
    private fun link(value: String?) = PushDeepLink.fromExtras { key -> if (key == "deep_link") value else null }

    @Test fun `reads the three live shapes`() {
        assertEquals("/live/join/tt1", link("/live/join/tt1"))
        assertEquals("/live/s1", link("/live/s1"))
        assertEquals("/live/s1/cover", link("/live/s1/cover"))
    }

    @Test fun `strips a locale prefix and keeps the query`() {
        assertEquals("/live/s1", link("/ar/live/s1"))
        assertEquals("/exams/e1?x=1", link("/en/exams/e1?x=1"))
    }

    @Test fun `ignores empty missing and foreign targets`() {
        assertNull(link(null))
        assertNull(link(""))
        assertNull(link("   "))
        assertNull(link("/"))
        assertNull(link("/ar"))
        assertNull(link("https://evil.example/live/x"))
        assertNull(link("//evil.example/live/x"))
    }
}
