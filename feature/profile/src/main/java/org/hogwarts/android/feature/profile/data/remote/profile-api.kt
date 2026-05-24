package org.hogwarts.android.feature.profile.data.remote

import org.hogwarts.android.feature.profile.data.remote.dto.ActivityFeedDto
import org.hogwarts.android.feature.profile.data.remote.dto.ContributionGraphDto
import org.hogwarts.android.feature.profile.data.remote.dto.PinnedItemsDto
import org.hogwarts.android.feature.profile.data.remote.dto.ProfileDto
import org.hogwarts.android.feature.profile.data.remote.dto.UpdateProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileApi {

    @GET("api/mobile/profile")
    suspend fun getProfile(): Response<ProfileDto>

    @GET("api/mobile/profile/{userId}")
    suspend fun getProfileById(@Path("userId") userId: String): Response<ProfileDto>

    @PUT("api/mobile/profile")
    suspend fun updateProfile(@Body profile: UpdateProfileDto): Response<ProfileDto>

    @GET("api/mobile/profile/contributions")
    suspend fun getContributions(
        @Query("userId") userId: String? = null,
        @Query("year") year: Int? = null
    ): Response<ContributionGraphDto>

    @GET("api/mobile/profile/activity")
    suspend fun getActivity(
        @Query("userId") userId: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ActivityFeedDto>

    @GET("api/mobile/profile/pinned")
    suspend fun getPinned(@Query("userId") userId: String? = null): Response<PinnedItemsDto>
}
