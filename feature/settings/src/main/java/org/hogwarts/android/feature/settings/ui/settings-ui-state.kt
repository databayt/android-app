package org.hogwarts.android.feature.settings.ui

/** The settings page's tabs, in the web's order — `settings/content-enhanced.tsx`. */
enum class SettingsTab { Appearance, Notifications, Password, Language }

/** The web's colour modes; the app persists the choice as `AppPreferences.themeMode`. */
enum class ThemeMode(val wire: String) {
    Light("light"),
    Dark("dark"),
    System("system");

    companion object {
        fun fromWire(value: String?): ThemeMode = entries.firstOrNull { it.wire == value } ?: System
    }
}

data class SettingsUiState(
    val tab: SettingsTab = SettingsTab.Appearance,
    val themeMode: ThemeMode = ThemeMode.System,
)
