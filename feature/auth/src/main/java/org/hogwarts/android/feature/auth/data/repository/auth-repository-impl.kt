package org.hogwarts.android.feature.auth.data.repository

import org.hogwarts.android.core.data.tenant.CurrentUser
import org.hogwarts.android.core.data.tenant.SessionManager
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.security.TokenManager
import org.hogwarts.android.feature.auth.data.remote.AuthApi
import org.hogwarts.android.feature.auth.data.remote.dto.AuthResponseDto
import org.hogwarts.android.feature.auth.data.remote.dto.FacebookAuthRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.GoogleAuthRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.LoginRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.NewPasswordRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.ResetPasswordRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.SignUpRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.VerifyOtpRequestDto
import org.hogwarts.android.feature.auth.domain.model.AuthResult
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository.
 *
 * Handles authentication against Hogwarts backend API.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult {
        val response = authApi.login(LoginRequestDto(email, password))
        return handleAuthResponse(response)
    }

    override suspend fun loginWithGoogle(idToken: String): AuthResult {
        val response = authApi.loginWithGoogle(GoogleAuthRequestDto(idToken))
        return handleAuthResponse(response)
    }

    override suspend fun loginWithFacebook(accessToken: String): AuthResult {
        val response = authApi.loginWithFacebook(FacebookAuthRequestDto(accessToken))
        return handleAuthResponse(response)
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        schoolId: String
    ): AuthResult {
        val response = authApi.register(
            SignUpRequestDto(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                schoolId = schoolId
            )
        )
        return handleAuthResponse(response)
    }

    override suspend fun requestPasswordReset(email: String) {
        val response = authApi.requestPasswordReset(ResetPasswordRequestDto(email))
        if (!response.isSuccessful) {
            throw AuthException(
                when (response.code()) {
                    404 -> "Email not found"
                    429 -> "Too many attempts. Please try again later."
                    else -> "Failed to send reset email: ${response.message()}"
                }
            )
        }
    }

    override suspend fun verifyOtp(email: String, otp: String) {
        val response = authApi.verifyOtp(VerifyOtpRequestDto(email, otp))
        if (!response.isSuccessful) {
            throw AuthException(
                when (response.code()) {
                    400 -> "Invalid or expired OTP"
                    429 -> "Too many attempts. Please try again later."
                    else -> "Verification failed: ${response.message()}"
                }
            )
        }
    }

    override suspend fun setNewPassword(email: String, otp: String, newPassword: String) {
        val response = authApi.setNewPassword(NewPasswordRequestDto(email, otp, newPassword))
        if (!response.isSuccessful) {
            throw AuthException(
                when (response.code()) {
                    400 -> "Invalid or expired OTP"
                    422 -> "Password does not meet requirements"
                    else -> "Failed to set new password: ${response.message()}"
                }
            )
        }
    }

    override suspend fun refreshToken(): AuthResult {
        val refreshToken = tokenManager.refreshToken
            ?: throw AuthException("No refresh token available")

        val response = authApi.refreshToken(refreshToken)

        if (!response.isSuccessful) {
            tokenManager.clearTokens()
            sessionManager.clearSession()
            throw AuthException("Session expired. Please login again.")
        }

        val body = response.body() ?: throw AuthException("Empty response from server")
        return saveAuthResult(body)
    }

    override suspend fun logout() {
        try {
            authApi.logout()
        } catch (e: Exception) {
            // Ignore logout API errors - we're logging out anyway
        } finally {
            tokenManager.clearTokens()
            sessionManager.clearSession()
        }
    }

    override suspend fun getSchools(): List<SchoolInfo> {
        val response = authApi.getSchools()
        if (!response.isSuccessful) {
            throw AuthException("Failed to load schools")
        }
        return response.body()?.map { SchoolInfo(id = it.id, name = it.name) } ?: emptyList()
    }

    override val isAuthenticated: Boolean
        get() = tokenManager.hasTokens

    override val currentSchoolId: String?
        get() = sessionManager.currentUser?.schoolId

    override val currentUserRole: String?
        get() = sessionManager.currentUser?.role?.name

    private suspend fun handleAuthResponse(response: Response<AuthResponseDto>): AuthResult {
        if (!response.isSuccessful) {
            throw AuthException(
                when (response.code()) {
                    401 -> "Invalid credentials"
                    404 -> "Account not found"
                    409 -> "Account already exists"
                    422 -> "Invalid input"
                    429 -> "Too many attempts. Please try again later."
                    else -> "Authentication failed: ${response.message()}"
                }
            )
        }

        val body = response.body() ?: throw AuthException("Empty response from server")
        return saveAuthResult(body)
    }

    private suspend fun saveAuthResult(body: AuthResponseDto): AuthResult {
        tokenManager.saveTokens(
            accessToken = body.accessToken,
            refreshToken = body.refreshToken,
            expiryMillis = body.expiresAt
        )

        val userEmail = body.user.email.orEmpty()
        val userSchoolId = body.user.schoolId.orEmpty()

        val currentUser = CurrentUser(
            id = body.user.id,
            email = userEmail,
            schoolId = userSchoolId,
            role = UserRole.valueOf(body.user.role),
            givenName = body.user.givenName,
            familyName = body.user.familyName,
            grade = body.user.grade
        )
        sessionManager.setUser(currentUser)

        return AuthResult(
            userId = body.user.id,
            email = userEmail,
            schoolId = userSchoolId,
            role = UserRole.valueOf(body.user.role),
            givenName = body.user.givenName,
            familyName = body.user.familyName,
            accessToken = body.accessToken,
            refreshToken = body.refreshToken,
            expiresAt = body.expiresAt,
            grade = body.user.grade
        )
    }
}

/**
 * Exception for authentication errors.
 */
class AuthException(message: String) : Exception(message)
