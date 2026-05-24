package org.hogwarts.android.feature.auth.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import org.hogwarts.android.feature.auth.domain.model.AuthResult
import javax.inject.Inject

/**
 * Use case for Google OAuth authentication.
 *
 * Sends the Google ID token to the backend which validates it
 * and returns a Hogwarts JWT.
 */
class GoogleAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<AuthResult> {
        return try {
            val result = authRepository.loginWithGoogle(idToken)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
