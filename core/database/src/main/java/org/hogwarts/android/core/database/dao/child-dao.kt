package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.ChildEntity

/**
 * Room DAO for guardian children.
 *
 * All queries filter by schoolId for multi-tenant isolation.
 */
@Dao
interface ChildDao {

    @Query("SELECT * FROM children WHERE guardianId = :guardianId AND schoolId = :schoolId ORDER BY givenName ASC")
    fun getChildrenByGuardian(guardianId: String, schoolId: String): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :id AND schoolId = :schoolId")
    suspend fun getChildById(id: String, schoolId: String): ChildEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChildren(children: List<ChildEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(child: ChildEntity)

    @Query("DELETE FROM children WHERE guardianId = :guardianId AND schoolId = :schoolId")
    suspend fun deleteByGuardian(guardianId: String, schoolId: String)

    @Query("DELETE FROM children WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    @Query("DELETE FROM children")
    suspend fun clearAll()
}
