package org.hogwarts.android.core.common.api

import org.junit.Assert.assertEquals
import org.junit.Test

class SilentReSignInTest {
    @Test
    fun `remembered login means one attempt`() {
        assertEquals(SilentReSignIn.Decision.Attempt, SilentReSignIn.decide(RememberedLogin("a@b.c", "pw", null)))
    }

    @Test
    fun `no login or no password goes to the login screen`() {
        assertEquals(SilentReSignIn.Decision.GoToLogin, SilentReSignIn.decide(null))
        assertEquals(SilentReSignIn.Decision.GoToLogin, SilentReSignIn.decide(RememberedLogin("a@b.c", "", null)))
        assertEquals(SilentReSignIn.Decision.GoToLogin, SilentReSignIn.decide(RememberedLogin(" ", "pw", null)))
    }
}
