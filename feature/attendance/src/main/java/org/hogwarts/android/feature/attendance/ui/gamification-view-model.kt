package org.hogwarts.android.feature.attendance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.attendance.domain.usecase.GetBadgesUseCase
import org.hogwarts.android.feature.attendance.domain.usecase.GetStreaksUseCase
import javax.inject.Inject

/**
 * ViewModel for the Gamification screen (badges, streaks, points).
 */
@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val getBadgesUseCase: GetBadgesUseCase,
    private val getStreaksUseCase: GetStreaksUseCase,
    private val tenantContext: TenantContext
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamificationUiState())
    val uiState: StateFlow<GamificationUiState> = _uiState.asStateFlow()

    init {
        loadGamificationData()
    }

    private fun loadGamificationData() {
        val userId = tenantContext.userId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val badgesDeferred = async { getBadgesUseCase(studentId = userId) }
                val streaksDeferred = async { getStreaksUseCase(studentId = userId) }

                val badges = badgesDeferred.await()
                val streak = streaksDeferred.await()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        badges = badges,
                        streak = streak,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load gamification data"
                    )
                }
            }
        }
    }

    fun retry() {
        loadGamificationData()
    }
}
