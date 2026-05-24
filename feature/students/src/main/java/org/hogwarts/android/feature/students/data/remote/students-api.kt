package org.hogwarts.android.feature.students.data.remote

import org.hogwarts.android.feature.students.data.remote.dto.CreateStudentDto
import org.hogwarts.android.feature.students.data.remote.dto.StudentDto
import org.hogwarts.android.feature.students.data.remote.dto.StudentListResponse
import org.hogwarts.android.feature.students.data.remote.dto.UpdateStudentDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API for student endpoints.
 */
interface StudentsApi {

    @GET("api/mobile/students")
    suspend fun getStudents(
        @Query("section_id") classId: String? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50
    ): Response<StudentListResponse>

    @GET("api/mobile/students/{studentId}")
    suspend fun getStudent(
        @Path("studentId") studentId: String
    ): Response<StudentDto>

    @POST("api/mobile/students")
    suspend fun createStudent(
        @Body student: CreateStudentDto
    ): Response<StudentDto>

    @PUT("api/mobile/students/{studentId}")
    suspend fun updateStudent(
        @Path("studentId") studentId: String,
        @Body student: UpdateStudentDto
    ): Response<StudentDto>
}
