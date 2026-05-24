package org.hogwarts.android.feature.students.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.students.domain.usecase.GetStudentsUseCase
import javax.inject.Inject

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val getStudentsUseCase: GetStudentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentsUiState())
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            val query = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
            val status = _uiState.value.selectedStatusFilter
            getStudentsUseCase(search = query, status = status).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, students = resource.data ?: it.students)
                    }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, students = resource.data ?: emptyList(), error = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, students = resource.data ?: it.students, error = resource.error?.message)
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadStudents()
        }
    }

    fun onStatusFilterChanged(status: String?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
        loadStudents()
    }
}
