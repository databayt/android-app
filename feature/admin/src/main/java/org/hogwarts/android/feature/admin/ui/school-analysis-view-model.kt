package org.hogwarts.android.feature.admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.admin.domain.model.SchoolAnalysis
import org.hogwarts.android.feature.admin.domain.usecase.GetSchoolAnalysisUseCase
import retrofit2.HttpException

enum class AnalysisError { Offline, Forbidden, Failed }

data class SchoolAnalysisUiState(
    val analysis: SchoolAnalysis? = null,
    val isLoading: Boolean = true,
    val error: AnalysisError? = null,
)

/** 403 is the server saying "admins only"; IO is a phone without a connection. */
internal fun classifyAnalysisError(e: Throwable): AnalysisError = when {
    e is HttpException && e.code() == 403 -> AnalysisError.Forbidden
    e is IOException -> AnalysisError.Offline
    else -> AnalysisError.Failed
}

@HiltViewModel
class SchoolAnalysisViewModel @Inject constructor(
    private val getAnalysis: GetSchoolAnalysisUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchoolAnalysisUiState())
    val uiState: StateFlow<SchoolAnalysisUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getAnalysis()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, analysis = result.data) }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, analysis = null, error = classifyAnalysisError(result.exception))
                }
                is Result.Loading -> Unit
            }
        }
    }
}
