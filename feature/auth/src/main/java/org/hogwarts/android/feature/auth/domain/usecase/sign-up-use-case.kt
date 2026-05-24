package org.hogwarts.android.feature.auth.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import org.hogwarts.android.feature.auth.domain.model.AuthResult
import javax.inject.Inject

/**
 * Use case for user registration.
 */
class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        schoolId: String
    ): Result<AuthResult> {
        return try {
            val result = authRepository.register(firstName, lastName, email, password, schoolId)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
