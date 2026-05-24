package org.hogwarts.android.feature.auth.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import org.hogwarts.android.feature.auth.domain.model.AuthResult
import javax.inject.Inject

/**
 * Use case for user authentication.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Authenticate user with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @return Result containing AuthResult on success or error
     */
    suspend operator fun invoke(email: String, password: String): Result<AuthResult> {
        return try {
            val result = authRepository.login(email, password)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
