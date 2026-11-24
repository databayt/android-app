package org.hogwarts.android.core.common.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLoginLinkTest {
    @Test
    fun `extracts the code and strips it from the href`() {
        val p = AppLoginLink.split("/live/42/room", "app_login=abc123")
        assertEquals("abc123", p.code)
        assertEquals("/live/42/room", p.href)
    }

    @Test
    fun `keeps the other query parameters in order`() {
        val p = AppLoginLink.split("/dashboard", "a=1&app_login=zz&b=2")
        assertEquals("zz", p.code)
        assertEquals("/dashboard?a=1&b=2", p.href)
    }

    @Test
    fun `no code leaves the href alone`() {
        val p = AppLoginLink.split("/dashboard", "chat=1")
        assertNull(p.code)
        assertEquals("/dashboard?chat=1", p.href)
        assertNull(AppLoginLink.split("/dashboard", null).code)
    }

    @Test
    fun `blank code is no code and a percent-encoded code is decoded`() {
        assertNull(AppLoginLink.split("/x", "app_login=").code)
        assertEquals("a b/c", AppLoginLink.split("/x", "app_login=a%20b%2Fc").code)
        assertEquals("/x", AppLoginLink.split("/x", "app_login=").href)
    }
}
