package org.hogwarts.android.feature.guardian.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.feature.guardian.data.repository.ChildFeeRecord
import org.hogwarts.android.feature.guardian.domain.model.Child
import org.hogwarts.android.feature.guardian.domain.usecase.GetChildFeesUseCase
import org.hogwarts.android.feature.guardian.domain.usecase.GetChildrenUseCase
import javax.inject.Inject

data class ChildFeesUiState(
    val children: List<Child> = emptyList(),
    val selectedChildId: String? = null,
    val fees: List<ChildFeeRecord> = emptyList(),
    val statusFilter: String = "All",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val filteredFees: List<ChildFeeRecord>
        get() = if (statusFilter == "All") fees
        else fees.filter { it.status.equals(statusFilter, ignoreCase = true) }

    val totalOutstanding: Double
        get() = fees.filter { !it.status.equals("paid", true) }.sumOf { it.amount - it.paidAmount }

    val overdueAmount: Double
        get() = fees.filter { it.status.equals("overdue", true) }.sumOf { it.amount - it.paidAmount }
}

@HiltViewModel
class ChildFeesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChildrenUseCase: GetChildrenUseCase,
    private val getChildFeesUseCase: GetChildFeesUseCase,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val childId: String? = savedStateHandle["childId"]

    private val _uiState = MutableStateFlow(ChildFeesUiState())
    val uiState: StateFlow<ChildFeesUiState> = _uiState.asStateFlow()

    init {
        loadChildren()
    }

    private fun loadChildren() {
        viewModelScope.launch {
            when (val result = getChildrenUseCase()) {
                is Result.Success -> {
                    val selected = childId ?: result.data.firstOrNull()?.id
                    _uiState.update { it.copy(children = result.data, selectedChildId = selected) }
                    selected?.let { loadFees(it) }
                }
                is Result.Error -> _uiState.update { it.copy(error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun selectChild(id: String) {
        _uiState.update { it.copy(selectedChildId = id) }
        loadFees(id)
    }

    fun setStatusFilter(filter: String) {
        _uiState.update { it.copy(statusFilter = filter) }
    }

    private fun loadFees(childId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getChildFeesUseCase(childId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, fees = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }
}
