package org.hogwarts.android.feature.teacher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.teacher.domain.model.TeacherClass
import org.hogwarts.android.feature.teacher.domain.usecase.GetMyClassesUseCase
import javax.inject.Inject

data class MyClassesUiState(
    val classes: List<TeacherClass> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MyClassesViewModel @Inject constructor(
    private val getMyClassesUseCase: GetMyClassesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyClassesUiState())
    val uiState: StateFlow<MyClassesUiState> = _uiState.asStateFlow()

    init {
        loadClasses()
    }

    private fun loadClasses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getMyClassesUseCase()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, classes = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun refresh() = loadClasses()
}
