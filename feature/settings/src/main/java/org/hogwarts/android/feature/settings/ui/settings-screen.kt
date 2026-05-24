package org.hogwarts.android.feature.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.wallpaper.WallpaperCatalog
import org.hogwarts.android.core.designsystem.wallpaper.WallpaperOption
import org.hogwarts.android.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.settings_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        AppleInsetGroupedList(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            state = listState
        ) {
            item {
                AppleListSection(header = stringResource(R.string.settings_section_account)) {
                    AppleListRow(
                        showDivider = false,
                        onClick = onNavigateToProfile
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.settings_profile), style = MaterialTheme.typography.bodyMedium)
                            Icon(
                                HogwartsIcons.Forward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                AppleListSection(header = stringResource(R.string.settings_section_appearance)) {
                    AppleListRow(
                        showDivider = true,
                        onClick = { viewModel.toggleThemePicker() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = when (uiState.themeMode) {
                                    "light" -> stringResource(R.string.settings_theme_light)
                                    "dark" -> stringResource(R.string.settings_theme_dark)
                                    else -> stringResource(R.string.settings_theme_system)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    AppleListRow(
                        showDivider = true,
                        onClick = { viewModel.toggleLanguagePicker() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = if (uiState.language == "ar") stringResource(R.string.settings_language_arabic) else stringResource(R.string.settings_language_english),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    AppleListRow(
                        showDivider = false,
                        onClick = { viewModel.toggleWallpaperPicker() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.settings_wallpaper), style = MaterialTheme.typography.bodyMedium)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                            ) {
                                val current = WallpaperCatalog.findById(uiState.wallpaperId)
                                Image(
                                    painter = painterResource(current.drawableRes),
                                    contentDescription = stringResource(R.string.settings_wallpaper_preview),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 22.dp, height = 32.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Text(
                                    text = stringResource(current.labelRes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                AppleListSection(header = stringResource(R.string.settings_section_notifications)) {
                    AppleListRow(showDivider = false) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.settings_push_notifications), style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = uiState.notificationsEnabled,
                                onCheckedChange = viewModel::onNotificationsToggled
                            )
                        }
                    }
                }
            }

            item {
                AppleListSection(header = stringResource(R.string.settings_section_about)) {
                    AppleListRow(showDivider = false) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.settings_version), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = uiState.appVersion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                AppleListSection {
                    AppleListRow(
                        showDivider = false,
                        onClick = { showLogoutDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                HogwartsIcons.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                stringResource(R.string.settings_logout),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.settings_logout_title)) },
            text = { Text(stringResource(R.string.settings_logout_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.settings_logout_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            }
        )
    }

    // Theme Picker Dialog
    if (uiState.showThemePicker) {
        val themeSystemDefault = stringResource(R.string.settings_theme_system_default)
        val themeLight = stringResource(R.string.settings_theme_light)
        val themeDark = stringResource(R.string.settings_theme_dark)

        AlertDialog(
            onDismissRequest = { viewModel.toggleThemePicker() },
            title = { Text(stringResource(R.string.settings_theme)) },
            text = {
                Column {
                    listOf("system" to themeSystemDefault, "light" to themeLight, "dark" to themeDark).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onThemeModeChanged(value)
                                    viewModel.toggleThemePicker()
                                }
                                .padding(vertical = AppleSpacing.Compact),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                        ) {
                            RadioButton(
                                selected = uiState.themeMode == value,
                                onClick = {
                                    viewModel.onThemeModeChanged(value)
                                    viewModel.toggleThemePicker()
                                }
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.toggleThemePicker() }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            }
        )
    }

    // Wallpaper Picker — full-bleed, vertically scrolling
    if (uiState.showWallpaperPicker) {
        Dialog(
            onDismissRequest = { viewModel.toggleWallpaperPicker() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text(stringResource(R.string.settings_wallpaper)) },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.toggleWallpaperPicker() }) {
                                Icon(
                                    HogwartsIcons.Close,
                                    contentDescription = stringResource(R.string.settings_cancel)
                                )
                            }
                        }
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(WallpaperCatalog.options, key = { it.id }) { option ->
                            WallpaperPickerCell(
                                option = option,
                                selected = option.id == uiState.wallpaperId,
                                onClick = {
                                    viewModel.onWallpaperChanged(option.id)
                                    viewModel.toggleWallpaperPicker()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Language Picker Dialog
    if (uiState.showLanguagePicker) {
        val langEnglish = stringResource(R.string.settings_language_english)
        val langArabic = stringResource(R.string.settings_language_arabic)

        AlertDialog(
            onDismissRequest = { viewModel.toggleLanguagePicker() },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                Column {
                    listOf("en" to langEnglish, "ar" to langArabic).forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onLanguageChanged(code)
                                    viewModel.toggleLanguagePicker()
                                }
                                .padding(vertical = AppleSpacing.Compact),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                        ) {
                            RadioButton(
                                selected = uiState.language == code,
                                onClick = {
                                    viewModel.onLanguageChanged(code)
                                    viewModel.toggleLanguagePicker()
                                }
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.toggleLanguagePicker() }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            }
        )
    }
}

@Composable
private fun WallpaperPickerCell(
    option: WallpaperOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(option.drawableRes),
            contentDescription = stringResource(option.labelRes),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom gradient + label so the name stays legible against any image.
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                    )
                )
                .padding(horizontal = AppleSpacing.Small, vertical = AppleSpacing.Compact)
        ) {
            Text(
                text = stringResource(option.labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(width = 3.dp, color = MaterialTheme.colorScheme.primary)
            )
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(AppleSpacing.Compact)
                    .size(24.dp)
            )
        }
    }
}
