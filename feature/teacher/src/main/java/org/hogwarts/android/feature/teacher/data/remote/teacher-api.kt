package org.hogwarts.android.feature.teacher.data.remote

import org.hogwarts.android.feature.teacher.data.remote.dto.AssessmentDto
import org.hogwarts.android.feature.teacher.data.remote.dto.BatchAttendanceRequestDto
import org.hogwarts.android.feature.teacher.data.remote.dto.BatchGradeRequestDto
import org.hogwarts.android.feature.teacher.data.remote.dto.ClassStudentDto
import org.hogwarts.android.feature.teacher.data.remote.dto.ScheduleSlotDto
import org.hogwarts.android.feature.teacher.data.remote.dto.TeacherClassDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for teacher endpoints.
 *
 * schoolId is extracted from the JWT token by the web API.
 * X-School-Id header is added by TenantInterceptor (ignored by web but harmless).
 */
interface TeacherApi {

    @GET("api/mobile/teacher/classes")
    suspend fun getClasses(): List<TeacherClassDto>

    @GET("api/mobile/teacher/classes/{classId}/students")
    suspend fun getClassStudents(
        @Path("classId") classId: String
    ): List<ClassStudentDto>

    @GET("api/mobile/teacher/schedule")
    suspend fun getSchedule(): List<ScheduleSlotDto>

    // NOTE: Web endpoint being added — will 404 until available
    @GET("api/mobile/teacher/classes/{classId}/assessments")
    suspend fun getAssessments(
        @Path("classId") classId: String
    ): List<AssessmentDto>

    // NOTE: Web endpoint being added — will 404 until available
    @POST("api/mobile/teacher/classes/{classId}/attendance")
    suspend fun submitBatchAttendance(
        @Path("classId") classId: String,
        @Body request: BatchAttendanceRequestDto
    )

    // NOTE: Web endpoint being added — will 404 until available
    @POST("api/mobile/teacher/classes/{classId}/grades")
    suspend fun submitBatchGrades(
        @Path("classId") classId: String,
        @Body request: BatchGradeRequestDto
    )
}
