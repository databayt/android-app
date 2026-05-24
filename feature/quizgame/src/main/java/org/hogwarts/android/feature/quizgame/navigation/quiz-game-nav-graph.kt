package org.hogwarts.android.feature.quizgame.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.quizgame.ui.AchievementsScreen
import org.hogwarts.android.feature.quizgame.ui.GameHubScreen
import org.hogwarts.android.feature.quizgame.ui.LeaderboardScreen
import org.hogwarts.android.feature.quizgame.ui.PracticeModeScreen
import org.hogwarts.android.feature.quizgame.ui.QuizSessionScreen
import org.hogwarts.android.feature.quizgame.ui.TimedChallengeScreen
import org.hogwarts.android.feature.quizgame.ui.TournamentScreen

@Serializable data object QuizGameHub
@Serializable data object QuizGamePractice
@Serializable data object QuizGameTimed
@Serializable data object QuizGameLeaderboard
@Serializable data object QuizGameAchievements
@Serializable data object QuizGameTournament
@Serializable data class QuizGameSession(val sessionId: String)

fun NavGraphBuilder.gameHubScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPractice: () -> Unit,
    onNavigateToTimed: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToTournament: () -> Unit
) {
    composable<QuizGameHub> {
        GameHubScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToPractice = onNavigateToPractice,
            onNavigateToTimed = onNavigateToTimed,
            onNavigateToLeaderboard = onNavigateToLeaderboard,
            onNavigateToAchievements = onNavigateToAchievements,
            onNavigateToTournament = onNavigateToTournament
        )
    }
}

fun NavGraphBuilder.practiceModeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSession: (String) -> Unit
) {
    composable<QuizGamePractice> {
        PracticeModeScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSession = onNavigateToSession
        )
    }
}

fun NavGraphBuilder.timedChallengeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSession: (String) -> Unit
) {
    composable<QuizGameTimed> {
        TimedChallengeScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSession = onNavigateToSession
        )
    }
}

fun NavGraphBuilder.quizLeaderboardScreen(
    onNavigateBack: () -> Unit
) {
    composable<QuizGameLeaderboard> {
        LeaderboardScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.quizAchievementsScreen(
    onNavigateBack: () -> Unit
) {
    composable<QuizGameAchievements> {
        AchievementsScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.tournamentScreen(
    onNavigateBack: () -> Unit
) {
    composable<QuizGameTournament> {
        TournamentScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.quizSessionScreen(
    onNavigateBack: () -> Unit
) {
    composable<QuizGameSession> {
        QuizSessionScreen(onNavigateBack = onNavigateBack)
    }
}
