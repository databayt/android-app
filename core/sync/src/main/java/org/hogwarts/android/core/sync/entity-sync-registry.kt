package org.hogwarts.android.core.sync

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry of entity-specific sync handlers.
 * Each handler knows how to fetch and save data for a specific entity type.
 */
@Singleton
class EntitySyncRegistry @Inject constructor() {

    private val handlers = mutableMapOf<String, EntitySyncHandler>()

    fun register(entityType: String, handler: EntitySyncHandler) {
        handlers[entityType] = handler
    }

    fun getHandler(entityType: String): EntitySyncHandler? = handlers[entityType]

    fun getAllHandlers(): Map<String, EntitySyncHandler> = handlers.toMap()

    fun getSupportedEntities(): Set<String> = handlers.keys.toSet()
}

/**
 * Interface for entity-specific sync logic.
 */
interface EntitySyncHandler {
    val entityType: String
    suspend fun fetchAndSave(schoolId: String, lastSyncAt: java.time.Instant?): SyncResult
}

data class SyncResult(
    val entityType: String,
    val fetched: Int = 0,
    val saved: Int = 0,
    val errors: Int = 0,
    val errorMessage: String? = null
)
