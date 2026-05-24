package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

/**
 * Entity for queuing mutations when offline.
 * Operations are processed by SyncWorker when connectivity is restored.
 */
@Entity(
    tableName = "pending_operations",
    indices = [
        Index(value = ["status"]),
        Index(value = ["entityType"]),
        Index(value = ["createdAt"])
    ]
)
data class PendingOperationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /** Type of operation: CREATE, UPDATE, DELETE */
    val type: OperationType,

    /** Entity type being operated on: Student, Attendance, Grade, etc. */
    val entityType: String,

    /** ID of the entity being operated on */
    val entityId: String,

    /** School ID for multi-tenant context */
    val schoolId: String,

    /** JSON serialized payload for the operation */
    val payload: String,

    /** When the operation was created */
    val createdAt: Instant = Instant.now(),

    /** Number of retry attempts */
    val retryCount: Int = 0,

    /** Maximum retries before marking as failed */
    val maxRetries: Int = 3,

    /** Last error message if failed */
    val lastError: String? = null,

    /** Current status of the operation */
    val status: OperationStatus = OperationStatus.PENDING,

    /** When the operation was last attempted */
    val lastAttemptAt: Instant? = null
)

/**
 * Type of mutation operation
 */
enum class OperationType {
    CREATE,
    UPDATE,
    DELETE
}

/**
 * Status of pending operation
 */
enum class OperationStatus {
    /** Waiting to be processed */
    PENDING,

    /** Currently being processed */
    PROCESSING,

    /** Failed after max retries */
    FAILED,

    /** Successfully completed */
    COMPLETED
}
