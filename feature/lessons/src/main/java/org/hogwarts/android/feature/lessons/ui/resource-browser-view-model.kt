package org.hogwarts.android.feature.lessons.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.lessons.domain.model.LessonResource
import org.hogwarts.android.feature.lessons.domain.model.ResourceType
import org.hogwarts.android.feature.lessons.domain.usecase.GetResourcesUseCase
import javax.inject.Inject

data class ResourceBrowserUiState(
    val allResources: List<LessonResource> = emptyList(),
    val filteredResources: List<LessonResource> = emptyList(),
    val selectedType: ResourceType? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ResourceBrowserViewModel @Inject constructor(
    private val getResourcesUseCase: GetResourcesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResourceBrowserUiState())
    val uiState: StateFlow<ResourceBrowserUiState> = _uiState.asStateFlow()

    init {
        loadResources()
    }

    private fun loadResources() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getResourcesUseCase()) {
                is Result.Success -> {
                    val resources = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allResources = resources,
                            filteredResources = filterResources(resources, it.selectedType)
                        )
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun selectType(type: ResourceType?) {
        _uiState.update { state ->
            state.copy(
                selectedType = type,
                filteredResources = filterResources(state.allResources, type)
            )
        }
    }

    fun refresh() = loadResources()

    private fun filterResources(
        resources: List<LessonResource>,
        type: ResourceType?
    ): List<LessonResource> {
        return if (type == null) resources else resources.filter { it.type == type }
    }
}
