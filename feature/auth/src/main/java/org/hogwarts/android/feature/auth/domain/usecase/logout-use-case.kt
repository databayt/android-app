package org.hogwarts.android.feature.auth.domain.usecase

import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user logout.
 *
 * Clears tokens, session, and notifies backend.
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
