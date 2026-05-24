package org.hogwarts.android.core.sync

import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Conflict resolution strategy for offline-first sync.
 * Uses last-write-wins with server priority.
 */
@Singleton
class ConflictResolver @Inject constructor() {

    /**
     * Resolve conflict between local and remote versions.
     * Strategy: Server wins unless local has unsaved mutations.
     */
    fun <T : Syncable> resolve(
        local: T,
        remote: T,
        hasPendingMutation: Boolean
    ): ConflictResolution<T> {
        return when {
            // If local has pending mutations, keep local (will be pushed on next sync)
            hasPendingMutation -> {
                Timber.d("Conflict: keeping local (has pending mutation) for ${local.syncId}")
                ConflictResolution.KeepLocal(local)
            }
            // If remote is newer, accept remote
            remote.updatedAt.isAfter(local.updatedAt) -> {
                Timber.d("Conflict: accepting remote (newer) for ${local.syncId}")
                ConflictResolution.AcceptRemote(remote)
            }
            // If local is newer (shouldn't happen without pending mutation), keep local
            local.updatedAt.isAfter(remote.updatedAt) -> {
                Timber.d("Conflict: keeping local (newer) for ${local.syncId}")
                ConflictResolution.KeepLocal(local)
            }
            // Same timestamp, server wins
            else -> {
                Timber.d("Conflict: accepting remote (same timestamp, server wins) for ${local.syncId}")
                ConflictResolution.AcceptRemote(remote)
            }
        }
    }
}

/**
 * Interface for entities that participate in sync.
 */
interface Syncable {
    val syncId: String
    val updatedAt: Instant
}

/**
 * Result of conflict resolution.
 */
sealed class ConflictResolution<T> {
    data class KeepLocal<T>(val data: T) : ConflictResolution<T>()
    data class AcceptRemote<T>(val data: T) : ConflictResolution<T>()
    data class Merge<T>(val data: T) : ConflictResolution<T>()
}
