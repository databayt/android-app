package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.UserEntity

/**
 * Data Access Object for User entity.
 *
 * IMPORTANT: All queries MUST include schoolId filter for multi-tenant isolation.
 */
@Dao
interface UserDao {

    /**
     * Get user by ID within a school
     * ALWAYS filter by schoolId for tenant isolation
     */
    @Query("SELECT * FROM users WHERE id = :userId AND schoolId = :schoolId")
    suspend fun getUserById(userId: String, schoolId: String): UserEntity?

    /**
     * Get user by ID (observable)
     */
    @Query("SELECT * FROM users WHERE id = :userId AND schoolId = :schoolId")
    fun observeUserById(userId: String, schoolId: String): Flow<UserEntity?>

    /**
     * Get user by email within a school
     */
    @Query("SELECT * FROM users WHERE email = :email AND schoolId = :schoolId")
    suspend fun getUserByEmail(email: String, schoolId: String): UserEntity?

    /**
     * Get all users for a school
     */
    @Query("SELECT * FROM users WHERE schoolId = :schoolId ORDER BY familyName, givenName")
    fun getUsersBySchool(schoolId: String): Flow<List<UserEntity>>

    /**
     * Get users by role within a school
     */
    @Query("SELECT * FROM users WHERE schoolId = :schoolId AND role = :role ORDER BY familyName, givenName")
    fun getUsersByRole(schoolId: String, role: String): Flow<List<UserEntity>>

    /**
     * Insert or replace user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Insert or replace multiple users
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    /**
     * Update user
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Delete user by ID
     */
    @Query("DELETE FROM users WHERE id = :userId AND schoolId = :schoolId")
    suspend fun deleteUser(userId: String, schoolId: String)

    /**
     * Delete all users for a school
     */
    @Query("DELETE FROM users WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    /**
     * Clear all cached users (used on logout)
     */
    @Query("DELETE FROM users")
    suspend fun clearAll()
}
