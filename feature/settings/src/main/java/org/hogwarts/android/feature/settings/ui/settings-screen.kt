package org.hogwarts.android.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.PageNav
import org.hogwarts.android.core.designsystem.kit.PageNavItem
import org.hogwarts.android.core.designsystem.locale.currentLocale
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.settings.R

/**
 * `/settings` on a phone: the page's tabs, then each tab's grey grouped list.
 * The app shell draws the platform header above it.
 *
 * @param onOpenHref web paths that have no native screen.
 * @param onOpenNotificationPreferences the real per-type preferences page.
 */
@Composable
fun SettingsScreen(
    onOpenHref: (String) -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        state = state,
        language = currentLocale().language.takeIf { it == "en" } ?: "ar",
        onSelectTab = viewModel::selectTab,
        onThemeMode = viewModel::setThemeMode,
        onLanguage = viewModel::setLanguage,
        onOpenHref = onOpenHref,
        onOpenNotificationPreferences = onOpenNotificationPreferences,
        onLogout = onLogout,
    )
}

@Composable
internal fun SettingsContent(
    state: SettingsUiState,
    language: String,
    onSelectTab: (SettingsTab) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onLanguage: (String) -> Unit,
    onOpenHref: (String) -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(HogwartsTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp),
    ) {
        PageNav(
            items = SettingsTab.entries.map { PageNavItem(it.name, stringResource(it.labelRes())) },
            selectedKey = state.tab.name,
            onSelect = { onSelectTab(SettingsTab.valueOf(it.key)) },
        )
        Spacer(Modifier.height(24.dp))
        when (state.tab) {
            SettingsTab.Appearance -> AppearanceTab(state.themeMode, onThemeMode)
            SettingsTab.Notifications -> NotificationsTab(onOpenNotificationPreferences)
            SettingsTab.Password -> PasswordTab()
            SettingsTab.Language -> LanguageTab(language, onLanguage)
        }
        Spacer(Modifier.height(32.dp))
        // Web: logout sits in the header's account menu, which the app shell doesn't carry yet.
        GroupedList {
            GroupedRow(
                title = stringResource(R.string.settings_logout),
                titleColor = HogwartsTheme.colors.destructive,
                onClick = onLogout,
            )
        }
    }
}

private fun SettingsTab.labelRes(): Int = when (this) {
    SettingsTab.Appearance -> R.string.settings_tab_appearance
    SettingsTab.Notifications -> R.string.settings_tab_notifications
    SettingsTab.Password -> R.string.settings_tab_password
    SettingsTab.Language -> R.string.settings_tab_language
}

@Composable
private fun TabHeading(title: String, description: String) {
    Column(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = HogwartsTheme.type.section, color = HogwartsTheme.colors.foreground)
        Text(description, style = HogwartsTheme.type.body, color = HogwartsTheme.colors.mutedForeground)
    }
}

/**
 * Appearance. The web tab is its preset colour-theme gallery, which the app's
 * fixed tokens can't apply; the app offers the header's light/dark mode here.
 */
@Composable
private fun AppearanceTab(mode: ThemeMode, onThemeMode: (ThemeMode) -> Unit) {
    TabHeading(stringResource(R.string.settings_appearance_title), stringResource(R.string.settings_appearance_description))
    GroupedList {
        ThemeMode.entries.forEachIndexed { index, option ->
            if (index > 0) RowDivider()
            GroupedRow(
                title = stringResource(option.labelRes()),
                icon = option.icon(),
                active = option == mode,
                onClick = { onThemeMode(option) },
            )
        }
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.Light -> R.string.settings_theme_light
    ThemeMode.Dark -> R.string.settings_theme_dark
    ThemeMode.System -> R.string.settings_theme_system
}

private fun ThemeMode.icon(): ImageVector = when (this) {
    ThemeMode.Light -> Icons.Outlined.LightMode
    ThemeMode.Dark -> Icons.Outlined.DarkMode
    ThemeMode.System -> Icons.Outlined.PhoneAndroid
}

/** Notifications. The web tab's form saves nowhere; the real preferences live on `/notifications/preferences`. */
@Composable
private fun NotificationsTab(onOpenNotificationPreferences: () -> Unit) {
    GroupedList {
        GroupedRow(
            title = stringResource(R.string.settings_notification_preferences),
            subtitle = stringResource(R.string.settings_notification_preferences_description),
            icon = Icons.Outlined.Notifications,
            chevron = true,
            onClick = onOpenNotificationPreferences,
        )
    }
}

/**
 * Password. No mobile API changes a signed-in user's password yet, and the
 * shell can't hand `/settings` to the web (it resolves to this screen), so
 * the tab says where to do it.
 */
@Composable
private fun PasswordTab() {
    Text(
        stringResource(R.string.settings_password_unavailable),
        style = HogwartsTheme.type.body,
        color = HogwartsTheme.colors.mutedForeground,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Language — the web's phone list: two rows on a grey panel, the current one marked. */
@Composable
private fun LanguageTab(language: String, onLanguage: (String) -> Unit) {
    TabHeading(stringResource(R.string.settings_language_preference), stringResource(R.string.settings_language_description))
    GroupedList {
        LanguageRow(flag = "🇸🇦", name = "العربية", note = "Arabic (RTL)", active = language == "ar") { onLanguage("ar") }
        RowDivider()
        LanguageRow(flag = "🇬🇧", name = "English", note = "English (LTR)", active = language == "en") { onLanguage("en") }
    }
}

@Composable
private fun LanguageRow(flag: String, name: String, note: String, active: Boolean, onClick: () -> Unit) {
    GroupedRow(
        title = name,
        subtitle = note,
        leading = { Text(flag, fontSize = 24.sp) },
        active = active,
        onClick = { if (!active) onClick() },
    )
}

/** `max-md:bg-muted max-md:rounded-xl max-md:divide-y` — the phone's grouped list. */
@Composable
private fun GroupedList(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .background(HogwartsTheme.colors.muted),
    ) { content() }
}

@Composable
private fun RowDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(HogwartsTheme.colors.border))
}

/** One `flex items-center gap-3 p-4` row, with the web's `Active` pill at the far end. */
@Composable
private fun GroupedRow(
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    icon: ImageVector? = null,
    leading: (@Composable () -> Unit)? = null,
    active: Boolean = false,
    chevron: Boolean = false,
    titleColor: androidx.compose.ui.graphics.Color = HogwartsTheme.colors.foreground,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Row(
        Modifier
            .fillMaxWidth()
            .semantics { selected = active }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = colors.foreground, modifier = Modifier.size(24.dp))
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            Text(title, style = type.body.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium), color = titleColor)
            if (subtitle != null) Text(subtitle, style = type.body, color = colors.mutedForeground)
        }
        if (active) {
            Text(
                stringResource(R.string.settings_active),
                style = type.caption,
                color = colors.primaryForeground,
                modifier = Modifier
                    .clip(HogwartsShapes.Pill)
                    .background(colors.primary)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        if (chevron) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.mutedForeground,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
