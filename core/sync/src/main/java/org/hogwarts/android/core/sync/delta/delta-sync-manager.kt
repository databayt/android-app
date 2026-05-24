package org.hogwarts.android.core.sync.delta

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeltaSyncManager @Inject constructor() {

    private val lastSyncTimestamps = mutableMapOf<String, Long>()

    fun getLastSyncTimestamp(entityType: String): Long {
        return lastSyncTimestamps[entityType] ?: 0L
    }

    fun updateSyncTimestamp(entityType: String, timestamp: Long) {
        lastSyncTimestamps[entityType] = timestamp
        Timber.d("Updated sync timestamp for $entityType: $timestamp")
    }

    fun buildDeltaParams(entityType: String): Map<String, String> {
        val since = getLastSyncTimestamp(entityType)
        return if (since > 0) {
            mapOf("since" to since.toString(), "includeDeleted" to "true")
        } else {
            emptyMap() // Full sync on first load
        }
    }

    fun resetEntity(entityType: String) {
        lastSyncTimestamps.remove(entityType)
        Timber.d("Reset sync timestamp for $entityType")
    }

    fun resetAll() {
        lastSyncTimestamps.clear()
        Timber.d("Reset all sync timestamps")
    }
}
