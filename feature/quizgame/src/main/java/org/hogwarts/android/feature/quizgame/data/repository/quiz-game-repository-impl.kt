package org.hogwarts.android.feature.quizgame.data.repository

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.quizgame.data.remote.QuizGameApi
import org.hogwarts.android.feature.quizgame.data.remote.dto.SendChallengeRequest
import org.hogwarts.android.feature.quizgame.data.remote.dto.StartQuizSessionRequest
import org.hogwarts.android.feature.quizgame.data.remote.dto.SubmitQuizRequest
import org.hogwarts.android.feature.quizgame.domain.model.*
import javax.inject.Inject

class QuizGameRepositoryImpl @Inject constructor(
    private val api: QuizGameApi,
    private val tenantContext: TenantContext
) : QuizGameRepository {

    override suspend fun getQuestions(subject: String?, topic: String?, difficulty: DifficultyLevel?, count: Int): Result<List<QuizQuestion>> =
        runCatching {
            api.getQuestions(subject, topic, difficulty?.name?.lowercase(), count).map { it.toDomain() }
        }

    override suspend fun startSession(mode: QuizMode, subject: String?, difficulty: DifficultyLevel?, questionCount: Int): Result<QuizSession> =
        runCatching {
            api.startSession(StartQuizSessionRequest(mode = mode.name.lowercase(), subject = subject, difficulty = difficulty?.name?.lowercase(), questionCount = questionCount)).toDomain()
        }

    override suspend fun submitSession(sessionId: String, answers: Map<String, String>, timeTakenSeconds: Int): Result<QuizResult> =
        runCatching {
            api.submitSession(sessionId, SubmitQuizRequest(answers, timeTakenSeconds)).toDomain()
        }

    override suspend fun getLeaderboard(scope: String, period: String): Result<List<LeaderboardEntry>> =
        runCatching {
            api.getLeaderboard(scope, period).map { it.toDomain() }
        }

    override suspend fun getAchievements(): Result<List<QuizAchievement>> =
        runCatching {
            api.getAchievements().map { it.toDomain() }
        }

    override suspend fun getDailyChallenge(): Result<DailyChallenge> =
        runCatching {
            api.getDailyChallenge().toDomain()
        }

    override suspend fun getTournaments(): Result<List<Tournament>> =
        runCatching {
            api.getTournaments().map { it.toDomain() }
        }

    override suspend fun joinTournament(tournamentId: String): Result<Tournament> =
        runCatching {
            api.joinTournament(tournamentId).toDomain()
        }

    override suspend fun sendChallenge(opponentId: String, subject: String?): Result<QuizSession> =
        runCatching {
            val schoolId = tenantContext.requireSchoolId()
            api.sendChallenge(SendChallengeRequest(schoolId = schoolId, opponentId = opponentId, subject = subject)).toDomain()
        }
}
