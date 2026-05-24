package org.hogwarts.android.core.sync

import app.cash.turbine.test
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var workManager: WorkManager
    private lateinit var syncManager: SyncManagerImpl
    private val mockOperation: Operation = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        workManager = mockk(relaxed = true)

        // Mock WorkManager operations to return mock Operation
        every {
            workManager.enqueueUniquePeriodicWork(any(), any(), any())
        } returns mockOperation
        every {
            workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        } returns mockOperation
        every {
            workManager.cancelUniqueWork(any())
        } returns mockOperation

        syncManager = SyncManagerImpl(workManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Initial status ---

    @Test
    fun `initial sync status is Idle`() = runTest {
        syncManager.syncStatus.test {
            val status = awaitItem()
            assertTrue(status is SyncStatus.Idle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Periodic sync scheduled on init ---

    @Test
    fun `periodic sync is scheduled on initialization`() {
        // SyncManagerImpl constructor calls schedulePeriodicSync
        verify(exactly = 1) {
            workManager.enqueueUniquePeriodicWork(
                SyncManagerImpl.PERIODIC_SYNC_WORK,
                any(),
                any()
            )
        }
    }

    // --- requestSync ---

    @Test
    fun `requestSync updates status to Syncing`() = runTest {
        syncManager.syncStatus.test {
            // Initial Idle
            val idle = awaitItem()
            assertTrue(idle is SyncStatus.Idle)

            syncManager.requestSync()

            // Status should be Syncing after request
            val syncing = awaitItem()
            assertTrue(syncing is SyncStatus.Syncing)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `requestSync enqueues one-time work with WorkManager`() = runTest {
        syncManager.requestSync()

        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                SyncManagerImpl.ONE_TIME_SYNC_WORK,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    // --- requestFullSync ---

    @Test
    fun `requestFullSync updates status to Syncing`() = runTest {
        syncManager.syncStatus.test {
            // Initial Idle
            val idle = awaitItem()
            assertTrue(idle is SyncStatus.Idle)

            syncManager.requestFullSync()

            // Status should be Syncing after full sync request
            val syncing = awaitItem()
            assertTrue(syncing is SyncStatus.Syncing)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `requestFullSync enqueues full sync work with WorkManager`() = runTest {
        syncManager.requestFullSync()

        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                SyncManagerImpl.FULL_SYNC_WORK,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    // --- cancelSync ---

    @Test
    fun `cancelSync resets status to Idle`() = runTest {
        syncManager.syncStatus.test {
            // Initial Idle
            val idle = awaitItem()
            assertTrue(idle is SyncStatus.Idle)

            // Move to Syncing
            syncManager.requestSync()
            val syncing = awaitItem()
            assertTrue(syncing is SyncStatus.Syncing)

            // Cancel
            syncManager.cancelSync()
            val cancelled = awaitItem()
            assertTrue(cancelled is SyncStatus.Idle)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancelSync cancels both one-time and full sync work`() {
        syncManager.cancelSync()

        verify(exactly = 1) {
            workManager.cancelUniqueWork(SyncManagerImpl.ONE_TIME_SYNC_WORK)
        }
        verify(exactly = 1) {
            workManager.cancelUniqueWork(SyncManagerImpl.FULL_SYNC_WORK)
        }
    }

    // --- updateStatus ---

    @Test
    fun `updateStatus changes sync status to Success`() = runTest {
        val successStatus = SyncStatus.Success(java.time.Instant.now())

        syncManager.syncStatus.test {
            // Initial Idle
            val idle = awaitItem()
            assertTrue(idle is SyncStatus.Idle)

            syncManager.updateStatus(successStatus)

            val success = awaitItem()
            assertTrue(success is SyncStatus.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateStatus changes sync status to Error`() = runTest {
        val errorStatus = SyncStatus.Error("Sync failed", null)

        syncManager.syncStatus.test {
            // Initial Idle
            val idle = awaitItem()
            assertTrue(idle is SyncStatus.Idle)

            syncManager.updateStatus(errorStatus)

            val error = awaitItem()
            assertTrue(error is SyncStatus.Error)
            assertEquals("Sync failed", (error as SyncStatus.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Status transitions ---

    @Test
    fun `full lifecycle - idle to syncing to success`() = runTest {
        syncManager.syncStatus.test {
            // 1. Initial Idle
            val idle = awaitItem()
            assertTrue(idle is SyncStatus.Idle)

            // 2. Request sync -> Syncing
            syncManager.requestSync()
            val syncing = awaitItem()
            assertTrue(syncing is SyncStatus.Syncing)

            // 3. Worker completes -> Success
            syncManager.updateStatus(SyncStatus.Success(java.time.Instant.now()))
            val success = awaitItem()
            assertTrue(success is SyncStatus.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `full lifecycle - idle to syncing to error`() = runTest {
        syncManager.syncStatus.test {
            // 1. Initial Idle
            val idle = awaitItem()
            assertTrue(idle is SyncStatus.Idle)

            // 2. Request sync -> Syncing
            syncManager.requestSync()
            val syncing = awaitItem()
            assertTrue(syncing is SyncStatus.Syncing)

            // 3. Worker fails -> Error
            syncManager.updateStatus(SyncStatus.Error("Network timeout", null))
            val error = awaitItem()
            assertTrue(error is SyncStatus.Error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
