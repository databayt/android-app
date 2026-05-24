package org.hogwarts.android.feature.quizgame.domain.model

import kotlinx.serialization.Serializable

enum class QuizMode {
    PRACTICE, TIMED, CHALLENGE, TOURNAMENT
}

enum class QuestionType {
    MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
}

enum class DifficultyLevel {
    EASY, MEDIUM, HARD
}

data class QuizQuestion(
    val id: String,
    val text: String,
    val type: QuestionType,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String?,
    val subject: String,
    val topic: String,
    val difficulty: DifficultyLevel,
    val points: Int
)

data class QuizSession(
    val id: String,
    val mode: QuizMode,
    val questions: List<QuizQuestion>,
    val currentIndex: Int,
    val score: Int,
    val streakCount: Int,
    val startedAt: Long,
    val timeLimitSeconds: Int?,
    val answers: Map<String, String>
)

data class QuizResult(
    val sessionId: String,
    val mode: QuizMode,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Int,
    val timeTakenSeconds: Int,
    val accuracy: Float,
    val xpEarned: Int,
    val streakBonus: Int,
    val newAchievements: List<QuizAchievement>
)

data class QuizAchievement(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val unlockedAt: Long?,
    val progress: Float,
    val category: String
)

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val userName: String,
    val avatarUrl: String?,
    val score: Int,
    val gamesPlayed: Int,
    val winRate: Float
)

data class Tournament(
    val id: String,
    val name: String,
    val subject: String,
    val startDate: Long,
    val endDate: Long,
    val currentRound: Int,
    val totalRounds: Int,
    val participants: Int,
    val status: TournamentStatus,
    val bracket: List<TournamentMatch>
)

enum class TournamentStatus {
    UPCOMING, IN_PROGRESS, COMPLETED
}

data class TournamentMatch(
    val id: String,
    val round: Int,
    val player1Id: String?,
    val player1Name: String?,
    val player2Id: String?,
    val player2Name: String?,
    val winnerId: String?,
    val player1Score: Int,
    val player2Score: Int
)

data class DailyChallenge(
    val id: String,
    val date: String,
    val subject: String,
    val questionCount: Int,
    val difficulty: DifficultyLevel,
    val completed: Boolean,
    val reward: Int
)
