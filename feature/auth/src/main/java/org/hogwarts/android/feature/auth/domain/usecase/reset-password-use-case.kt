package org.hogwarts.android.feature.auth.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import javax.inject.Inject

/** Email a 6-digit reset code. */
class RequestPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return try {
            authRepository.requestPasswordReset(email)
            Result.Success(Unit)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

/**
 * Set a new password with the emailed code. The code is checked here, by
 * `new-password`, not by a prior `verify-otp` call: that route deletes the
 * code on success, which would make this call fail.
 */
class SetNewPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String, newPassword: String): Result<Unit> {
        return try {
            authRepository.setNewPassword(email, otp, newPassword)
            Result.Success(Unit)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
