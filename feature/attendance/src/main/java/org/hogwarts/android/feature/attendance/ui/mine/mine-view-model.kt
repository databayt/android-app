package org.hogwarts.android.feature.attendance.ui.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepository
import org.hogwarts.android.feature.attendance.domain.model.ChildAttendance
import org.hogwarts.android.feature.attendance.domain.model.StudentAttendance
import javax.inject.Inject

sealed interface MineUiState {
    data object Loading : MineUiState
    data class Guardian(val children: List<ChildAttendance>) : MineUiState
    data class Student(val attendance: StudentAttendance) : MineUiState

    /** Neither a guardian with children nor a student — or the reads failed. */
    data class Unavailable(val failed: Boolean) : MineUiState
}

/**
 * A student's or a guardian's own attendance — mirrors
 * `attendance/overview/student-guardian-overview.tsx`: guardian first, then student.
 */
@HiltViewModel
class MineViewModel @Inject constructor(
    private val repository: AttendanceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MineUiState>(MineUiState.Loading)
    val uiState: StateFlow<MineUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = MineUiState.Loading
        viewModelScope.launch {
            var failed = false
            val children = try {
                repository.guardianAttendance()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failed = true
                emptyList()
            }
            if (children.isNotEmpty()) {
                _uiState.value = MineUiState.Guardian(children)
                return@launch
            }
            val student = try {
                repository.studentAttendance()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failed = true
                null
            }
            _uiState.update { student?.let { MineUiState.Student(it) } ?: MineUiState.Unavailable(failed) }
        }
    }
}
