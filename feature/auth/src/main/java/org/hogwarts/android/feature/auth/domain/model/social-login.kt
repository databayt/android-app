package org.hogwarts.android.feature.auth.domain.model


/** The two answers a social sign-in can give. */
sealed interface SocialLogin {
    data class Authenticated(val result: AuthResult) : SocialLogin

    /** No tokens yet: pick one of [schools] and sign in again with its id. */
    data class NeedsSchool(val schools: List<SchoolInfo>) : SocialLogin
}
