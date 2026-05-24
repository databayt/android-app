package org.hogwarts.android.feature.settings.ui

import org.hogwarts.android.core.designsystem.wallpaper.WallpaperCatalog

data class SettingsUiState(
    val themeMode: String = "system", // "light", "dark", "system"
    val isDarkMode: Boolean? = null,
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val appVersion: String = "1.0.0",
    val showLanguagePicker: Boolean = false,
    val showThemePicker: Boolean = false,
    val wallpaperId: String = WallpaperCatalog.DEFAULT_ID,
    val showWallpaperPicker: Boolean = false
)
