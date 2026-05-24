package org.hogwarts.android.feature.announcements.data.remote

import org.hogwarts.android.feature.announcements.data.remote.dto.AnnouncementDto
import org.hogwarts.android.feature.announcements.data.remote.dto.AnnouncementListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnnouncementsApi {

    @GET("api/mobile/announcements")
    suspend fun getAnnouncements(
        @Query("type") type: String? = null
    ): Response<AnnouncementListResponse>

    @GET("api/mobile/announcements/{id}")
    suspend fun getAnnouncement(
        @Path("id") id: String
    ): Response<AnnouncementDto>

    @GET("api/mobile/events")
    suspend fun getEvents(
        @Query("from") fromDate: String? = null,
        @Query("to") toDate: String? = null
    ): Response<AnnouncementListResponse>
}
