package org.hogwarts.android.feature.auth.ui

import org.hogwarts.android.feature.auth.R
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthFrameTest {
    @Test
    fun `maskEmail matches the web`() {
        assertEquals("pa****@balqalam.com", maskEmail("parent@balqalam.com"))
        assertEquals("ab@x.com", maskEmail("ab@x.com"))
        assertEquals("a@x.com", maskEmail("a@x.com"))
        assertEquals("no-at", maskEmail("no-at"))
    }

    @Test
    fun `every error has web wording`() {
        assertEquals(R.string.auth_error_invalid_credentials, AuthError.InvalidCredentials.messageRes())
        assertEquals(R.string.auth_error_suspended, AuthError.Suspended.messageRes())
        assertEquals(R.string.auth_error_permission_denied, AuthError.UnsupportedRole.messageRes())
        AuthError.entries.forEach { it.messageRes() }
    }
}
