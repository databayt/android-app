package org.hogwarts.android.feature.announcements.data.remote

import org.hogwarts.android.feature.announcements.data.remote.dto.AnnouncementDto
import org.hogwarts.android.feature.announcements.data.remote.dto.AnnouncementListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnnouncementsApi {

    /** The list the web's `/announcements` shows this caller's role. */
    @GET("api/mobile/announcements")
    suspend fun getAnnouncements(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("title") title: String? = null,
        @Query("lang") lang: String? = null,
    ): Response<AnnouncementListResponse>

    /** The reading page; 404 when the caller is not its audience. Marks it read. */
    @GET("api/mobile/announcements/{id}")
    suspend fun getAnnouncement(
        @Path("id") id: String,
        @Query("lang") lang: String? = null,
    ): Response<AnnouncementDto>
}
