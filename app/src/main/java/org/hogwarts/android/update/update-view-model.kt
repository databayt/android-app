package org.hogwarts.android.update

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.brand.AppVersionInfo
import org.hogwarts.android.core.common.brand.BrandConfig
import org.hogwarts.android.core.common.brand.LiveRoomPresence

/** What the screen draws: a decision plus the text and link it needs. */
data class UpdateUi(
    val decision: UpdateDecision = UpdateDecision.None,
    val apkUrl: String = "",
    val notesAr: String? = null,
    val notesEn: String? = null,
)

/**
 * Runs the version check and turns its result into [UpdateUi]. The decision is deferred (not lost)
 * while a live class room is open: [LiveRoomPresence] flips it back on when the room is left.
 */
@HiltViewModel
class UpdateViewModel @javax.inject.Inject constructor(
    @ApplicationContext context: Context,
    private val checker: UpdateChecker,
    private val brand: BrandConfig,
    private val app: AppVersionInfo,
) : ViewModel() {

    private val prefs = context.getSharedPreferences("update_prompt", Context.MODE_PRIVATE)
    private val manifest = MutableStateFlow<UpdateManifest?>(null)
    private val optionalOnScreen = MutableStateFlow(false)
    private val optionalDismissed = MutableStateFlow(false)

    val ui: StateFlow<UpdateUi> = combine(
        manifest, LiveRoomPresence.inRoom, optionalOnScreen, optionalDismissed,
    ) { m, inRoom, onScreen, dismissed ->
        if (m == null) return@combine UpdateUi()
        val now = System.currentTimeMillis()
        // Once the optional prompt is up it stays until answered, even though "shown" is now recorded.
        val lastShown = if (onScreen) 0L else prefs.getLong(KEY_LAST_OPTIONAL, 0L)
        var decision = decideUpdate(app.versionCode, m.latestCode, m.minCode, lastShown, now, inRoom)
        if (decision == UpdateDecision.Optional && dismissed) decision = UpdateDecision.None
        if (decision == UpdateDecision.Optional && !onScreen) {
            prefs.edit().putLong(KEY_LAST_OPTIONAL, now).apply()
            optionalOnScreen.value = true
        }
        UpdateUi(decision, m.apkUrl?.takeIf { it.isNotBlank() } ?: brand.apkUrl, m.notesAr, m.notesEn)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UpdateUi())

    /** Called on every activity start; the checker throttles to once per 30 min. */
    fun check() {
        viewModelScope.launch {
            checker.check(brand.updateUrl)?.let { manifest.value = it }
        }
    }

    fun dismissOptional() {
        optionalDismissed.value = true
        optionalOnScreen.value = false
    }

    private companion object {
        const val KEY_LAST_OPTIONAL = "last_optional_shown_ms"
    }
}
