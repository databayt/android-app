package org.hogwarts.android.core.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks per-entity sync status for UI display.
 */
@Singleton
class SyncStatusTracker @Inject constructor() {

    private val _entityStatuses = MutableStateFlow<Map<String, EntitySyncStatus>>(emptyMap())
    val entityStatuses: Flow<Map<String, EntitySyncStatus>> = _entityStatuses.asStateFlow()

    private val _lastFullSync = MutableStateFlow<Instant?>(null)
    val lastFullSync: Flow<Instant?> = _lastFullSync.asStateFlow()

    val pendingCount: Flow<Int> = _entityStatuses.map { statuses ->
        statuses.values.count { it is EntitySyncStatus.Syncing }
    }

    val hasErrors: Flow<Boolean> = _entityStatuses.map { statuses ->
        statuses.values.any { it is EntitySyncStatus.Error }
    }

    fun updateEntityStatus(entityType: String, status: EntitySyncStatus) {
        _entityStatuses.value = _entityStatuses.value.toMutableMap().apply {
            put(entityType, status)
        }
    }

    fun markFullSyncComplete() {
        _lastFullSync.value = Instant.now()
    }

    fun clearAll() {
        _entityStatuses.value = emptyMap()
    }
}

sealed class EntitySyncStatus {
    data object Idle : EntitySyncStatus()
    data object Syncing : EntitySyncStatus()
    data class Synced(val lastSyncAt: Instant, val itemCount: Int) : EntitySyncStatus()
    data class Error(val message: String, val lastAttempt: Instant) : EntitySyncStatus()
}
