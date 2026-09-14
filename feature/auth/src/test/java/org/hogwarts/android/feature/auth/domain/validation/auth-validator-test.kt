package org.hogwarts.android.feature.auth.domain.validation

import org.hogwarts.android.feature.auth.domain.model.FieldError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun `identifier mirrors createLoginSchema`() {
        assertEquals(FieldError.IdentifierRequired, AuthValidator.identifier("  "))
        assertNull(AuthValidator.identifier("admin@balqalam.com"))
        assertNull(AuthValidator.identifier(" student.1-a_b "))
        assertEquals(FieldError.InvalidIdentifier, AuthValidator.identifier("ab"))
        assertEquals(FieldError.InvalidIdentifier, AuthValidator.identifier("name with space"))
        assertEquals(FieldError.InvalidIdentifier, AuthValidator.identifier("admin@"))
        assertEquals(FieldError.InvalidIdentifier, AuthValidator.identifier("a".repeat(65)))
    }

    @Test
    fun `password is only required on login`() {
        assertEquals(FieldError.PasswordRequired, AuthValidator.password(""))
        assertNull(AuthValidator.password("1"))
    }

    @Test
    fun `new password needs six characters`() {
        assertEquals(FieldError.PasswordRequired, AuthValidator.newPassword(""))
        assertEquals(FieldError.PasswordTooShort, AuthValidator.newPassword("12345"))
        assertNull(AuthValidator.newPassword("123456"))
    }

    @Test
    fun `email`() {
        assertNull(AuthValidator.email("Parent@Balqalam.com"))
        assertEquals(FieldError.InvalidEmail, AuthValidator.email("parent"))
        assertEquals(FieldError.InvalidEmail, AuthValidator.email("a..b@x.com"))
    }
}
