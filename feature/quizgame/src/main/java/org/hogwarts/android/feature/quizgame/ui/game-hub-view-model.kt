package org.hogwarts.android.feature.quizgame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.quizgame.domain.model.DailyChallenge
import org.hogwarts.android.feature.quizgame.domain.model.LeaderboardEntry
import org.hogwarts.android.feature.quizgame.domain.model.QuizAchievement
import org.hogwarts.android.feature.quizgame.domain.usecase.GetAchievementsUseCase
import org.hogwarts.android.feature.quizgame.domain.usecase.GetDailyChallengeUseCase
import org.hogwarts.android.feature.quizgame.domain.usecase.GetLeaderboardUseCase
import timber.log.Timber
import javax.inject.Inject

data class GameHubUiState(
    val isLoading: Boolean = true,
    val dailyChallenge: DailyChallenge? = null,
    val achievements: List<QuizAchievement> = emptyList(),
    val topLeaderboard: List<LeaderboardEntry> = emptyList(),
    val unlockedAchievementsCount: Int = 0,
    val totalAchievementsCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class GameHubViewModel @Inject constructor(
    private val getDailyChallengeUseCase: GetDailyChallengeUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameHubUiState())
    val uiState: StateFlow<GameHubUiState> = _uiState.asStateFlow()

    init {
        loadHubData()
    }

    fun loadHubData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val dailyChallengeResult = getDailyChallengeUseCase()
            val achievementsResult = getAchievementsUseCase()
            val leaderboardResult = getLeaderboardUseCase(scope = "school", period = "weekly")

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    dailyChallenge = dailyChallengeResult.getOrNull(),
                    achievements = achievementsResult.getOrDefault(emptyList()),
                    unlockedAchievementsCount = achievementsResult.getOrDefault(emptyList()).count { it.unlockedAt != null },
                    totalAchievementsCount = achievementsResult.getOrDefault(emptyList()).size,
                    topLeaderboard = leaderboardResult.getOrDefault(emptyList()).take(5),
                    error = when {
                        dailyChallengeResult.isFailure && achievementsResult.isFailure && leaderboardResult.isFailure ->
                            "Failed to load quiz data. Please check your connection."
                        else -> null
                    }
                )
            }

            dailyChallengeResult.onFailure { Timber.e(it, "Failed to load daily challenge") }
            achievementsResult.onFailure { Timber.e(it, "Failed to load achievements") }
            leaderboardResult.onFailure { Timber.e(it, "Failed to load leaderboard") }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
