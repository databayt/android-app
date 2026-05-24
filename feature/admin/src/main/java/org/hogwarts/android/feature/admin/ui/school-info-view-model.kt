package org.hogwarts.android.feature.admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.admin.domain.model.SchoolInfo
import org.hogwarts.android.feature.admin.domain.usecase.GetSchoolInfoUseCase
import org.hogwarts.android.feature.admin.domain.usecase.UpdateSchoolInfoUseCase
import javax.inject.Inject

data class SchoolInfoUiState(
    val schoolInfo: SchoolInfo? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class SchoolInfoViewModel @Inject constructor(
    private val getSchoolInfoUseCase: GetSchoolInfoUseCase,
    private val updateSchoolInfoUseCase: UpdateSchoolInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchoolInfoUiState())
    val uiState: StateFlow<SchoolInfoUiState> = _uiState.asStateFlow()

    init {
        loadSchoolInfo()
    }

    private fun loadSchoolInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getSchoolInfoUseCase()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, schoolInfo = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun toggleEdit() {
        _uiState.update { it.copy(isEditing = !it.isEditing, saveSuccess = false) }
    }

    fun saveSchoolInfo(info: SchoolInfo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = updateSchoolInfoUseCase(info)) {
                is Result.Success -> _uiState.update {
                    it.copy(isSaving = false, isEditing = false, schoolInfo = result.data, saveSuccess = true)
                }
                is Result.Error -> _uiState.update { it.copy(isSaving = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun refresh() = loadSchoolInfo()
}
