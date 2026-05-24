package org.hogwarts.android.feature.reportcards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.reportcards.domain.model.ReportCard
import org.hogwarts.android.feature.reportcards.domain.usecase.GetReportCardsUseCase
import javax.inject.Inject

/**
 * UI state for the report cards list screen.
 */
data class ReportCardsListUiState(
    val isLoading: Boolean = true,
    val reportCards: List<ReportCard> = emptyList(),
    val error: String? = null,
    val isGuardian: Boolean = false,
    val children: List<ChildInfo> = emptyList(),
    val selectedChildId: String? = null
)

/**
 * Simple child info for guardian child selector.
 */
data class ChildInfo(
    val id: String,
    val name: String
)

/**
 * ViewModel for the report cards list screen.
 *
 * Handles loading report cards, guardian child selection,
 * and retry logic.
 */
@HiltViewModel
class ReportCardsListViewModel @Inject constructor(
    private val getReportCardsUseCase: GetReportCardsUseCase,
    private val tenantContext: TenantContext
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportCardsListUiState())
    val uiState: StateFlow<ReportCardsListUiState> = _uiState.asStateFlow()

    init {
        setupGuardianContext()
        loadReportCards()
    }

    /**
     * Check if user is a guardian and set up child selector context.
     */
    private fun setupGuardianContext() {
        val isGuardian = tenantContext.hasRole(UserRole.GUARDIAN)
        _uiState.update { it.copy(isGuardian = isGuardian) }

        // Guardian children would be loaded from a separate endpoint
        // or provided by the guardian module. For now, report cards
        // will load for the current user. When children list is available,
        // the guardian can switch between them.
    }

    /**
     * Load report cards from the backend.
     */
    fun loadReportCards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val studentId = if (_uiState.value.isGuardian) {
                _uiState.value.selectedChildId
            } else {
                null // The backend returns data for the authenticated user
            }

            when (val result = getReportCardsUseCase(studentId = studentId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reportCards = result.data,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to load report cards"
                        )
                    }
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    /**
     * Switch selected child (guardian only) and reload report cards.
     */
    fun selectChild(childId: String) {
        _uiState.update { it.copy(selectedChildId = childId) }
        loadReportCards()
    }

    /**
     * Retry loading report cards after an error.
     */
    fun retry() {
        loadReportCards()
    }
}
