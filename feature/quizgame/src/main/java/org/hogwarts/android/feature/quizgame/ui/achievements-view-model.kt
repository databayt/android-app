package org.hogwarts.android.feature.quizgame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.quizgame.domain.model.QuizAchievement
import org.hogwarts.android.feature.quizgame.domain.usecase.GetAchievementsUseCase
import timber.log.Timber
import javax.inject.Inject

data class AchievementsUiState(
    val isLoading: Boolean = true,
    val achievements: List<QuizAchievement> = emptyList(),
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val getAchievementsUseCase: GetAchievementsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    fun loadAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getAchievementsUseCase()

            result.onSuccess { achievements ->
                val categories = achievements.map { it.category }.distinct()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        achievements = achievements,
                        categories = categories
                    )
                }
            }.onFailure { error ->
                Timber.e(error, "Failed to load achievements")
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load achievements.")
                }
            }
        }
    }

    fun selectCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun getFilteredAchievements(): List<QuizAchievement> {
        val state = _uiState.value
        return if (state.selectedCategory == null) {
            state.achievements
        } else {
            state.achievements.filter { it.category == state.selectedCategory }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
