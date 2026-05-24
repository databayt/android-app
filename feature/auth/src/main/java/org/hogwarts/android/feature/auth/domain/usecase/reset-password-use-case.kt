package org.hogwarts.android.feature.auth.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for requesting password reset OTP.
 */
class RequestPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return try {
            authRepository.requestPasswordReset(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

/**
 * Use case for verifying OTP code.
 */
class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String): Result<Unit> {
        return try {
            authRepository.verifyOtp(email, otp)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

/**
 * Use case for setting a new password after OTP verification.
 */
class SetNewPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String, newPassword: String): Result<Unit> {
        return try {
            authRepository.setNewPassword(email, otp, newPassword)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
