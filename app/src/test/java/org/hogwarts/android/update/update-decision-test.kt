package org.hogwarts.android.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateDecisionTest {
    private val now = 10L * OPTIONAL_PROMPT_INTERVAL_MS

    private fun d(installed: Int, latest: Int, min: Int, last: Long = 0L, inRoom: Boolean = false) =
        decideUpdate(installed, latest, min, last, now, inRoom)

    @Test fun `up to date is none`() = assertEquals(UpdateDecision.None, d(4, 4, 4))
    @Test fun `below min is required`() = assertEquals(UpdateDecision.Required, d(3, 4, 4))
    @Test fun `below min is required even when shown before`() = assertEquals(UpdateDecision.Required, d(3, 4, 4, last = now - 1))
    @Test fun `below latest but above min is optional`() = assertEquals(UpdateDecision.Optional, d(4, 5, 4))
    @Test fun `optional is held back within 24 h`() = assertEquals(UpdateDecision.None, d(4, 5, 4, last = now - OPTIONAL_PROMPT_INTERVAL_MS + 1))
    @Test fun `optional returns after 24 h`() = assertEquals(UpdateDecision.Optional, d(4, 5, 4, last = now - OPTIONAL_PROMPT_INTERVAL_MS))
    @Test fun `never inside a live room`() {
        assertEquals(UpdateDecision.None, d(3, 4, 4, inRoom = true))
        assertEquals(UpdateDecision.None, d(4, 5, 4, inRoom = true))
    }
    @Test fun `newer than latest is none`() = assertEquals(UpdateDecision.None, d(9, 5, 4))

    @Test fun `manifest tolerates unknown and missing fields`() {
        val m = UpdateManifest.parse("""{"latestCode":4,"minCode":3,"extra":1}""")
        assertNotNull(m); assertEquals(4, m!!.latestCode); assertNull(m.apkUrl)
    }
    @Test fun `bad manifest is null`() = assertNull(UpdateManifest.parse("<html>"))
}
