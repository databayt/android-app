package org.hogwarts.android.feature.auth.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import org.hogwarts.android.feature.auth.domain.model.SocialLogin
import javax.inject.Inject

/**
 * Google sign-in: the backend verifies the ID token and answers with tokens
 * or with the schools the email belongs to ([SocialLogin.NeedsSchool]).
 */
class GoogleAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String, schoolId: String? = null): Result<SocialLogin> {
        return try {
            Result.Success(authRepository.loginWithGoogle(idToken, schoolId))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
