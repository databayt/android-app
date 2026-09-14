package org.hogwarts.android.feature.settings.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h844dp-xxhdpi")
class SettingsScreenshotTest {

    @Test fun appearance_en() = shot("settings_appearance_en", SettingsTab.Appearance, rtl = false, dark = false)
    @Config(qualifiers = "+ar")
    @Test fun appearance_ar() = shot("settings_appearance_ar", SettingsTab.Appearance, rtl = true, dark = false)
    @Config(qualifiers = "+ar")
    @Test fun appearance_ar_dark() = shot("settings_appearance_ar_dark", SettingsTab.Appearance, rtl = true, dark = true)

    @Test fun language_en() = shot("settings_language_en", SettingsTab.Language, rtl = false, dark = false)
    @Config(qualifiers = "+ar")
    @Test fun language_ar() = shot("settings_language_ar", SettingsTab.Language, rtl = true, dark = false)
    @Config(qualifiers = "+ar")
    @Test fun language_ar_dark() = shot("settings_language_ar_dark", SettingsTab.Language, rtl = true, dark = true)

    @Config(qualifiers = "+ar")
    @Test fun notifications_ar() = shot("settings_notifications_ar", SettingsTab.Notifications, rtl = true, dark = false)
    @Test fun password_en() = shot("settings_password_en", SettingsTab.Password, rtl = false, dark = false)

    private fun shot(name: String, tab: SettingsTab, rtl: Boolean, dark: Boolean) {
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) {
                    SettingsContent(
                        state = SettingsUiState(tab = tab, themeMode = ThemeMode.System),
                        language = if (rtl) "ar" else "en",
                        onSelectTab = {},
                        onThemeMode = {},
                        onLanguage = {},
                        onOpenHref = {},
                        onOpenNotificationPreferences = {},
                        onLogout = {},
                    )
                }
            }
        }
    }
}
