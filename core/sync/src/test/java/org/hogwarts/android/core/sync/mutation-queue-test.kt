package org.hogwarts.android.core.sync

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hogwarts.android.core.database.dao.PendingOperationDao
import org.hogwarts.android.core.database.entity.OperationStatus
import org.hogwarts.android.core.database.entity.OperationType
import org.hogwarts.android.core.database.entity.PendingOperationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MutationQueueTest {

    private lateinit var pendingOperationDao: PendingOperationDao
    private lateinit var mutationQueue: MutationQueue

    private fun createOperation(
        id: String = "op-1",
        type: OperationType = OperationType.CREATE,
        entityType: String = "Attendance",
        entityId: String = "entity-1",
        schoolId: String = "school-1",
        payload: String = """{"status":"PRESENT"}""",
        retryCount: Int = 0,
        maxRetries: Int = 3,
        status: OperationStatus = OperationStatus.PENDING
    ) = PendingOperationEntity(
        id = id,
        type = type,
        entityType = entityType,
        entityId = entityId,
        schoolId = schoolId,
        payload = payload,
        createdAt = Instant.now(),
        retryCount = retryCount,
        maxRetries = maxRetries,
        status = status
    )

    @Before
    fun setUp() {
        pendingOperationDao = mockk(relaxed = true)
        mutationQueue = MutationQueue(pendingOperationDao)
    }

    // --- Empty queue ---

    @Test
    fun `processPending with empty queue does nothing`() = runTest {
        coEvery { pendingOperationDao.getAllPending() } returns emptyList()

        mutationQueue.processPending()

        coVerify(exactly = 1) { pendingOperationDao.getAllPending() }
        coVerify(exactly = 0) { pendingOperationDao.markProcessing(any()) }
        coVerify(exactly = 0) { pendingOperationDao.markCompleted(any()) }
        coVerify(exactly = 0) { pendingOperationDao.markFailed(any(), any()) }
    }

    // --- FIFO order processing ---

    @Test
    fun `processPending processes operations in FIFO order`() = runTest {
        val operations = listOf(
            createOperation(id = "op-1", entityType = "Attendance"),
            createOperation(id = "op-2", entityType = "Grade"),
            createOperation(id = "op-3", entityType = "Message")
        )

        coEvery { pendingOperationDao.getAllPending() } returns operations

        mutationQueue.processPending()

        // Verify operations are processed in order (FIFO)
        coVerifyOrder {
            pendingOperationDao.markProcessing("op-1")
            pendingOperationDao.markCompleted("op-1")
            pendingOperationDao.markProcessing("op-2")
            pendingOperationDao.markCompleted("op-2")
            pendingOperationDao.markProcessing("op-3")
            pendingOperationDao.markCompleted("op-3")
        }
    }

    @Test
    fun `processPending marks each operation as processing before completion`() = runTest {
        val operation = createOperation(id = "op-1", entityType = "Attendance")
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        mutationQueue.processPending()

        coVerifyOrder {
            pendingOperationDao.markProcessing("op-1")
            pendingOperationDao.markCompleted("op-1")
        }
    }

    // --- Handles all entity types ---

    @Test
    fun `processPending handles Attendance entity type`() = runTest {
        val operation = createOperation(entityType = "Attendance")
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        mutationQueue.processPending()

        coVerify { pendingOperationDao.markCompleted(operation.id) }
    }

    @Test
    fun `processPending handles Grade entity type`() = runTest {
        val operation = createOperation(entityType = "Grade")
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        mutationQueue.processPending()

        coVerify { pendingOperationDao.markCompleted(operation.id) }
    }

    @Test
    fun `processPending handles Message entity type`() = runTest {
        val operation = createOperation(entityType = "Message")
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        mutationQueue.processPending()

        coVerify { pendingOperationDao.markCompleted(operation.id) }
    }

    @Test
    fun `processPending handles Fee entity type`() = runTest {
        val operation = createOperation(entityType = "Fee")
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        mutationQueue.processPending()

        coVerify { pendingOperationDao.markCompleted(operation.id) }
    }

    @Test
    fun `processPending handles Student entity type`() = runTest {
        val operation = createOperation(entityType = "Student")
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        mutationQueue.processPending()

        coVerify { pendingOperationDao.markCompleted(operation.id) }
    }

    // --- Unknown entity type ---

    @Test
    fun `processPending marks unknown entity type as failed`() = runTest {
        val operation = createOperation(entityType = "Unknown")
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        mutationQueue.processPending()

        coVerify {
            pendingOperationDao.markFailed(operation.id, "Unknown entity type: Unknown")
        }
        coVerify(exactly = 0) { pendingOperationDao.markCompleted(operation.id) }
    }

    @Test
    fun `processPending continues processing after unknown entity type`() = runTest {
        val operations = listOf(
            createOperation(id = "op-1", entityType = "Unknown"),
            createOperation(id = "op-2", entityType = "Attendance")
        )
        coEvery { pendingOperationDao.getAllPending() } returns operations

        mutationQueue.processPending()

        // Unknown should be failed, but next operation should still be processed
        coVerify { pendingOperationDao.markFailed("op-1", "Unknown entity type: Unknown") }
        coVerify { pendingOperationDao.markCompleted("op-2") }
    }

    // --- Failed operation increments retry count ---

    @Test
    fun `failed operation increments retry count and resets to pending`() = runTest {
        val operation = createOperation(
            id = "op-fail",
            entityType = "Attendance",
            retryCount = 0,
            maxRetries = 3
        )
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        // Simulate processing failure by making markProcessing succeed
        // but then throwing exception during the process method
        coEvery { pendingOperationDao.markProcessing("op-fail") } throws RuntimeException("API timeout")

        mutationQueue.processPending()

        // Should update operation with incremented retry count
        val capturedOperation = slot<PendingOperationEntity>()
        coVerify { pendingOperationDao.updateOperation(capture(capturedOperation)) }

        assertEquals(1, capturedOperation.captured.retryCount)
        assertEquals(OperationStatus.PENDING, capturedOperation.captured.status)
        assertEquals("API timeout", capturedOperation.captured.lastError)
    }

    @Test
    fun `second failure increments retry count to 2`() = runTest {
        val operation = createOperation(
            id = "op-fail",
            entityType = "Attendance",
            retryCount = 1,
            maxRetries = 3
        )
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)
        coEvery { pendingOperationDao.markProcessing("op-fail") } throws RuntimeException("API timeout")

        mutationQueue.processPending()

        val capturedOperation = slot<PendingOperationEntity>()
        coVerify { pendingOperationDao.updateOperation(capture(capturedOperation)) }

        assertEquals(2, capturedOperation.captured.retryCount)
        assertEquals(OperationStatus.PENDING, capturedOperation.captured.status)
    }

    // --- Max retries marks operation as FAILED ---

    @Test
    fun `max retries reached marks operation as FAILED`() = runTest {
        val operation = createOperation(
            id = "op-maxed",
            entityType = "Attendance",
            retryCount = 2,  // Already retried twice, maxRetries=3
            maxRetries = 3
        )
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)
        coEvery { pendingOperationDao.markProcessing("op-maxed") } throws RuntimeException("Permanent failure")

        mutationQueue.processPending()

        // retryCount + 1 = 3, which equals maxRetries = 3
        // So it should be marked as failed, not retried
        coVerify {
            pendingOperationDao.markFailed("op-maxed", "Permanent failure")
        }
        coVerify(exactly = 0) { pendingOperationDao.updateOperation(any()) }
    }

    @Test
    fun `operation with maxRetries of 1 fails immediately on first error`() = runTest {
        val operation = createOperation(
            id = "op-once",
            entityType = "Grade",
            retryCount = 0,
            maxRetries = 1
        )
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)
        coEvery { pendingOperationDao.markProcessing("op-once") } throws RuntimeException("First and only try")

        mutationQueue.processPending()

        // retryCount + 1 = 1, which equals maxRetries = 1
        coVerify {
            pendingOperationDao.markFailed("op-once", "First and only try")
        }
        coVerify(exactly = 0) { pendingOperationDao.updateOperation(any()) }
    }

    // --- Error with null message ---

    @Test
    fun `failed operation with null error message uses Unknown error`() = runTest {
        val operation = createOperation(
            id = "op-null-err",
            entityType = "Attendance",
            retryCount = 2,
            maxRetries = 3
        )
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        // Exception with null message
        coEvery { pendingOperationDao.markProcessing("op-null-err") } throws RuntimeException()

        mutationQueue.processPending()

        coVerify {
            pendingOperationDao.markFailed("op-null-err", "Unknown error")
        }
    }

    // --- Mixed success and failure ---

    @Test
    fun `processPending continues after one operation fails`() = runTest {
        val operations = listOf(
            createOperation(id = "op-1", entityType = "Attendance"),
            createOperation(id = "op-2", entityType = "Grade", retryCount = 2, maxRetries = 3),
            createOperation(id = "op-3", entityType = "Message")
        )
        coEvery { pendingOperationDao.getAllPending() } returns operations

        // Second operation fails during processing
        coEvery { pendingOperationDao.markProcessing("op-2") } throws RuntimeException("DB error")

        mutationQueue.processPending()

        // First should succeed
        coVerify { pendingOperationDao.markCompleted("op-1") }

        // Second should fail (max retries)
        coVerify { pendingOperationDao.markFailed("op-2", "DB error") }

        // Third should still be processed and succeed
        coVerify { pendingOperationDao.markCompleted("op-3") }
    }

    // --- Operation types ---

    @Test
    fun `processPending handles CREATE operations`() = runTest {
        val operation = createOperation(type = OperationType.CREATE, entityType = "Attendance")
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        mutationQueue.processPending()

        coVerify { pendingOperationDao.markCompleted(operation.id) }
    }

    @Test
    fun `processPending handles UPDATE operations`() = runTest {
        val operation = createOperation(type = OperationType.UPDATE, entityType = "Grade")
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        mutationQueue.processPending()

        coVerify { pendingOperationDao.markCompleted(operation.id) }
    }

    @Test
    fun `processPending handles DELETE operations`() = runTest {
        val operation = createOperation(type = OperationType.DELETE, entityType = "Student")
        coEvery { pendingOperationDao.getAllPending() } returns listOf(operation)

        mutationQueue.processPending()

        coVerify { pendingOperationDao.markCompleted(operation.id) }
    }
}
