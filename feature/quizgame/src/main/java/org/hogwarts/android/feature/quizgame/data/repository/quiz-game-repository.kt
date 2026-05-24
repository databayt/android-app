package org.hogwarts.android.feature.quizgame.data.repository

import org.hogwarts.android.feature.quizgame.domain.model.*

interface QuizGameRepository {
    suspend fun getQuestions(subject: String?, topic: String?, difficulty: DifficultyLevel?, count: Int): Result<List<QuizQuestion>>
    suspend fun startSession(mode: QuizMode, subject: String?, difficulty: DifficultyLevel?, questionCount: Int): Result<QuizSession>
    suspend fun submitSession(sessionId: String, answers: Map<String, String>, timeTakenSeconds: Int): Result<QuizResult>
    suspend fun getLeaderboard(scope: String, period: String): Result<List<LeaderboardEntry>>
    suspend fun getAchievements(): Result<List<QuizAchievement>>
    suspend fun getDailyChallenge(): Result<DailyChallenge>
    suspend fun getTournaments(): Result<List<Tournament>>
    suspend fun joinTournament(tournamentId: String): Result<Tournament>
    suspend fun sendChallenge(opponentId: String, subject: String?): Result<QuizSession>
}
