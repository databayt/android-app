package org.hogwarts.android.core.sync

import kotlinx.coroutines.flow.Flow

interface SyncManager {
    val syncStatus: Flow<SyncStatus>
    suspend fun requestSync()
    suspend fun requestFullSync()
    fun cancelSync()
}
