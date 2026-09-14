package org.hogwarts.android.feature.timetable.data.remote

import org.hogwarts.android.feature.timetable.data.remote.dto.ChildListResponse
import org.hogwarts.android.feature.timetable.data.remote.dto.DashboardDayResponse
import org.hogwarts.android.feature.timetable.data.remote.dto.SlotListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/** The mobile routes behind `/timetable`. Read-only. */
interface TimetableApi {

    /** A student's (section + enrolled classes) or teacher's week, with today's live classes. */
    @GET("api/mobile/timetable/{userId}")
    suspend fun getWeek(@Path("userId") userId: String): Response<SlotListResponse>

    /** The web's `getTodaySchedule` rides on the dashboard: periods, breaks and the day's closure. */
    @GET("api/mobile/dashboard")
    suspend fun getDashboardDay(): Response<DashboardDayResponse>

    @GET("api/mobile/guardian/children")
    suspend fun getChildren(): Response<ChildListResponse>

    @GET("api/mobile/guardian/children/{childId}/timetable")
    suspend fun getChildWeek(@Path("childId") childId: String): Response<SlotListResponse>
}
