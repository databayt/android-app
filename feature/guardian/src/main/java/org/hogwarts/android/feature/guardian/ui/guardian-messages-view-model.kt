package org.hogwarts.android.feature.guardian.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.guardian.data.repository.ChildTeacher
import org.hogwarts.android.feature.guardian.domain.usecase.GetChildTeachersUseCase
import org.hogwarts.android.feature.guardian.domain.usecase.GetChildrenUseCase
import javax.inject.Inject

data class GuardianMessagesUiState(
    val teachers: List<ChildTeacher> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GuardianMessagesViewModel @Inject constructor(
    private val getChildrenUseCase: GetChildrenUseCase,
    private val getChildTeachersUseCase: GetChildTeachersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuardianMessagesUiState())
    val uiState: StateFlow<GuardianMessagesUiState> = _uiState.asStateFlow()

    init {
        loadTeachers()
    }

    private fun loadTeachers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val childrenResult = getChildrenUseCase()) {
                is Result.Success -> {
                    val allTeachers = mutableListOf<ChildTeacher>()
                    for (child in childrenResult.data) {
                        when (val teachersResult = getChildTeachersUseCase(child.id)) {
                            is Result.Success -> allTeachers.addAll(teachersResult.data)
                            is Result.Error -> {}
                            is Result.Loading -> {}
                        }
                    }
                    // Deduplicate by teacher ID
                    val uniqueTeachers = allTeachers.distinctBy { it.id }
                    _uiState.update { it.copy(isLoading = false, teachers = uniqueTeachers) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = childrenResult.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
}
