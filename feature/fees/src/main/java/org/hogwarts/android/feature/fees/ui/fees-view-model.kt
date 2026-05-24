package org.hogwarts.android.feature.fees.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.fees.domain.usecase.GetFeesUseCase
import javax.inject.Inject

@HiltViewModel
class FeesViewModel @Inject constructor(
    private val getFeesUseCase: GetFeesUseCase,
    private val tenantContext: TenantContext,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeesUiState())
    val uiState: StateFlow<FeesUiState> = _uiState.asStateFlow()

    init {
        loadFees()
    }

    private fun loadFees() {
        val userId = tenantContext.userId ?: return
        viewModelScope.launch {
            val status = _uiState.value.selectedStatusFilter
            getFeesUseCase(studentId = userId, status = status).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, fees = resource.data ?: it.fees)
                    }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, fees = resource.data ?: emptyList(), error = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, fees = resource.data ?: it.fees, error = resource.error?.message)
                    }
                }
            }
        }
    }

    fun onStatusFilterChanged(status: String?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
        loadFees()
    }
}
