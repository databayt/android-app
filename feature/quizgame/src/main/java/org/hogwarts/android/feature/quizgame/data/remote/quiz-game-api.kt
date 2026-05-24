package org.hogwarts.android.feature.quizgame.data.remote

import org.hogwarts.android.feature.quizgame.data.remote.dto.*
import retrofit2.http.*

/**
 * schoolId is extracted from the JWT token by the web API.
 * X-School-Id header is added by TenantInterceptor (ignored by web but harmless).
 */
interface QuizGameApi {
    @GET("api/mobile/quiz/questions")
    suspend fun getQuestions(
        @Query("subject") subject: String? = null,
        @Query("topic") topic: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("count") count: Int = 10
    ): List<QuizQuestionDto>

    @POST("api/mobile/quiz/sessions")
    suspend fun startSession(
        @Body request: StartQuizSessionRequest
    ): QuizSessionDto

    @POST("api/mobile/quiz/sessions/{sessionId}/submit")
    suspend fun submitSession(
        @Path("sessionId") sessionId: String,
        @Body request: SubmitQuizRequest
    ): QuizResultDto

    @GET("api/mobile/quiz/leaderboard")
    suspend fun getLeaderboard(
        @Query("scope") scope: String = "school",
        @Query("period") period: String = "weekly"
    ): List<LeaderboardEntryDto>

    @GET("api/mobile/quiz/achievements")
    suspend fun getAchievements(): List<QuizAchievementDto>

    @GET("api/mobile/quiz/daily-challenge")
    suspend fun getDailyChallenge(): DailyChallengeDto

    @GET("api/mobile/quiz/tournaments")
    suspend fun getTournaments(): List<TournamentDto>

    @POST("api/mobile/quiz/tournaments/{tournamentId}/join")
    suspend fun joinTournament(
        @Path("tournamentId") tournamentId: String
    ): TournamentDto

    @POST("api/mobile/quiz/challenge")
    suspend fun sendChallenge(
        @Body request: SendChallengeRequest
    ): QuizSessionDto
}
