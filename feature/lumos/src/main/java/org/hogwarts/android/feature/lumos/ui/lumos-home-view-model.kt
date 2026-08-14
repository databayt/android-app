package org.hogwarts.android.feature.lumos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.lumos.domain.model.Course
import org.hogwarts.android.feature.lumos.domain.usecase.GetContinueWatchingUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.GetCoursesUseCase
import javax.inject.Inject

data class LumosHomeUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = true,
    val continueWatching: List<Course> = emptyList(),
    val featuredCourses: List<Course> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class LumosHomeViewModel @Inject constructor(
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getContinueWatchingUseCase: GetContinueWatchingUseCase,
    private val tenantContext: TenantContext
) : ViewModel() {

    private val _uiState = MutableStateFlow(LumosHomeUiState(isLoading = true))
    val uiState: StateFlow<LumosHomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun refresh() {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val coursesResult = getCoursesUseCase()
            val continueResult = getContinueWatchingUseCase()

            val featured = if (coursesResult is Result.Success) coursesResult.data else emptyList()
            val continueWatching = if (continueResult is Result.Success) continueResult.data else emptyList()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    featuredCourses = featured,
                    continueWatching = continueWatching
                )
            }
        }
    }
}
