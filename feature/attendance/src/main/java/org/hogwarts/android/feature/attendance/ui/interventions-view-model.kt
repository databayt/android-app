package org.hogwarts.android.feature.attendance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.attendance.domain.usecase.GetInterventionsUseCase
import javax.inject.Inject

/**
 * ViewModel for the Interventions screen.
 *
 * Displays students needing attention due to low attendance rates.
 * Used by teachers and admins.
 */
@HiltViewModel
class InterventionsViewModel @Inject constructor(
    private val getInterventionsUseCase: GetInterventionsUseCase,
    private val tenantContext: TenantContext
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterventionsUiState())
    val uiState: StateFlow<InterventionsUiState> = _uiState.asStateFlow()

    init {
        loadInterventions()
    }

    private fun loadInterventions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                tenantContext.requireSchoolId()
                val interventions = getInterventionsUseCase()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        interventions = interventions,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load interventions"
                    )
                }
            }
        }
    }

    fun setFilter(filter: InterventionFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun retry() {
        loadInterventions()
    }
}
