package org.hogwarts.android.feature.auth.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SignupValidatorTest {

    // ──────────────────────────────────────────────
    // Email Validation
    // ──────────────────────────────────────────────

    @Test
    fun `valid email returns null`() {
        assertNull(SignupValidator.validateEmail("student@hogwarts.edu"))
    }

    @Test
    fun `blank email returns error`() {
        assertEquals("Email is required", SignupValidator.validateEmail(""))
    }

    @Test
    fun `whitespace-only email returns error`() {
        assertEquals("Email is required", SignupValidator.validateEmail("   "))
    }

    @Test
    fun `email without @ returns error`() {
        assertEquals("Invalid email format", SignupValidator.validateEmail("studenthogwarts.edu"))
    }

    @Test
    fun `email without dot returns error`() {
        assertEquals("Invalid email format", SignupValidator.validateEmail("student@hogwartsedu"))
    }

    @Test
    fun `email with @ and dot is valid`() {
        assertNull(SignupValidator.validateEmail("a@b.c"))
    }

    // ──────────────────────────────────────────────
    // Password Validation
    // ──────────────────────────────────────────────

    @Test
    fun `valid password returns null`() {
        assertNull(SignupValidator.validatePassword("Password1"))
    }

    @Test
    fun `blank password returns error`() {
        assertEquals("Password is required", SignupValidator.validatePassword(""))
    }

    @Test
    fun `whitespace-only password returns error`() {
        assertEquals("Password is required", SignupValidator.validatePassword("   "))
    }

    @Test
    fun `short password returns error`() {
        assertEquals(
            "Password must be at least 8 characters",
            SignupValidator.validatePassword("Pass1")
        )
    }

    @Test
    fun `password without uppercase returns error`() {
        assertEquals(
            "Password must contain an uppercase letter",
            SignupValidator.validatePassword("password1")
        )
    }

    @Test
    fun `password without lowercase returns error`() {
        assertEquals(
            "Password must contain a lowercase letter",
            SignupValidator.validatePassword("PASSWORD1")
        )
    }

    @Test
    fun `password without digit returns error`() {
        assertEquals(
            "Password must contain a number",
            SignupValidator.validatePassword("Passwordd")
        )
    }

    @Test
    fun `exactly 8 chars with all requirements is valid`() {
        assertNull(SignupValidator.validatePassword("Abcdefg1"))
    }

    @Test
    fun `7 chars with all requirements is too short`() {
        assertNotNull(SignupValidator.validatePassword("Abcdef1"))
    }
}
