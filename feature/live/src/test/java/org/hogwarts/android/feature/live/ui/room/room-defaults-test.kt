package org.hogwarts.android.feature.live.ui.room

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomDefaultsTest {
    @Test
    fun `hosts always start live, even when the room joins muted`() {
        for (role in listOf("HOST", "CO_HOST")) {
            assertTrue(RoomDefaults.micStartsOn(role, joinMuted = false))
            assertTrue(RoomDefaults.micStartsOn(role, joinMuted = true))
            assertTrue(RoomDefaults.cameraStartsOn(role, joinMuted = true))
        }
    }

    @Test
    fun `participants follow joinMuted`() {
        assertTrue(RoomDefaults.micStartsOn("PARTICIPANT", joinMuted = false))
        assertFalse(RoomDefaults.micStartsOn("PARTICIPANT", joinMuted = true))
        assertTrue(RoomDefaults.cameraStartsOn("PARTICIPANT", joinMuted = false))
        assertFalse(RoomDefaults.cameraStartsOn("PARTICIPANT", joinMuted = true))
    }

    @Test
    fun `observers never publish`() {
        assertFalse(RoomDefaults.micStartsOn("OBSERVER", joinMuted = false))
        assertFalse(RoomDefaults.cameraStartsOn("OBSERVER", joinMuted = false))
        assertFalse(RoomDefaults.micStartsOn("OBSERVER", joinMuted = true))
    }

    @Test
    fun `unknown role is a participant`() {
        assertTrue(RoomDefaults.micStartsOn(null, joinMuted = false))
        assertFalse(RoomDefaults.micStartsOn(null, joinMuted = true))
    }

    @Test
    fun `banner is for a host with the mic off in the room`() {
        assertTrue(RoomDefaults.showMicBanner(isHost = true, micOn = false, inRoom = true, entrySettled = true))
        assertFalse(RoomDefaults.showMicBanner(isHost = true, micOn = true, inRoom = true, entrySettled = true))
        assertFalse(RoomDefaults.showMicBanner(isHost = false, micOn = false, inRoom = true, entrySettled = true))
        assertFalse(RoomDefaults.showMicBanner(isHost = true, micOn = false, inRoom = false, entrySettled = true))
        assertFalse(RoomDefaults.showMicBanner(isHost = true, micOn = false, inRoom = true, entrySettled = false))
    }
}
