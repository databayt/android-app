package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.OperationStatus
import org.hogwarts.android.core.database.entity.PendingOperationEntity

/**
 * DAO for managing offline mutation queue.
 */
@Dao
interface PendingOperationDao {

    /**
     * Get all pending operations for processing
     */
    @Query("SELECT * FROM pending_operations WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getOperationsByStatus(status: OperationStatus = OperationStatus.PENDING): List<PendingOperationEntity>

    /**
     * Get all pending operations (convenience alias for SyncWorker)
     */
    @Query("SELECT * FROM pending_operations WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<PendingOperationEntity>

    /**
     * Get pending operations count
     */
    @Query("SELECT COUNT(*) FROM pending_operations WHERE status = :status")
    fun observePendingCount(status: OperationStatus = OperationStatus.PENDING): Flow<Int>

    /**
     * Get all pending operations (observable)
     */
    @Query("SELECT * FROM pending_operations WHERE status IN (:statuses) ORDER BY createdAt DESC")
    fun observeOperations(
        statuses: List<OperationStatus> = listOf(
            OperationStatus.PENDING,
            OperationStatus.PROCESSING,
            OperationStatus.FAILED
        )
    ): Flow<List<PendingOperationEntity>>

    /**
     * Get operation by ID
     */
    @Query("SELECT * FROM pending_operations WHERE id = :id")
    suspend fun getOperationById(id: String): PendingOperationEntity?

    /**
     * Get operations for a specific entity
     */
    @Query("SELECT * FROM pending_operations WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun getOperationsForEntity(entityType: String, entityId: String): List<PendingOperationEntity>

    /**
     * Insert new operation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: PendingOperationEntity)

    /**
     * Update operation (e.g., status change, retry count)
     */
    @Update
    suspend fun updateOperation(operation: PendingOperationEntity)

    /**
     * Mark operation as completed
     */
    @Query("UPDATE pending_operations SET status = 'COMPLETED' WHERE id = :id")
    suspend fun markCompleted(id: String)

    /**
     * Mark operation as failed with error message
     */
    @Query("UPDATE pending_operations SET status = 'FAILED', lastError = :error WHERE id = :id")
    suspend fun markFailed(id: String, error: String)

    /**
     * Mark operation as processing
     */
    @Query("UPDATE pending_operations SET status = 'PROCESSING' WHERE id = :id")
    suspend fun markProcessing(id: String)

    /**
     * Delete operation by ID
     */
    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteOperation(id: String)

    /**
     * Delete completed operations
     */
    @Query("DELETE FROM pending_operations WHERE status = :status")
    suspend fun deleteByStatus(status: OperationStatus = OperationStatus.COMPLETED)

    /**
     * Delete stale operations (older than specified time)
     */
    @Query("DELETE FROM pending_operations WHERE status = :status AND createdAt < :before")
    suspend fun deleteStaleOperations(status: OperationStatus, before: Long)

    /**
     * Clear all operations (use carefully)
     */
    @Query("DELETE FROM pending_operations")
    suspend fun clearAll()
}
