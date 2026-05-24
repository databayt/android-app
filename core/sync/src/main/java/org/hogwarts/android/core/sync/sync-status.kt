package org.hogwarts.android.core.sync

sealed class SyncStatus {
    data object Idle : SyncStatus()
    data object Syncing : SyncStatus()
    data class Success(val lastSyncAt: java.time.Instant) : SyncStatus()
    data class Error(val message: String, val lastSyncAt: java.time.Instant?) : SyncStatus()
}
