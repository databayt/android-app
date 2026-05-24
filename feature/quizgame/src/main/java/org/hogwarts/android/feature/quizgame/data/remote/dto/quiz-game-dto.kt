package org.hogwarts.android.feature.quizgame.data.remote.dto

import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.quizgame.domain.model.*

@Serializable
data class QuizQuestionDto(
    val id: String,
    val text: String,
    val type: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String? = null,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val points: Int
) {
    fun toDomain() = QuizQuestion(
        id = id, text = text,
        type = QuestionType.valueOf(type.uppercase()),
        options = options, correctAnswer = correctAnswer,
        explanation = explanation, subject = subject, topic = topic,
        difficulty = DifficultyLevel.valueOf(difficulty.uppercase()),
        points = points
    )
}

@Serializable
data class QuizSessionDto(
    val id: String,
    val mode: String,
    val questions: List<QuizQuestionDto>,
    val timeLimitSeconds: Int? = null
) {
    fun toDomain() = QuizSession(
        id = id, mode = QuizMode.valueOf(mode.uppercase()),
        questions = questions.map { it.toDomain() },
        currentIndex = 0, score = 0, streakCount = 0,
        startedAt = System.currentTimeMillis(),
        timeLimitSeconds = timeLimitSeconds, answers = emptyMap()
    )
}

@Serializable
data class QuizResultDto(
    val sessionId: String,
    val mode: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Int,
    val timeTakenSeconds: Int,
    val accuracy: Float,
    val xpEarned: Int,
    val streakBonus: Int,
    val newAchievements: List<QuizAchievementDto>
) {
    fun toDomain() = QuizResult(
        sessionId = sessionId, mode = QuizMode.valueOf(mode.uppercase()),
        totalQuestions = totalQuestions, correctAnswers = correctAnswers,
        score = score, timeTakenSeconds = timeTakenSeconds,
        accuracy = accuracy, xpEarned = xpEarned, streakBonus = streakBonus,
        newAchievements = newAchievements.map { it.toDomain() }
    )
}

@Serializable
data class QuizAchievementDto(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val unlockedAt: Long? = null,
    val progress: Float,
    val category: String
) {
    fun toDomain() = QuizAchievement(
        id = id, name = name, description = description,
        iconName = iconName, unlockedAt = unlockedAt,
        progress = progress, category = category
    )
}

@Serializable
data class LeaderboardEntryDto(
    val rank: Int,
    val userId: String,
    val userName: String,
    val avatarUrl: String? = null,
    val score: Int,
    val gamesPlayed: Int,
    val winRate: Float
) {
    fun toDomain() = LeaderboardEntry(
        rank = rank, userId = userId, userName = userName,
        avatarUrl = avatarUrl, score = score,
        gamesPlayed = gamesPlayed, winRate = winRate
    )
}

@Serializable
data class TournamentDto(
    val id: String,
    val name: String,
    val subject: String,
    val startDate: Long,
    val endDate: Long,
    val currentRound: Int,
    val totalRounds: Int,
    val participants: Int,
    val status: String,
    val bracket: List<TournamentMatchDto> = emptyList()
) {
    fun toDomain() = Tournament(
        id = id, name = name, subject = subject,
        startDate = startDate, endDate = endDate,
        currentRound = currentRound, totalRounds = totalRounds,
        participants = participants,
        status = TournamentStatus.valueOf(status.uppercase()),
        bracket = bracket.map { it.toDomain() }
    )
}

@Serializable
data class TournamentMatchDto(
    val id: String,
    val round: Int,
    val player1Id: String? = null,
    val player1Name: String? = null,
    val player2Id: String? = null,
    val player2Name: String? = null,
    val winnerId: String? = null,
    val player1Score: Int = 0,
    val player2Score: Int = 0
) {
    fun toDomain() = TournamentMatch(
        id = id, round = round,
        player1Id = player1Id, player1Name = player1Name,
        player2Id = player2Id, player2Name = player2Name,
        winnerId = winnerId, player1Score = player1Score,
        player2Score = player2Score
    )
}

@Serializable
data class DailyChallengeDto(
    val id: String,
    val date: String,
    val subject: String,
    val questionCount: Int,
    val difficulty: String,
    val completed: Boolean,
    val reward: Int
) {
    fun toDomain() = DailyChallenge(
        id = id, date = date, subject = subject,
        questionCount = questionCount,
        difficulty = DifficultyLevel.valueOf(difficulty.uppercase()),
        completed = completed, reward = reward
    )
}

@Serializable
data class StartQuizSessionRequest(
    val mode: String,
    val subject: String? = null,
    val difficulty: String? = null,
    val questionCount: Int = 10
)

@Serializable
data class SubmitQuizRequest(
    val answers: Map<String, String>,
    val timeTakenSeconds: Int
)

@Serializable
data class SendChallengeRequest(
    val schoolId: String,
    val opponentId: String,
    val subject: String? = null
)
