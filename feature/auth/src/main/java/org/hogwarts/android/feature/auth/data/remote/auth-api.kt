package org.hogwarts.android.feature.auth.data.remote

import org.hogwarts.android.feature.auth.data.remote.dto.AuthResponseDto
import org.hogwarts.android.feature.auth.data.remote.dto.FacebookAuthRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.GoogleAuthRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.LoginRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.NewPasswordRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.ResetPasswordRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.SchoolDto
import org.hogwarts.android.feature.auth.data.remote.dto.SignUpRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.VerifyOtpRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Retrofit API interface for authentication endpoints.
 *
 * Integrates with Hogwarts backend mobile API.
 */
interface AuthApi {

    /**
     * Login with email and password.
     */
    @POST("api/mobile/auth")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    /**
     * Login with Google ID token.
     */
    @POST("api/mobile/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleAuthRequestDto): Response<AuthResponseDto>

    /**
     * Login with Facebook access token.
     */
    @POST("api/mobile/auth/facebook")
    suspend fun loginWithFacebook(@Body request: FacebookAuthRequestDto): Response<AuthResponseDto>

    /**
     * Register a new user.
     */
    @POST("api/mobile/auth/register")
    suspend fun register(@Body request: SignUpRequestDto): Response<AuthResponseDto>

    /**
     * Request password reset OTP.
     */
    @POST("api/mobile/auth/reset")
    suspend fun requestPasswordReset(@Body request: ResetPasswordRequestDto): Response<Unit>

    /**
     * Verify OTP code.
     */
    @POST("api/mobile/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequestDto): Response<Unit>

    /**
     * Set new password after OTP verification.
     */
    @POST("api/mobile/auth/new-password")
    suspend fun setNewPassword(@Body request: NewPasswordRequestDto): Response<Unit>

    /**
     * Refresh access token.
     */
    @PUT("api/mobile/auth")
    suspend fun refreshToken(
        @Header("X-Refresh-Token") refreshToken: String
    ): Response<AuthResponseDto>

    /**
     * Logout and invalidate session.
     */
    @POST("api/mobile/auth/logout")
    suspend fun logout(): Response<Unit>

    /**
     * Get available schools for registration.
     */
    @GET("api/mobile/schools")
    suspend fun getSchools(): Response<List<SchoolDto>>
}
