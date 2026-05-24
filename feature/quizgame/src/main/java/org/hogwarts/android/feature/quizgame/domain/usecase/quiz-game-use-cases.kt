package org.hogwarts.android.feature.quizgame.domain.usecase

import org.hogwarts.android.feature.quizgame.data.repository.QuizGameRepository
import org.hogwarts.android.feature.quizgame.domain.model.*
import javax.inject.Inject

class StartQuizSessionUseCase @Inject constructor(
    private val repository: QuizGameRepository
) {
    suspend operator fun invoke(
        mode: QuizMode,
        subject: String? = null,
        difficulty: DifficultyLevel? = null,
        questionCount: Int = 10
    ): Result<QuizSession> = repository.startSession(mode, subject, difficulty, questionCount)
}

class SubmitQuizSessionUseCase @Inject constructor(
    private val repository: QuizGameRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        answers: Map<String, String>,
        timeTakenSeconds: Int
    ): Result<QuizResult> = repository.submitSession(sessionId, answers, timeTakenSeconds)
}

class GetLeaderboardUseCase @Inject constructor(
    private val repository: QuizGameRepository
) {
    suspend operator fun invoke(
        scope: String = "school",
        period: String = "weekly"
    ): Result<List<LeaderboardEntry>> = repository.getLeaderboard(scope, period)
}

class GetAchievementsUseCase @Inject constructor(
    private val repository: QuizGameRepository
) {
    suspend operator fun invoke(): Result<List<QuizAchievement>> = repository.getAchievements()
}

class GetDailyChallengeUseCase @Inject constructor(
    private val repository: QuizGameRepository
) {
    suspend operator fun invoke(): Result<DailyChallenge> = repository.getDailyChallenge()
}

class GetTournamentsUseCase @Inject constructor(
    private val repository: QuizGameRepository
) {
    suspend operator fun invoke(): Result<List<Tournament>> = repository.getTournaments()
}

class JoinTournamentUseCase @Inject constructor(
    private val repository: QuizGameRepository
) {
    suspend operator fun invoke(tournamentId: String): Result<Tournament> =
        repository.joinTournament(tournamentId)
}

class SendChallengeUseCase @Inject constructor(
    private val repository: QuizGameRepository
) {
    suspend operator fun invoke(opponentId: String, subject: String? = null): Result<QuizSession> =
        repository.sendChallenge(opponentId, subject)
}
