package org.hogwarts.android.core.common.api

/** The login the phone remembers: what a silent re-sign-in sends to `POST api/mobile/auth`. */
data class RememberedLogin(
    val identifier: String,
    val password: String,
    /** The school subdomain; only set for a login code or username, never for an email. */
    val school: String? = null,
)

/**
 * The decision after a refresh token is refused: one silent re-sign-in with the remembered
 * login, or the login screen. Pure, so the "never loop" rule is testable: a failed attempt
 * forgets the password, which turns every later decision into [GoToLogin].
 */
object SilentReSignIn {
    enum class Decision { Attempt, GoToLogin }

    fun decide(login: RememberedLogin?): Decision =
        if (login != null && login.identifier.isNotBlank() && login.password.isNotEmpty()) Decision.Attempt
        else Decision.GoToLogin
}
