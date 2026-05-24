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
import org.hogwarts.android.feature.guardian.data.repository.ChildAttendanceRecord
import org.hogwarts.android.feature.guardian.domain.model.Child
import org.hogwarts.android.feature.guardian.domain.usecase.GetChildAttendanceUseCase
import org.hogwarts.android.feature.guardian.domain.usecase.GetChildrenUseCase
import javax.inject.Inject

data class ChildAttendanceUiState(
    val children: List<Child> = emptyList(),
    val selectedChildId: String? = null,
    val records: List<ChildAttendanceRecord> = emptyList(),
    val statusFilter: String = "All",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val filteredRecords: List<ChildAttendanceRecord>
        get() = if (statusFilter == "All") records
        else records.filter { it.status.equals(statusFilter, ignoreCase = true) }

    val presentCount: Int get() = records.count { it.status.equals("present", true) }
    val absentCount: Int get() = records.count { it.status.equals("absent", true) }
    val lateCount: Int get() = records.count { it.status.equals("late", true) }
    val excusedCount: Int get() = records.count { it.status.equals("excused", true) }
    val attendanceRate: Float
        get() {
            val total = records.size
            if (total == 0) return 0f
            return (presentCount + lateCount).toFloat() / total
        }
}

@HiltViewModel
class ChildAttendanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChildrenUseCase: GetChildrenUseCase,
    private val getChildAttendanceUseCase: GetChildAttendanceUseCase
) : ViewModel() {

    private val childId: String? = savedStateHandle["childId"]

    private val _uiState = MutableStateFlow(ChildAttendanceUiState())
    val uiState: StateFlow<ChildAttendanceUiState> = _uiState.asStateFlow()

    init {
        loadChildren()
    }

    private fun loadChildren() {
        viewModelScope.launch {
            when (val result = getChildrenUseCase()) {
                is Result.Success -> {
                    val selected = childId ?: result.data.firstOrNull()?.id
                    _uiState.update { it.copy(children = result.data, selectedChildId = selected) }
                    selected?.let { loadAttendance(it) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun selectChild(id: String) {
        _uiState.update { it.copy(selectedChildId = id) }
        loadAttendance(id)
    }

    fun setStatusFilter(filter: String) {
        _uiState.update { it.copy(statusFilter = filter) }
    }

    private fun loadAttendance(childId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getChildAttendanceUseCase(childId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, records = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
}
