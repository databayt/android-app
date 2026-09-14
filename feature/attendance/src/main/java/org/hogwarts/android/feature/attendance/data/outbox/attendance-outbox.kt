package org.hogwarts.android.feature.attendance.data.outbox

import kotlinx.serialization.json.Json
import org.hogwarts.android.core.database.dao.PendingOperationDao
import org.hogwarts.android.core.database.entity.OperationType
import org.hogwarts.android.core.database.entity.PendingOperationEntity
import org.hogwarts.android.core.database.entity.PendingOperationKinds
import org.hogwarts.android.feature.attendance.data.remote.AttendanceApi
import org.hogwarts.android.feature.attendance.data.remote.dto.OfflineSyncItemDto
import org.hogwarts.android.feature.attendance.data.remote.dto.OfflineSyncRequest
import javax.inject.Inject
import javax.inject.Singleton

/** Starts the background drain; WorkManager in the app, a counter in tests. */
fun interface AttendanceSyncScheduler {
    fun schedule()
}

/** How a drain ended, so the worker knows whether to try again. */
enum class DrainResult { Done, RetryLater }

/**
 * Quick-attendance marks made without a connection, parked in
 * `pending_operations` until `/api/mobile/offline/sync` answers for them.
 *
 * One row per section and day ([entityId] = `attendance:{section}:{date}`): a
 * re-mark replaces the parked one. The row id is the item's idempotency key,
 * so a resend after a lost response is answered `duplicate`, never applied
 * twice. A row leaves only on a server verdict — `applied`/`duplicate` delete
 * it, `rejected` parks it as FAILED with the server's code.
 */
@Singleton
class AttendanceOutbox @Inject constructor(
    private val dao: PendingOperationDao,
    private val json: Json,
    private val scheduler: AttendanceSyncScheduler,
) {

    suspend fun enqueue(item: OfflineSyncItemDto, coalesceKey: String, schoolId: String) {
        dao.getOperationsForEntity(KIND, coalesceKey).forEach { dao.deleteOperation(it.id) }
        dao.insertOperation(
            PendingOperationEntity(
                id = item.idempotencyKey,
                type = OperationType.UPDATE,
                entityType = KIND,
                entityId = coalesceKey,
                schoolId = schoolId,
                payload = json.encodeToString(OfflineSyncItemDto.serializer(), item),
            ),
        )
        scheduler.schedule()
    }

    /** Send every parked mark, in the order they were made, 50 at a time. */
    suspend fun drain(api: AttendanceApi): DrainResult {
        val pending = dao.getAllPending().filter { it.entityType == KIND }
        for (batch in pending.chunked(MAX_BATCH)) {
            val items = batch.mapNotNull { row ->
                runCatching { json.decodeFromString(OfflineSyncItemDto.serializer(), row.payload) }
                    .onFailure { dao.markFailed(row.id, "UNREADABLE_PAYLOAD") }
                    .getOrNull()
            }
            if (items.isEmpty()) continue
            val response = try {
                api.offlineSync(OfflineSyncRequest(items))
            } catch (e: retrofit2.HttpException) {
                // A body the server refuses as a whole will never pass; anything else may.
                if (e.code() == 400) {
                    items.forEach { dao.markFailed(it.idempotencyKey, "HTTP_400") }
                    continue
                }
                return DrainResult.RetryLater
            } catch (e: java.io.IOException) {
                return DrainResult.RetryLater
            }
            for (verdict in response.results) {
                when (verdict.result) {
                    "applied", "duplicate" -> dao.deleteOperation(verdict.idempotencyKey)
                    "rejected" -> dao.markFailed(verdict.idempotencyKey, verdict.code ?: "REJECTED")
                }
            }
        }
        val left = dao.getAllPending().count { it.entityType == KIND }
        return if (left > 0) DrainResult.RetryLater else DrainResult.Done
    }

    companion object {
        const val KIND = PendingOperationKinds.ATTENDANCE_QUICK
        const val MAX_BATCH = 50
    }
}
