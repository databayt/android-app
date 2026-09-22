package org.hogwarts.android.feature.subjects.textbook.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.readerDataStore by preferencesDataStore(name = "textbook_reader")

/** The six reading palettes, in the order the reference lays its cards out. */
enum class ReaderThemeName { Original, Quiet, Paper, Bold, Calm, Focus }

enum class GuideDim { High, Medium, Low, None }

/**
 * Reader preferences — the web's `prefs.ts`, key for key (`hogwarts:textbook:*`),
 * kept on the device as the web keeps them in localStorage.
 */
data class ReaderPrefs(
    val theme: ReaderThemeName = ReaderThemeName.Original,
    val dark: Boolean = false,
    val sans: Boolean = false,
    /** Index into [SCALES]. */
    val scaleIdx: Int = 1,
    /** 0 tight · 1 normal · 2 loose — see [LEADINGS]. */
    val leadingIdx: Int = 1,
    val brightness: Int = BRIGHTNESS_MAX,
    val rotationLocked: Boolean = false,
    val guide: Boolean = false,
    val guideDim: GuideDim = GuideDim.High,
) {
    val scale: Float get() = SCALES[scaleIdx]
    val leading: Float get() = LEADINGS[leadingIdx]

    companion object {
        val SCALES = listOf(0.85f, 1f, 1.15f, 1.3f, 1.5f, 1.75f)
        val LEADINGS = listOf(1.6f, 1.9f, 2.2f)
        const val BRIGHTNESS_MIN = 30
        const val BRIGHTNESS_MAX = 100
    }
}

/** Where the reader stood in a book: flow, PDF page and the block on screen. */
data class ReaderAnchor(val sec: Int, val page: Int?, val block: Int)

@Singleton
class ReaderPrefsStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val theme = stringPreferencesKey("hogwarts:textbook:theme")
        val font = stringPreferencesKey("hogwarts:textbook:font")
        val scale = stringPreferencesKey("hogwarts:textbook:scale")
        val leading = stringPreferencesKey("hogwarts:textbook:leading")
        val mode = stringPreferencesKey("hogwarts:textbook:mode")
        val brightness = stringPreferencesKey("hogwarts:textbook:brightness")
        val rotation = stringPreferencesKey("hogwarts:textbook:rotation")
        val guide = stringPreferencesKey("hogwarts:textbook:guide")
        val guideDim = stringPreferencesKey("hogwarts:textbook:guideDim")
        fun pos(slug: String) = stringPreferencesKey("hogwarts:textbook:$slug:pos")
        fun bookmarks(slug: String) = stringPreferencesKey("hogwarts:textbook:$slug:bookmarks")
    }

    val prefs: Flow<ReaderPrefs> = context.readerDataStore.data.map { it.toPrefs() }

    fun bookmarks(slug: String): Flow<List<Int>> = context.readerDataStore.data.map { p ->
        p[Keys.bookmarks(slug)]?.let(::parseBookmarks).orEmpty()
    }

    suspend fun anchor(slug: String): ReaderAnchor? =
        context.readerDataStore.data.first()[Keys.pos(slug)]?.let(::parseAnchor)

    suspend fun saveAnchor(slug: String, anchor: ReaderAnchor) {
        context.readerDataStore.edit {
            it[Keys.pos(slug)] = """{"sec":${anchor.sec},"page":${anchor.page ?: "null"},"block":${anchor.block}}"""
        }
    }

    suspend fun saveBookmarks(slug: String, pages: List<Int>) {
        context.readerDataStore.edit { it[Keys.bookmarks(slug)] = pages.joinToString(",", "[", "]") }
    }

    suspend fun update(transform: (ReaderPrefs) -> ReaderPrefs) {
        context.readerDataStore.edit { p ->
            val next = transform(p.toPrefs())
            p[Keys.theme] = next.theme.name.lowercase()
            p[Keys.mode] = if (next.dark) "dark" else "light"
            p[Keys.font] = if (next.sans) "sans" else "serif"
            p[Keys.scale] = next.scaleIdx.toString()
            p[Keys.leading] = listOf("tight", "normal", "loose")[next.leadingIdx]
            p[Keys.brightness] = next.brightness.toString()
            p[Keys.rotation] = if (next.rotationLocked) "on" else "off"
            p[Keys.guide] = if (next.guide) "on" else "off"
            p[Keys.guideDim] = next.guideDim.name.lowercase()
        }
    }

    private fun Preferences.toPrefs() = ReaderPrefs(
        theme = ReaderThemeName.entries.find { it.name.equals(this[Keys.theme], true) } ?: ReaderThemeName.Original,
        dark = this[Keys.mode] == "dark",
        sans = this[Keys.font] == "sans",
        scaleIdx = this[Keys.scale]?.toIntOrNull()?.takeIf { it in ReaderPrefs.SCALES.indices } ?: 1,
        leadingIdx = when (this[Keys.leading]) { "tight" -> 0; "loose" -> 2; else -> 1 },
        brightness = (this[Keys.brightness]?.toIntOrNull() ?: ReaderPrefs.BRIGHTNESS_MAX)
            .coerceIn(ReaderPrefs.BRIGHTNESS_MIN, ReaderPrefs.BRIGHTNESS_MAX),
        rotationLocked = this[Keys.rotation] == "on",
        guide = this[Keys.guide] == "on",
        guideDim = GuideDim.entries.find { it.name.equals(this[Keys.guideDim], true) } ?: GuideDim.High,
    )
}

/** `{"sec":3,"page":12,"block":4}` — the web's stored anchor. */
internal fun parseAnchor(raw: String): ReaderAnchor? {
    fun field(name: String): String? =
        Regex(""""$name"\s*:\s*(-?\d+|null)""").find(raw)?.groupValues?.get(1)
    val sec = field("sec")?.toIntOrNull() ?: return null
    return ReaderAnchor(sec, field("page")?.toIntOrNull(), field("block")?.toIntOrNull() ?: 0)
}

internal fun parseBookmarks(raw: String): List<Int> =
    Regex("""-?\d+""").findAll(raw).mapNotNull { it.value.toIntOrNull() }.toList()
