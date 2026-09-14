package org.hogwarts.android.feature.settings.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.preferences.AppPreferences
import javax.inject.Inject

/** Applies a per-app language; the real one goes through AppCompat, tests record the call. */
fun interface AppLocaleSetter {
    fun set(languageTag: String)
}

@HiltViewModel
class SettingsViewModel internal constructor(
    private val preferences: SettingsPreferences,
    private val localeSetter: AppLocaleSetter,
) : ViewModel() {

    @Inject constructor(appPreferences: AppPreferences) : this(
        preferences = AppSettingsPreferences(appPreferences),
        // From a live AppCompatActivity this reaches the platform LocaleManager on
        // API 33+ and AppCompat's own storage below — the path the shell Menu uses.
        localeSetter = { tag -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag)) },
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.themeMode().collect { mode -> _uiState.update { it.copy(themeMode = ThemeMode.fromWire(mode)) } }
        }
    }

    fun selectTab(tab: SettingsTab) = _uiState.update { it.copy(tab = tab) }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode.wire) }
    }

    fun setLanguage(language: String) {
        localeSetter.set(language)
        viewModelScope.launch { preferences.setLanguage(language) }
    }
}

/** The two preferences this screen writes, behind a seam so tests need no DataStore. */
interface SettingsPreferences {
    fun themeMode(): kotlinx.coroutines.flow.Flow<String>
    suspend fun setThemeMode(mode: String)
    suspend fun setLanguage(language: String)
}

private class AppSettingsPreferences(private val prefs: AppPreferences) : SettingsPreferences {
    override fun themeMode() = prefs.themeMode
    override suspend fun setThemeMode(mode: String) = prefs.setThemeMode(mode)
    override suspend fun setLanguage(language: String) = prefs.setLanguage(language)
}
