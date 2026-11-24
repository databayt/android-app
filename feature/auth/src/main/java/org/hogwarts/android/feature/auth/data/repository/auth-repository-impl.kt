package org.hogwarts.android.feature.auth.data.repository

import kotlinx.coroutines.CancellationException
import org.hogwarts.android.core.data.tenant.CurrentUser
import org.hogwarts.android.core.data.tenant.SessionManager
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.common.api.SilentReSignIn
import org.hogwarts.android.core.security.CredentialManager
import org.hogwarts.android.core.security.TokenManager
import org.hogwarts.android.feature.auth.data.remote.AuthApi
import org.hogwarts.android.feature.auth.data.remote.dto.AuthResponseDto
import org.hogwarts.android.feature.auth.data.remote.dto.GoogleAuthRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.HandoffRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.LoginRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.NewPasswordRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.ResetPasswordRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.SchoolDto
import org.hogwarts.android.feature.auth.data.remote.dto.SignUpRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.UserDto
import org.hogwarts.android.feature.auth.data.remote.dto.VerifyOtpRequestDto
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.hogwarts.android.feature.auth.domain.model.AuthException
import org.hogwarts.android.feature.auth.domain.model.AuthResult
import org.hogwarts.android.feature.auth.domain.model.SchoolInfo
import org.hogwarts.android.feature.auth.domain.model.SocialLogin
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [AuthRepository] over hogwarts `api/mobile/auth/`. Tokens are saved only
 * after the role parses, so an unknown role never leaves a half session.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager,
    private val credentialManager: CredentialManager
) : AuthRepository {

    override suspend fun login(identifier: String, password: String, school: String?): AuthResult {
        val response = call { authApi.login(LoginRequestDto(identifier.trim(), password, school)) }
        return saveAuthResult(response.bodyOrThrow(AuthEndpoint.Login))
    }

    override suspend fun loginWithGoogle(idToken: String, schoolId: String?): SocialLogin {
        val body = call { authApi.loginWithGoogle(GoogleAuthRequestDto(idToken, schoolId)) }
            .bodyOrThrow(AuthEndpoint.Social)

        if (body.needsSchool) {
            if (body.schools.isEmpty()) throw AuthException(AuthError.NoSchool)
            return SocialLogin.NeedsSchool(body.schools.map { it.toInfo() })
        }
        val user = body.user
        val accessToken = body.accessToken
        if (user == null || accessToken.isNullOrEmpty() || body.expiresAt == null) {
            throw AuthException(AuthError.Generic)
        }
        return SocialLogin.Authenticated(
            saveAuthResult(AuthResponseDto(accessToken, body.refreshToken, body.expiresAt, user))
        )
    }

    override suspend fun signInWithHandoff(code: String, onReplacingSession: suspend () -> Unit): AuthResult? {
        val body = try {
            val response = call { authApi.handoff(HandoffRequestDto(code)) }
            if (!response.isSuccessful) return null
            response.body() ?: return null
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return null
        }
        val signedIn = tokenManager.hasTokens
        val sameUser = HandoffPolicy.isSameUser(signedIn, sessionManager.currentUser?.id, body.user.id)
        // The browser's person is the person at the phone: another user's session is replaced.
        if (signedIn && !sameUser) {
            onReplacingSession()
            tokenManager.clearTokens()
            sessionManager.clearSession()
            credentialManager.clearCredentials()
        }
        return try {
            saveAuthResult(body)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        schoolId: String
    ): AuthResult {
        val response = call {
            authApi.register(
                SignUpRequestDto(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    schoolId = schoolId
                )
            )
        }
        return saveAuthResult(response.bodyOrThrow(AuthEndpoint.Login))
    }

    override suspend fun requestPasswordReset(email: String) {
        call { authApi.requestPasswordReset(ResetPasswordRequestDto(email.trim().lowercase())) }
            .throwIfFailed(AuthEndpoint.Reset)
    }

    override suspend fun verifyOtp(email: String, otp: String) {
        call { authApi.verifyOtp(VerifyOtpRequestDto(email.trim().lowercase(), otp)) }
            .throwIfFailed(AuthEndpoint.NewPassword)
    }

    override suspend fun setNewPassword(email: String, otp: String, newPassword: String) {
        call { authApi.setNewPassword(NewPasswordRequestDto(email.trim().lowercase(), otp, newPassword)) }
            .throwIfFailed(AuthEndpoint.NewPassword)
    }

    override suspend fun refreshToken(): AuthResult {
        val refreshToken = tokenManager.refreshToken
            ?: throw AuthException(AuthError.Generic)

        val response = call { authApi.refreshToken(refreshToken) }
        if (!response.isSuccessful) {
            // Only a refusal ends the session. A 5xx or a gateway error is the server's bad minute, not
            // the reader's: signing them out for it would cost a teacher her class on a slow morning.
            if (response.code() == 401 || response.code() == 403) {
                // One silent re-sign-in with the remembered login before the login screen.
                reSignInSilently()?.let { return it }
                tokenManager.clearTokens()
                sessionManager.clearSession()
            }
            throw AuthException(authErrorFor(AuthEndpoint.Login, response.code(), null))
        }
        return saveAuthResult(response.body() ?: throw AuthException(AuthError.Generic))
    }

    /** The refresh token was refused: one login with the remembered password. A refusal forgets it. */
    private suspend fun reSignInSilently(): AuthResult? {
        val login = credentialManager.rememberedLogin
        if (SilentReSignIn.decide(login) == SilentReSignIn.Decision.GoToLogin || login == null) return null
        return try {
            val response = call { authApi.login(LoginRequestDto(login.identifier, login.password, login.school)) }
            when {
                response.isSuccessful -> response.body()?.let { saveAuthResult(it) }
                response.code() == 401 || response.code() == 403 -> {
                    credentialManager.forgetPassword()
                    null
                }
                else -> null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun logout() {
        try {
            authApi.logout()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Ignore logout API errors - we're logging out anyway
        } finally {
            tokenManager.clearTokens()
            sessionManager.clearSession()
        }
    }

    override suspend fun getSchools(): List<SchoolInfo> =
        call { authApi.getSchools() }.bodyOrThrow(AuthEndpoint.Social).map { it.toInfo() }

    override val isAuthenticated: Boolean
        get() = tokenManager.hasTokens

    override val currentSchoolId: String?
        get() = sessionManager.currentUser?.schoolId

    override val currentUserRole: String?
        get() = sessionManager.currentUser?.role?.name

    /** Network failures become [AuthError.Network]. */
    private suspend fun <T> call(block: suspend () -> Response<T>): Response<T> =
        try {
            block()
        } catch (e: IOException) {
            throw AuthException(AuthError.Network, e)
        }

    private fun <T> Response<T>.throwIfFailed(endpoint: AuthEndpoint) {
        if (!isSuccessful) {
            throw AuthException(authErrorFor(endpoint, code(), parseErrorMessage(errorBody()?.string())))
        }
    }

    private fun <T> Response<T>.bodyOrThrow(endpoint: AuthEndpoint): T {
        throwIfFailed(endpoint)
        return body() ?: throw AuthException(AuthError.Generic)
    }

    private suspend fun saveAuthResult(body: AuthResponseDto): AuthResult {
        // Resolve the role before persisting anything, so an unexpected role
        // can never leave tokens saved without a session.
        val role = UserRole.fromWire(body.user.role)
        if (role == UserRole.UNKNOWN) {
            throw AuthException(AuthError.UnsupportedRole)
        }

        tokenManager.saveTokens(
            accessToken = body.accessToken,
            refreshToken = body.refreshToken,
            expiryMillis = body.expiresAt
        )

        val user: UserDto = body.user
        val userEmail = user.email.orEmpty()
        val userSchoolId = user.schoolId.orEmpty()

        sessionManager.setUser(
            CurrentUser(
                id = user.id,
                email = userEmail,
                schoolId = userSchoolId,
                role = role,
                givenName = user.givenName,
                familyName = user.familyName,
                grade = user.grade
            )
        )

        return AuthResult(
            userId = user.id,
            email = userEmail,
            schoolId = userSchoolId,
            role = role,
            givenName = user.givenName,
            familyName = user.familyName,
            accessToken = body.accessToken,
            refreshToken = body.refreshToken,
            expiresAt = body.expiresAt,
            grade = user.grade
        )
    }
}

private fun SchoolDto.toInfo() = SchoolInfo(id = id, name = name, nameEn = nameEn, logoUrl = logoUrl, domain = domain)

/** Who the browser hand-off signs in, relative to the phone's current session. */
object HandoffPolicy {
    /** Same user = signed in AND the stored user id equals the hand-off's; an unknown stored user is "different". */
    fun isSameUser(signedIn: Boolean, currentUserId: String?, handoffUserId: String): Boolean =
        signedIn && currentUserId != null && currentUserId == handoffUserId
}
