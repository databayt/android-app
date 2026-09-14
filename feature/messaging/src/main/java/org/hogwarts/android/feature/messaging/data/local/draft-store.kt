package org.hogwarts.android.feature.messaging.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.draftsDataStore by preferencesDataStore(name = "messaging_drafts")

/** The composer's unsent text per conversation (`messaging:drafts` on the web). */
interface ConversationDrafts {
    fun observe(conversationId: String): Flow<String>
    suspend fun save(conversationId: String, text: String)
}

/** Per-conversation draft persistence, local to the device as the web's is. */
@Singleton
class DraftStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : ConversationDrafts {
    private fun key(conversationId: String) = stringPreferencesKey("draft:$conversationId")

    override fun observe(conversationId: String): Flow<String> =
        context.draftsDataStore.data.map { prefs -> prefs[key(conversationId)] ?: "" }

    override suspend fun save(conversationId: String, text: String) {
        context.draftsDataStore.edit { prefs ->
            if (text.isBlank()) prefs.remove(key(conversationId))
            else prefs[key(conversationId)] = text
        }
    }

    suspend fun clear(conversationId: String) {
        context.draftsDataStore.edit { prefs -> prefs.remove(key(conversationId)) }
    }
}
