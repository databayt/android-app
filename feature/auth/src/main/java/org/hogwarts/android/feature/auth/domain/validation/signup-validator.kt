package org.hogwarts.android.feature.auth.domain.validation

/**
 * Validates sign-up form fields.
 */
object SignupValidator {

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !email.contains("@") || !email.contains(".") -> "Invalid email format"
        else -> null
    }

    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "Password is required"
        if (password.length < 8) return "Password must be at least 8 characters"
        if (!password.any { it.isUpperCase() }) return "Password must contain an uppercase letter"
        if (!password.any { it.isLowerCase() }) return "Password must contain a lowercase letter"
        if (!password.any { it.isDigit() }) return "Password must contain a number"
        return null
    }
}
