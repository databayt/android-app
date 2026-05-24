package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Atom Studio — catalog of atoms sourced from the iOS 26 Figma library.
 *
 * Only atoms with a direct Figma counterpart appear here. Non-Figma atoms
 * remain in the codebase for legacy feature use but are intentionally hidden
 * until migrated.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtomStudio(
    onNavigateBack: (() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    var email by remember { mutableStateOf("") }
    var rowFieldValue by remember { mutableStateOf("") }
    val listScrollState = rememberLazyListState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Atom Studio") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = AppleSpacing.Standard)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.ExtraLarge)
        ) {
            Spacer(Modifier.height(AppleSpacing.Compact))

            // 1. AppIcon — shown on a wallpaper-like dark bg to mirror iOS home screen
            AtomSection("AppIcon (1:3538)") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFF2C2C3E))
                        .padding(vertical = 16.dp)
                ) {
                    AppIconRow(
                        items = listOf(
                            AppIconItem(
                                label = "Messages",
                                iconUrl = "",
                                fallbackIcon = Icons.Outlined.Email,
                                fallbackBackground = androidx.compose.ui.graphics.Color(0xFF34C759)
                            ),
                            AppIconItem(
                                label = "Mail",
                                iconUrl = "",
                                fallbackIcon = Icons.Default.Email,
                                fallbackBackground = androidx.compose.ui.graphics.Color(0xFF007AFF)
                            ),
                            AppIconItem(
                                label = "Notes",
                                iconUrl = "",
                                fallbackIcon = Icons.Outlined.NoteAlt,
                                fallbackBackground = androidx.compose.ui.graphics.Color(0xFFFFCC00)
                            ),
                            AppIconItem(
                                label = "Reminders",
                                iconUrl = "",
                                fallbackIcon = Icons.Outlined.Notifications,
                                fallbackBackground = androidx.compose.ui.graphics.Color(0xFFFF3B30)
                            ),
                            AppIconItem(
                                label = "Books",
                                iconUrl = "",
                                fallbackIcon = Icons.Outlined.Book,
                                fallbackBackground = androidx.compose.ui.graphics.Color(0xFFFF9500)
                            )
                        )
                    )
                }
            }

            // 2. Tab Bar
            AtomSection("Tab Bar (12:2378)") {
                HogwartsTabBar(scrollState = listScrollState) {
                    TabCell(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = Icons.Default.Home, label = "Home")
                    TabCell(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = Icons.Default.Search, label = "Search")
                    TabCell(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = Icons.Default.Notifications, label = "Alerts")
                    TabCell(selected = selectedTab == 3, onClick = { selectedTab = 3 }, icon = Icons.Default.Person, label = "Profile")
                }
            }

            // 3. Empty State
            AtomSection("Empty State (17:3252)") {
                EmptyState(
                    icon = Icons.Default.Search,
                    title = "No Results",
                    subtitle = "Try a different search term.",
                    actionLabel = "Clear Search",
                    onAction = {}
                )
            }

            // 4. Liquid Glass (glass-card) — Figma uses fixed 160×160 except Small=48
            AtomSection("Liquid Glass (16:3092 / 3096 / 3099 / 3105)") {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    GlassCard(
                        variant = GlassVariant.RegularLarge,
                        modifier = Modifier.size(160.dp)
                    ) {
                        Box(Modifier.padding(AppleSpacing.Standard)) {
                            Text("Large", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    GlassCard(
                        variant = GlassVariant.RegularMedium,
                        modifier = Modifier.size(160.dp)
                    ) {
                        Box(Modifier.padding(AppleSpacing.Standard)) {
                            Text("Medium", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    GlassCard(
                        variant = GlassVariant.RegularSmall,
                        modifier = Modifier.size(48.dp)
                    ) {}
                    GlassCard(
                        variant = GlassVariant.Clear,
                        modifier = Modifier.size(160.dp)
                    ) {
                        Box(Modifier.padding(AppleSpacing.Standard)) {
                            Text("Clear", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // 5. Action Sheet
            AtomSection("Action Sheet (1:58)") {
                HogwartsActionSheet(
                    title = "Choose Action",
                    message = "Select an option below.",
                    actions = listOf(
                        ActionSheetAction("Delete", isDestructive = true) {},
                        ActionSheetAction("Edit") {},
                        ActionSheetAction("Share") {},
                        ActionSheetAction("Cancel") {}
                    )
                )
            }

            // 6. Alert
            AtomSection("Alert (7:2186)") {
                HogwartsAlert(
                    title = "Delete Item?",
                    message = "This action cannot be undone.",
                    primaryAction = "Delete",
                    onPrimaryAction = {},
                    secondaryAction = "Cancel",
                    onSecondaryAction = {}
                )
            }

            // 7. Overlay
            AtomSection("Overlay (16:3081)") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    HogwartsOverlay(isVisible = true)
                    Text(
                        "Overlay (scrim) above content",
                        modifier = Modifier.padding(AppleSpacing.Standard),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 8. Row / Text Field — iOS list row style (Figma 7:2112)
            AtomSection("Row – Text Field (7:2112)") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    HogwartsRowTextField(
                        title = "Title",
                        value = rowFieldValue,
                        onValueChange = { rowFieldValue = it },
                        placeholder = "Value"
                    )
                    HogwartsRowTextField(
                        title = "Email",
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "name@example.com"
                    )
                }
            }

            // 9. Notification — Collapsed
            AtomSection("Notification – Collapsed (4:1613)") {
                NotificationBanner(
                    title = "Hogwarts",
                    description = "Your attendance has been recorded.",
                    time = "9:41 AM",
                    icon = Icons.Default.School
                )
            }

            // 10. Sidebar Search Field — Figma has trailing mic icon
            AtomSection("Sidebar Search Field (4:1669)") {
                HogwartsSearchBar(
                    query = searchText,
                    onQueryChange = { searchText = it },
                    placeholder = "Search",
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            // 11. Header
            AtomSection("Header (17:3294)") {
                SectionHeader(title = "Students")
                SectionHeader(title = "Attendance", subtitle = "Today, 24 present")
            }

            Spacer(Modifier.height(AppleSpacing.ExtraLarge))
        }
    }
}

@Composable
private fun RowScope.TabCell(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    HogwartsTabItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        label = label,
        modifier = Modifier.weight(1f)
    )
}

@Composable
private fun AtomSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppleSpacing.Small)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AtomStudioPreview() {
    HogwartsTheme {
        AtomStudio()
    }
}
