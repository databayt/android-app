package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.AttendanceBadgeEntity
import org.hogwarts.android.core.database.entity.AttendanceInterventionEntity
import org.hogwarts.android.core.database.entity.HallPassEntity

@Dao
interface AttendanceBadgeDao {
    @Query("SELECT * FROM attendance_badges WHERE userId = :userId AND schoolId = :schoolId ORDER BY earnedAt DESC")
    fun getBadges(userId: String, schoolId: String): Flow<List<AttendanceBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBadges(badges: List<AttendanceBadgeEntity>)

    @Query("SELECT * FROM hall_passes WHERE schoolId = :schoolId ORDER BY requestedAt DESC")
    fun getHallPasses(schoolId: String): Flow<List<HallPassEntity>>

    @Query("SELECT * FROM hall_passes WHERE studentId = :studentId AND schoolId = :schoolId ORDER BY requestedAt DESC")
    fun getHallPassesByStudent(studentId: String, schoolId: String): Flow<List<HallPassEntity>>

    @Query("SELECT * FROM hall_passes WHERE teacherId = :teacherId AND schoolId = :schoolId ORDER BY requestedAt DESC")
    fun getHallPassesByTeacher(teacherId: String, schoolId: String): Flow<List<HallPassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHallPasses(passes: List<HallPassEntity>)

    @Query("SELECT * FROM attendance_interventions WHERE schoolId = :schoolId ORDER BY createdAt DESC")
    fun getInterventions(schoolId: String): Flow<List<AttendanceInterventionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInterventions(interventions: List<AttendanceInterventionEntity>)
}
