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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            appPreferences.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode, isDarkMode = when (mode) {
                    "dark" -> true
                    "light" -> false
                    else -> null
                }) }
            }
        }
        viewModelScope.launch {
            appPreferences.language.collect { lang ->
                _uiState.update { it.copy(language = lang) }
            }
        }
        viewModelScope.launch {
            appPreferences.notificationsEnabled.collect { enabled ->
                _uiState.update { it.copy(notificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            appPreferences.wallpaper.collect { id ->
                _uiState.update { it.copy(wallpaperId = id) }
            }
        }
    }

    fun onThemeModeChanged(mode: String) {
        viewModelScope.launch { appPreferences.setThemeMode(mode) }
    }

    fun onDarkModeChanged(isDarkMode: Boolean?) {
        val mode = when (isDarkMode) {
            true -> "dark"
            false -> "light"
            null -> "system"
        }
        onThemeModeChanged(mode)
    }

    fun onLanguageChanged(language: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language)
        )
        viewModelScope.launch { appPreferences.setLanguage(language) }
    }

    fun onNotificationsToggled(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setNotificationsEnabled(enabled) }
    }

    fun toggleLanguagePicker() {
        _uiState.update { it.copy(showLanguagePicker = !it.showLanguagePicker) }
    }

    fun toggleThemePicker() {
        _uiState.update { it.copy(showThemePicker = !it.showThemePicker) }
    }

    fun toggleWallpaperPicker() {
        _uiState.update { it.copy(showWallpaperPicker = !it.showWallpaperPicker) }
    }

    fun onWallpaperChanged(id: String) {
        viewModelScope.launch { appPreferences.setWallpaper(id) }
    }
}
