package org.hogwarts.android.feature.live.ui.room

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionStateTest {
    @Test
    fun `granted always wins`() {
        for (asked in listOf(true, false)) for (rationale in listOf(true, false)) {
            assertEquals(PermissionState.GRANTED, permissionState(true, asked, rationale))
        }
    }

    @Test
    fun `never asked is asked`() {
        assertEquals(PermissionState.ASK, permissionState(false, askedBefore = false, shouldShowRationale = false))
    }

    @Test
    fun `a denial the system will re-ask explains first`() {
        assertEquals(PermissionState.RATIONALE, permissionState(false, askedBefore = true, shouldShowRationale = true))
        assertEquals(PermissionState.RATIONALE, permissionState(false, askedBefore = false, shouldShowRationale = true))
    }

    @Test
    fun `asked with no rationale left is blocked`() {
        assertEquals(PermissionState.BLOCKED, permissionState(false, askedBefore = true, shouldShowRationale = false))
    }
}
