package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.SubjectEntity

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE schoolId = :schoolId ORDER BY name ASC")
    fun getSubjects(schoolId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id AND schoolId = :schoolId")
    suspend fun getSubjectById(id: String, schoolId: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubjects(subjects: List<SubjectEntity>)
}
