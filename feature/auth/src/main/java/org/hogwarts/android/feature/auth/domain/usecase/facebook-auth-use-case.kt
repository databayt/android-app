package org.hogwarts.android.feature.auth.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import org.hogwarts.android.feature.auth.domain.model.AuthResult
import javax.inject.Inject

/**
 * Use case for Facebook OAuth authentication.
 *
 * Sends the Facebook access token to the backend which validates it
 * and returns a Hogwarts JWT.
 */
class FacebookAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(accessToken: String): Result<AuthResult> {
        return try {
            val result = authRepository.loginWithFacebook(accessToken)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
