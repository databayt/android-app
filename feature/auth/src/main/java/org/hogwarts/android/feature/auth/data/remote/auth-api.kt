package org.hogwarts.android.feature.auth.data.remote

import org.hogwarts.android.feature.auth.data.remote.dto.AuthResponseDto
import org.hogwarts.android.feature.auth.data.remote.dto.GoogleAuthRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.LoginRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.NewPasswordRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.ResetPasswordRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.SchoolDto
import org.hogwarts.android.feature.auth.data.remote.dto.SignUpRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.SocialAuthResponseDto
import org.hogwarts.android.feature.auth.data.remote.dto.VerifyOtpRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * hogwarts `src/app/api/mobile/auth/` and `api/mobile/schools`.
 * Failures carry `{ error }`; see [org.hogwarts.android.feature.auth.data.repository.authErrorFor].
 */
interface AuthApi {

    /** `{ identifier, password }` — identifier is an email or a student username. */
    @POST("api/mobile/auth")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    /** Tokens, or `200 { needs_school, schools }` for a platform-level Google identity. */
    @POST("api/mobile/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleAuthRequestDto): Response<SocialAuthResponseDto>

    @POST("api/mobile/auth/register")
    suspend fun register(@Body request: SignUpRequestDto): Response<AuthResponseDto>

    /** Always 200 (no email enumeration); emails a 6-digit code valid 10 minutes. */
    @POST("api/mobile/auth/reset")
    suspend fun requestPasswordReset(@Body request: ResetPasswordRequestDto): Response<Unit>

    /**
     * Checks a code — and DELETES it on success, so a later
     * [setNewPassword] with the same code is rejected. The reset flow therefore
     * does not call this; `new-password` validates the code itself.
     */
    @POST("api/mobile/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequestDto): Response<Unit>

    /** `{ email, otp, new_password }` — validates and consumes the code. */
    @POST("api/mobile/auth/new-password")
    suspend fun setNewPassword(@Body request: NewPasswordRequestDto): Response<Unit>

    @PUT("api/mobile/auth")
    suspend fun refreshToken(
        @Header("X-Refresh-Token") refreshToken: String
    ): Response<AuthResponseDto>

    @POST("api/mobile/auth/logout")
    suspend fun logout(): Response<Unit>

    /** Published, active schools — `[{ id, name, name_en, logo_url, domain }]`. */
    @GET("api/mobile/schools")
    suspend fun getSchools(): Response<List<SchoolDto>>
}
