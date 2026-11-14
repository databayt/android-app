package org.hogwarts.android.feature.auth.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import org.hogwarts.android.feature.auth.domain.model.AuthResult
import javax.inject.Inject

/** Sign in with an email or student username and a password. */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(identifier: String, password: String, school: String? = null): Result<AuthResult> {
        return try {
            Result.Success(authRepository.login(identifier, password, school))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
