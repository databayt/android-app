package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.AcademicYearEntity
import org.hogwarts.android.core.database.entity.SchoolSettingsEntity

@Dao
interface SchoolSettingsDao {
    @Query("SELECT * FROM school_settings WHERE schoolId = :schoolId LIMIT 1")
    fun getSchoolSettings(schoolId: String): Flow<SchoolSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchoolSettings(settings: SchoolSettingsEntity)

    @Query("SELECT * FROM academic_years WHERE schoolId = :schoolId ORDER BY startDate DESC")
    fun getAcademicYears(schoolId: String): Flow<List<AcademicYearEntity>>

    @Query("SELECT * FROM academic_years WHERE isCurrent = 1 AND schoolId = :schoolId LIMIT 1")
    suspend fun getCurrentAcademicYear(schoolId: String): AcademicYearEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAcademicYears(years: List<AcademicYearEntity>)
}
