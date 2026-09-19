package org.hogwarts.android.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hogwarts_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "light", "dark", "system"
        val WALLPAPER = stringPreferencesKey("wallpaper") // matches WallpaperCatalog ids
        val LOCALE_DEFAULTED = booleanPreferencesKey("locale_defaulted") // Arabic applied on first launch
        val SEARCH_RECENTS = stringPreferencesKey("search_recents") // "<id>|<ms>" lines, newest first
    }

    val themeMode: Flow<String> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }
    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "en" }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
    val wallpaper: Flow<String> = context.dataStore.data.map { it[Keys.WALLPAPER] ?: "aurora" }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    /** Whether the first-launch Arabic default has already been applied (once, ever). */
    val localeDefaulted: Flow<Boolean> = context.dataStore.data.map { it[Keys.LOCALE_DEFAULTED] ?: false }

    /** Records the first-launch default, and the language it chose, so settings agrees. */
    suspend fun markLocaleDefaulted(language: String) {
        context.dataStore.edit {
            it[Keys.LOCALE_DEFAULTED] = true
            it[Keys.LANGUAGE] = language
        }
    }

    suspend fun setWallpaper(id: String) {
        context.dataStore.edit { it[Keys.WALLPAPER] = id }
    }

    /**
     * The search's recent rows, newest first — the ids only, since the row
     * itself is rebuilt from the nav registry each time and a stored title
     * would go stale the moment the language changed.
     *
     * `use-recent-items.ts` keeps ten and forgets anything older than thirty
     * days; so does this. Stored as one `id|epochMillis` per line rather than
     * JSON, because two fields do not need a parser.
     */
    val searchRecents: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val cutoff = System.currentTimeMillis() - RECENT_TTL_MS
        prefs[Keys.SEARCH_RECENTS].orEmpty()
            .lineSequence()
            .mapNotNull { line ->
                val id = line.substringBefore('|', "")
                val at = line.substringAfter('|', "").toLongOrNull()
                if (id.isEmpty() || at == null || at <= cutoff) null else id
            }
            .toList()
    }

    /** Puts [id] at the front, keeping the most recent [MAX_RECENTS]. */
    suspend fun recordSearchRecent(id: String) {
        context.dataStore.edit { prefs ->
            val now = System.currentTimeMillis()
            val cutoff = now - RECENT_TTL_MS
            val kept = prefs[Keys.SEARCH_RECENTS].orEmpty()
                .lineSequence()
                .filter { line ->
                    val at = line.substringAfter('|', "").toLongOrNull()
                    line.substringBefore('|', "").let { it.isNotEmpty() && it != id } && at != null && at > cutoff
                }
                .take(MAX_RECENTS - 1)
                .toList()
            prefs[Keys.SEARCH_RECENTS] = (listOf("$id|$now") + kept).joinToString("\n")
        }
    }
}

private const val MAX_RECENTS = 10
private const val RECENT_TTL_MS = 30L * 24 * 60 * 60 * 1000
