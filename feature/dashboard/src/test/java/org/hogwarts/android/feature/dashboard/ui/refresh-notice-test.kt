package org.hogwarts.android.feature.dashboard.ui

import org.hogwarts.android.feature.dashboard.data.repository.RefreshFailure
import org.hogwarts.android.feature.dashboard.data.repository.classifyFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RefreshNoticeTest {
    // --- which message ---
    @Test fun `no failure shows nothing, online or not`() {
        assertEquals(RefreshNotice.None, refreshNotice(null, deviceOnline = true))
        assertEquals(RefreshNotice.None, refreshNotice(null, deviceOnline = false))
    }
    @Test fun `a connectivity failure is offline even when the system says online`() =
        assertEquals(RefreshNotice.Offline, refreshNotice(RefreshFailure.Connectivity, deviceOnline = true))
    @Test fun `any failure with no validated network is offline`() {
        assertEquals(RefreshNotice.Offline, refreshNotice(RefreshFailure.Server, deviceOnline = false))
        assertEquals(RefreshNotice.Offline, refreshNotice(RefreshFailure.Timeout, deviceOnline = false))
    }
    @Test fun `a server error or timeout on an online phone is neutral`() {
        assertEquals(RefreshNotice.CouldNotRefresh, refreshNotice(RefreshFailure.Server, deviceOnline = true))
        assertEquals(RefreshNotice.CouldNotRefresh, refreshNotice(RefreshFailure.Timeout, deviceOnline = true))
    }

    // --- which errors count as connectivity ---
    @Test fun `dns, refused and no-route are connectivity`() {
        assertEquals(RefreshFailure.Connectivity, classifyFailure(UnknownHostException("x")))
        assertEquals(RefreshFailure.Connectivity, classifyFailure(ConnectException("x")))
        assertEquals(RefreshFailure.Connectivity, classifyFailure(NoRouteToHostException("x")))
    }
    @Test fun `a timeout is not connectivity`() =
        assertEquals(RefreshFailure.Timeout, classifyFailure(SocketTimeoutException("x")))
    @Test fun `other errors are server side`() {
        assertEquals(RefreshFailure.Server, classifyFailure(IOException("unexpected end of stream")))
        assertEquals(RefreshFailure.Server, classifyFailure(ProtocolException("x")))
        assertEquals(RefreshFailure.Server, classifyFailure(IllegalStateException("bad body")))
    }

    // --- back-off + resume ---
    @Test fun `back-off is 5, 15, 45 seconds then stops`() {
        assertEquals(5_000L, retryDelayMs(0))
        assertEquals(15_000L, retryDelayMs(1))
        assertEquals(45_000L, retryDelayMs(2))
        assertNull(retryDelayMs(3))
    }
    @Test fun `resume without a failure waits 30 seconds`() {
        assertFalse(shouldRefreshOnResume(lastStartMs = 0, nowMs = 29_999, hadFailure = false))
        assertTrue(shouldRefreshOnResume(lastStartMs = 0, nowMs = 30_000, hadFailure = false))
    }
    @Test fun `resume after a failure refreshes almost at once`() {
        assertFalse(shouldRefreshOnResume(lastStartMs = 0, nowMs = 4_999, hadFailure = true))
        assertTrue(shouldRefreshOnResume(lastStartMs = 0, nowMs = 5_000, hadFailure = true))
    }
}
