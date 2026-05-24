package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.NotificationEntity

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications WHERE schoolId = :schoolId ORDER BY createdAt DESC")
    fun observeAll(schoolId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE schoolId = :schoolId AND type = :type ORDER BY createdAt DESC")
    fun observeByType(schoolId: String, type: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE schoolId = :schoolId AND isRead = 0 ORDER BY createdAt DESC")
    fun observeUnread(schoolId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE schoolId = :schoolId AND isRead = 0")
    fun observeUnreadCount(schoolId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE schoolId = :schoolId AND id = :notificationId")
    suspend fun markRead(schoolId: String, notificationId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE schoolId = :schoolId")
    suspend fun markAllRead(schoolId: String)

    @Query("DELETE FROM notifications WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}
