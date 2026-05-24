package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.ChapterEntity
import org.hogwarts.android.core.database.entity.CourseEntity
import org.hogwarts.android.core.database.entity.EnrollmentEntity
import org.hogwarts.android.core.database.entity.LessonEntity
import org.hogwarts.android.core.database.entity.LessonProgressEntity

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE schoolId = :schoolId ORDER BY title ASC")
    fun getCourses(schoolId: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id AND schoolId = :schoolId")
    suspend fun getCourseById(id: String, schoolId: String): CourseEntity?

    @Query("SELECT * FROM chapters WHERE courseId = :courseId ORDER BY orderIndex ASC")
    suspend fun getChapters(courseId: String): List<ChapterEntity>

    @Query("SELECT * FROM lessons WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    suspend fun getLessons(chapterId: String): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonById(id: String): LessonEntity?

    @Query("SELECT * FROM enrollments WHERE userId = :userId")
    fun getEnrollments(userId: String): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments WHERE courseId = :courseId AND userId = :userId")
    suspend fun getEnrollment(courseId: String, userId: String): EnrollmentEntity?

    @Query("SELECT * FROM lesson_progress WHERE enrollmentId = :enrollmentId")
    suspend fun getLessonProgress(enrollmentId: String): List<LessonProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChapters(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEnrollments(enrollments: List<EnrollmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessonProgress(progress: List<LessonProgressEntity>)
}
