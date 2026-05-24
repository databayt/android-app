package org.hogwarts.android.feature.auth.domain.model

data class OAuthConfig(
    val provider: OAuthProvider,
    val clientId: String,
    val redirectUri: String,
    val scopes: List<String>
)

enum class OAuthProvider {
    GOOGLE, FACEBOOK;

    val displayName: String get() = when (this) {
        GOOGLE -> "Google"
        FACEBOOK -> "Facebook"
    }
}

data class OAuthResult(
    val provider: OAuthProvider,
    val idToken: String?,
    val accessToken: String?,
    val email: String?,
    val displayName: String?,
    val error: String? = null
) {
    val isSuccess: Boolean get() = error == null && (idToken != null || accessToken != null)
}
