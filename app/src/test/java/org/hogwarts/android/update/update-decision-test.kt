package org.hogwarts.android.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateDecisionTest {
    private fun d(installed: Int, latest: Int, min: Int, inRoom: Boolean = false) =
        decideUpdate(installed, latest, min, inRoom)

    @Test fun `up to date is none`() = assertEquals(UpdateDecision.None, d(4, 4, 4))
    @Test fun `below min is required`() = assertEquals(UpdateDecision.Required, d(3, 4, 4))
    @Test fun `below latest but above min is optional and silent`() = assertEquals(UpdateDecision.Optional, d(4, 5, 4))
    @Test fun `optional is never throttled away`() = assertEquals(UpdateDecision.Optional, d(4, 5, 4))
    @Test fun `never inside a live room`() {
        assertEquals(UpdateDecision.None, d(3, 4, 4, inRoom = true))
        assertEquals(UpdateDecision.None, d(4, 5, 4, inRoom = true))
    }
    @Test fun `newer than latest is none`() = assertEquals(UpdateDecision.None, d(9, 5, 4))

    private fun a(decision: UpdateDecision, inRoom: Boolean = false, downloaded: Boolean = false, busy: Boolean = false) =
        nextUpdateAction(decision, inRoom, downloaded, busy)

    @Test fun `optional downloads quietly in the background`() = assertEquals(UpdateAction.Download, a(UpdateDecision.Optional))
    @Test fun `required downloads at once`() = assertEquals(UpdateAction.Download, a(UpdateDecision.Required))
    @Test fun `optional installs as soon as downloaded`() = assertEquals(UpdateAction.Install, a(UpdateDecision.Optional, downloaded = true))
    @Test fun `required installs immediately after download`() = assertEquals(UpdateAction.Install, a(UpdateDecision.Required, downloaded = true))
    @Test fun `in a room nothing happens`() {
        assertEquals(UpdateAction.Nothing, a(UpdateDecision.Required, inRoom = true))
        assertEquals(UpdateAction.Nothing, a(UpdateDecision.Optional, inRoom = true, downloaded = true))
    }
    @Test fun `nothing to do when none or already busy`() {
        assertEquals(UpdateAction.Nothing, a(UpdateDecision.None, downloaded = true))
        assertEquals(UpdateAction.Nothing, a(UpdateDecision.Optional, busy = true))
    }

    @Test fun `confirm screen only in the foreground outside a room`() {
        assertEquals(ConfirmRoute.StartActivity, routeConfirm(appInForeground = true, inRoom = false))
        assertEquals(ConfirmRoute.Notify, routeConfirm(appInForeground = true, inRoom = true))
        assertEquals(ConfirmRoute.Notify, routeConfirm(appInForeground = false, inRoom = false))
    }

    @Test fun `manifest tolerates unknown and missing fields`() {
        val m = UpdateManifest.parse("""{"latestCode":4,"minCode":3,"extra":1}""")
        assertNotNull(m); assertEquals(4, m!!.latestCode); assertNull(m.apkUrl)
    }
    @Test fun `bad manifest is null`() = assertNull(UpdateManifest.parse("<html>"))
}
