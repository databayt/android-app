package org.hogwarts.android.shell

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IncomingLinkTest {
    @Test
    fun classRoomLinkLosesItsLocale() {
        assertEquals("/live/abc123/room", IncomingLink.path("https", "myschool.balqalam.com", "/ar/live/abc123/room"))
        assertEquals("/live/abc123", IncomingLink.path("https", "myschool.balqalam.com", "/en/live/abc123/"))
    }

    @Test
    fun apexAndBarePaths() {
        assertEquals("/live/abc123", IncomingLink.path("https", "balqalam.com", "/live/abc123"))
        assertEquals("/", IncomingLink.path("https", "myschool.balqalam.com", "/ar"))
    }

    @Test
    fun otherHostsAndSchemesAreNotOurs() {
        assertNull(IncomingLink.path("https", "example.com", "/ar/live/abc123"))
        assertNull(IncomingLink.path("https", "evilbalqalam.com", "/ar/live/abc123"))
        assertNull(IncomingLink.path("http", "myschool.balqalam.com", "/ar/live/abc123"))
        assertNull(IncomingLink.path("https", null, "/ar/live/abc123"))
    }
}
