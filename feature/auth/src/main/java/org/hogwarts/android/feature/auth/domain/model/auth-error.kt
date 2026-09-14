package org.hogwarts.android.feature.auth.domain.model

/**
 * Why an auth call failed, independent of wording. Screens turn each into the
 * web's localized message (`messages.errors.auth.*`, `auth.*`).
 */
enum class AuthError {
    /** 401 — "Invalid email or password". */
    InvalidCredentials,

    /** 401 — the account has no password; it signs in with Google. */
    SocialOnly,

    /** 403 on sign-in — the account is suspended. */
    Suspended,

    /** 403 on new-password — the account cannot be changed from the app. */
    PermissionDenied,

    /** 404. */
    AccountNotFound,

    /** 429. */
    TooManyRequests,

    /** No connection / timeout. */
    Network,

    /** 401 on the code step. */
    InvalidCode,

    /** 401 on the code step, the code's 10 minutes are up. */
    CodeExpired,

    /** Social sign-in found no school the email belongs to. */
    NoSchool,

    /** Tokens came back for a role the app does not know. */
    UnsupportedRole,

    /** Saved biometric credentials no longer sign in. */
    BiometricFailed,

    /** Anything else. */
    Generic,
}

/** A form field's own complaint — mirrors `createLoginSchema` / reset / new-password schemas. */
enum class FieldError {
    IdentifierRequired,
    InvalidIdentifier,
    PasswordRequired,
    InvalidEmail,
    PasswordTooShort,
}

/** Thrown by the repository; [error] is what the UI shows. */
class AuthException(val error: AuthError, cause: Throwable? = null) : Exception(error.name, cause)

/** The kind of failure for any throwable a use case caught. */
fun Throwable.toAuthError(): AuthError = when (this) {
    is AuthException -> error
    is java.io.IOException -> AuthError.Network
    else -> AuthError.Generic
}
