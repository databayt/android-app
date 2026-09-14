package org.hogwarts.android.feature.auth.domain.validation

import org.hogwarts.android.feature.auth.domain.model.FieldError

/**
 * The web's auth schemas (`src/components/auth/validation.ts`), field by field.
 */
object AuthValidator {
    /** `USERNAME_LOGIN_RE` — generated per-school student handles. */
    private val USERNAME = Regex("^[A-Za-z0-9._-]{3,64}$")

    /** zod's `.email()` pattern. */
    private val EMAIL = Regex("^(?!\\.)(?!.*\\.\\.)([A-Za-z0-9_'+\\-.]*)[A-Za-z0-9_+-]@([A-Za-z0-9][A-Za-z0-9\\-]*\\.)+[A-Za-z]{2,}$")

    const val MIN_PASSWORD = 6

    fun identifier(value: String): FieldError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> FieldError.IdentifierRequired
            trimmed.contains('@') -> if (EMAIL.matches(trimmed)) null else FieldError.InvalidIdentifier
            USERNAME.matches(trimmed) -> null
            else -> FieldError.InvalidIdentifier
        }
    }

    fun password(value: String): FieldError? =
        if (value.isEmpty()) FieldError.PasswordRequired else null

    fun email(value: String): FieldError? =
        if (EMAIL.matches(value.trim())) null else FieldError.InvalidEmail

    fun newPassword(value: String): FieldError? = when {
        value.isEmpty() -> FieldError.PasswordRequired
        value.length < MIN_PASSWORD -> FieldError.PasswordTooShort
        else -> null
    }
}
