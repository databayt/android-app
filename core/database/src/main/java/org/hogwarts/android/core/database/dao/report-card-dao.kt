package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.ReportCardEntity
import org.hogwarts.android.core.database.entity.SubjectReportEntity

@Dao
interface ReportCardDao {
    @Query("SELECT * FROM report_cards WHERE schoolId = :schoolId ORDER BY academicYear DESC, termName DESC")
    fun getReportCards(schoolId: String): Flow<List<ReportCardEntity>>

    @Query("SELECT * FROM report_cards WHERE studentId = :studentId AND schoolId = :schoolId ORDER BY academicYear DESC")
    fun getReportCardsByStudent(studentId: String, schoolId: String): Flow<List<ReportCardEntity>>

    @Query("SELECT * FROM report_cards WHERE id = :id AND schoolId = :schoolId")
    suspend fun getReportCardById(id: String, schoolId: String): ReportCardEntity?

    @Query("SELECT * FROM subject_reports WHERE reportCardId = :reportCardId")
    suspend fun getSubjectReports(reportCardId: String): List<SubjectReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReportCards(reportCards: List<ReportCardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubjectReports(reports: List<SubjectReportEntity>)
}
