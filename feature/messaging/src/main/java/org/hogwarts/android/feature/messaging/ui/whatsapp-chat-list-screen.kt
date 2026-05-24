package org.hogwarts.android.feature.messaging.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors
import org.hogwarts.android.core.designsystem.theme.WhatsAppColors

/// iOS WhatsApp-style chat list screen (Android).
/// Mirrors Figma: HqgFh4Lxp8QtTnW04czQQN node 124:1406
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppChatListScreen(
    rows: List<WaChatRowData>,
    archivedCount: Int = 0,
    onRowClick: (String) -> Unit = {},
    onNewChat: () -> Unit = {},
    onCamera: () -> Unit = {},
    onOptions: () -> Unit = {},
    onOpenArchived: () -> Unit = {},
    onBackToDashboard: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val wa = LocalWhatsAppColors.current
    var search by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(WaFilter.All) }
    var activeTab by remember { mutableStateOf(WaTab.Chats) }

    val filtered = rows.filter { row ->
        val matchesFilter = when (activeFilter) {
            WaFilter.All -> true
            WaFilter.Unread -> row.unreadCount > 0
            WaFilter.Favourites -> row.pinned
            WaFilter.Groups -> row.isGroup
        }
        if (!matchesFilter) return@filter false
        if (search.isBlank()) true else row.name.contains(search, ignoreCase = true)
    }
    val totalUnread = rows.sumOf { it.unreadCount }

    Box(modifier.fillMaxSize().background(wa.surfacePrimary)) {
        Column(Modifier.fillMaxSize()) {
            WaHeader(onOptions, onCamera, onNewChat, wa)

            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                item { WaTitleBlock(search, { search = it }, activeFilter, { activeFilter = it }, wa) }
                item { WaArchivedRow(archivedCount, onOpenArchived, wa) }
                items(filtered, key = { it.id }) { row ->
                    WaChatRow(row, onClick = { onRowClick(row.id) }, wa)
                }
                item { Spacer(Modifier.height(18.dp)) }
                item { WaInfoEncrypt(wa) }
                item { Spacer(Modifier.height(32.dp)) }
            }

            WaTabbar(
                active = activeTab,
                unreadCount = totalUnread,
                onSelect = { tab ->
                    if (tab == WaTab.Back) onBackToDashboard() else activeTab = tab
                },
                wa = wa,
            )
        }
    }
}

enum class WaFilter(val label: String) { All("All"), Unread("Unread"), Favourites("Favourites"), Groups("Groups") }
enum class WaTab(val label: String) { Calls("Calls"), Classes("Classes"), Chats("Chats"), Back("Back") }

data class WaChatRowData(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val avatarFallback: String,
    val isGroup: Boolean = false,
    val online: Boolean = false,
    val preview: String,
    val previewLeading: WaPreviewLeading? = null,
    val previewItalic: Boolean = false,
    val timestamp: String,
    val unreadCount: Int = 0,
    val mentioned: Boolean = false,
    val pinned: Boolean = false,
    val muted: Boolean = false,
)

enum class WaPreviewLeading { CheckRead, CheckSent, Voice, Location, Deleted }

@Composable
private fun WaHeader(
    onOptions: () -> Unit,
    onCamera: () -> Unit,
    onAdd: () -> Unit,
    wa: WhatsAppColors,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(wa.surfacePanelBlur),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularButton(onClick = onOptions, background = wa.surfaceCtaCircular) {
                Icon(Icons.Default.MoreHoriz, contentDescription = "Options", tint = wa.textPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.weight(1f))
            CircularButton(onClick = onCamera, background = wa.surfaceCtaCircular) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = wa.textPrimary, modifier = Modifier.size(18.dp))
            }
            CircularButton(onClick = onAdd, background = wa.surfaceProduct) {
                Icon(Icons.Default.Add, contentDescription = "New chat", tint = wa.textInvert, modifier = Modifier.size(20.dp))
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.33.dp)
                .background(wa.borderPanel),
        )
    }
}

@Composable
private fun CircularButton(
    onClick: () -> Unit,
    background: Color,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(28.dp)
            .background(background, shape = CircleShape),
    ) { content() }
}

@Composable
private fun WaTitleBlock(
    search: String,
    onSearchChange: (String) -> Unit,
    activeFilter: WaFilter,
    onFilterChange: (WaFilter) -> Unit,
    wa: WhatsAppColors,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "Chats",
            color = wa.textPrimary,
            fontSize = 33.33.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1.3332).sp,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            Modifier
                .fillMaxWidth()
                .background(wa.surfaceSearchChat, shape = RoundedCornerShape(10.dp))
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = wa.textSecondary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(1.dp))
            Box(Modifier.weight(1f)) {
                if (search.isEmpty()) {
                    Text(stringResource(R.string.messaging_search_messages), color = wa.textSecondary, fontSize = 16.4.sp)
                }
                BasicTextField(value = search, onValueChange = onSearchChange)
            }
        }
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WaFilter.values().forEach { f ->
                FilterChipCustom(
                    label = f.label,
                    active = f == activeFilter,
                    onClick = { onFilterChange(f) },
                    wa = wa,
                )
            }
            FilterChipIconOnly(onClick = {}, wa = wa)
        }
    }
}

@Composable
private fun FilterChipCustom(label: String, active: Boolean, onClick: () -> Unit, wa: WhatsAppColors) {
    Box(
        Modifier
            .background(
                if (active) wa.surfaceCtaFiltersActive else wa.surfaceCtaFilters,
                shape = RoundedCornerShape(19.dp),
            )
            .padding(horizontal = 14.dp)
            .height(34.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (active) wa.textCtaFiltersActive else wa.textCtaFilters,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 19.sp,
            letterSpacing = (-0.14).sp,
        )
    }
}

@Composable
private fun FilterChipIconOnly(onClick: () -> Unit, wa: WhatsAppColors) {
    Box(
        Modifier
            .size(34.dp)
            .background(wa.surfaceCtaFilters, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add filter", tint = wa.textCtaFilters, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun WaArchivedRow(count: Int, onClick: () -> Unit, wa: WhatsAppColors) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Default.Archive, contentDescription = null, tint = wa.textSecondary, modifier = Modifier.size(24.dp))
        Column(
            Modifier
                .weight(1f)
                .padding(bottom = 12.dp, end = 15.dp, top = 2.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.messaging_archived), color = wa.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (count > 0) Text("$count", color = wa.textSecondary, fontSize = 14.sp)
            }
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.33.dp)
                    .background(wa.borderSeparator),
            )
        }
    }
}

@Composable
private fun WaChatRow(row: WaChatRowData, onClick: () -> Unit, wa: WhatsAppColors) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.66.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Avatar with presence dot
        Box(Modifier.size(56.dp).padding(top = 2.dp)) {
            Box(
                Modifier
                    .size(56.dp)
                    .background(
                        if (row.isGroup) Brush.linearGradient(listOf(Color(0xFFB1B5C0), Color(0xFF858992))) else Brush.linearGradient(listOf(wa.textSecondary.copy(alpha = 0.9f), wa.textSecondary.copy(alpha = 0.9f))),
                        shape = CircleShape,
                    )
                    .border(BorderStroke(0.33.dp, wa.borderAvatar), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (row.isGroup) {
                    Icon(Icons.Outlined.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                } else {
                    Text(row.avatarFallback, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (row.online) {
                Box(
                    Modifier
                        .size(14.dp)
                        .offset(x = 42.dp, y = 42.dp)
                        .background(wa.surfaceProduct, shape = CircleShape)
                        .border(BorderStroke(2.dp, wa.surfacePrimary), CircleShape),
                )
            }
        }

        Column(
            Modifier
                .weight(1f)
                .padding(end = 15.dp)
                .height(67.33.dp),
            verticalArrangement = Arrangement.spacedBy(1.5.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    row.name,
                    color = wa.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.32).sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                Text(
                    row.timestamp,
                    color = if (row.unreadCount > 0) wa.textProduct else wa.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    letterSpacing = (-0.14).sp,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                WaPreviewText(row, wa, modifier = Modifier.weight(1f))
                WaRowTrailingBadges(row, wa)
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.33.dp)
                    .background(wa.borderSeparator),
            )
        }
    }
}

@Composable
private fun WaPreviewText(row: WaChatRowData, wa: WhatsAppColors, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        val (leadingIcon, leadingTint) = when (row.previewLeading) {
            WaPreviewLeading.CheckRead -> Icons.Default.Check to wa.textProduct
            WaPreviewLeading.CheckSent -> Icons.Default.Check to wa.textSecondary
            WaPreviewLeading.Voice -> Icons.Default.Mic to wa.textSecondary
            WaPreviewLeading.Location -> Icons.Default.LocationOn to wa.textSecondary
            WaPreviewLeading.Deleted -> Icons.Default.Block to wa.textSecondary
            null -> null to wa.textSecondary
        }
        if (leadingIcon != null) {
            val isCheck = row.previewLeading == WaPreviewLeading.CheckRead || row.previewLeading == WaPreviewLeading.CheckSent
            Box(Modifier.size(if (isCheck) 19.dp else 16.dp)) {
                Icon(leadingIcon, contentDescription = null, tint = leadingTint, modifier = Modifier.size(if (isCheck) 14.dp else 12.dp).align(Alignment.Center))
                if (isCheck) {
                    Icon(leadingIcon, contentDescription = null, tint = leadingTint, modifier = Modifier.size(14.dp).offset(x = 4.dp).align(Alignment.Center))
                }
            }
            Spacer(Modifier.width(2.dp))
        }
        Text(
            row.preview,
            color = wa.textSecondary,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            letterSpacing = (-0.14).sp,
            fontStyle = if (row.previewItalic) FontStyle.Italic else FontStyle.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun WaRowTrailingBadges(row: WaChatRowData, wa: WhatsAppColors) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (row.mentioned) {
            Text(
                "@",
                color = wa.textProduct,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
            )
        }
        if (row.unreadCount > 0) {
            Box(
                Modifier
                    .background(wa.surfaceProduct, shape = CircleShape)
                    .padding(horizontal = 6.dp)
                    .widthIn(min = 18.dp)
                    .height(18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("${row.unreadCount}", color = wa.textInvert, fontSize = 12.sp)
            }
        } else if (row.pinned) {
            Icon(
                Icons.Default.PushPin,
                contentDescription = "Pinned",
                tint = wa.textSecondary,
                modifier = Modifier.size(16.dp).rotate(45f),
            )
        }
    }
}

@Composable
private fun WaInfoEncrypt(wa: WhatsAppColors) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(vertical = 1.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = wa.textSecondary, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.messaging_personal_messages_prefix), color = wa.textSecondary, fontSize = 11.sp, lineHeight = 11.sp)
        Text(stringResource(R.string.messaging_e2e_encrypted), color = wa.textProduct, fontSize = 11.sp, lineHeight = 11.sp)
    }
}

@Composable
private fun WaTabbar(
    active: WaTab,
    unreadCount: Int,
    onSelect: (WaTab) -> Unit,
    wa: WhatsAppColors,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(wa.surfacePanelBlur),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.33.dp)
                .background(wa.borderPanel),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 23.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            listOf(
                Triple(WaTab.Calls, Icons.Default.Phone, null as Int?),
                Triple(WaTab.Classes, Icons.Outlined.Groups, null),
                Triple(WaTab.Chats, Icons.Default.Chat, unreadCount.takeIf { it > 0 }),
                Triple(WaTab.Back, Icons.Default.Settings, null),
            ).forEach { (tab, icon, badge) ->
                val color = if (tab == active) wa.textTabbarSelected else wa.textTabbar
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = { onSelect(tab) }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                            Text(tab.label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (badge != null) {
                        Box(
                            Modifier
                                .offset(x = 8.dp, y = (-2).dp)
                                .background(wa.surfaceProduct, shape = CircleShape)
                                .padding(horizontal = 6.dp)
                                .widthIn(min = 18.dp)
                                .height(18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("$badge", color = wa.textInvert, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        // Home indicator (5 × 140 pill)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 21.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .width(140.dp)
                    .height(5.dp)
                    .background(wa.surfaceInvert, shape = RoundedCornerShape(100.dp)),
            )
        }
    }
}

/**
 * Route wrapper used by the nav graph. Binds the existing ContactsViewModel and maps
 * the contact stream into WaChatRowData for the iOS WhatsApp chat-list UI.
 */
@Composable
fun WhatsAppChatListRoute(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToWhatsAppSettings: () -> Unit,
    viewModel: ContactsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val rows = remember(uiState.groups, uiState.contacts) {
        val source = if (uiState.contacts.isNotEmpty()) uiState.contacts else uiState.groups.flatMap { it.contacts }
        source.map { c ->
            WaChatRowData(
                id = c.conversationId ?: c.id,
                name = c.displayName,
                avatarUrl = c.avatarUrl,
                avatarFallback = c.displayName.take(1).uppercase(),
                isGroup = false,
                online = false,
                preview = c.lastMessage ?: "",
                previewLeading = null,
                previewItalic = c.isTyping,
                timestamp = c.lastMessageAt?.let { java.time.format.DateTimeFormatter.ofPattern("HH:mm").withZone(java.time.ZoneId.systemDefault()).format(it) } ?: "",
                unreadCount = c.unreadCount,
                mentioned = false,
                pinned = c.isPinned,
                muted = c.isMuted,
            )
        }
    }
    WhatsAppChatListScreen(
        rows = rows,
        onRowClick = { id -> onNavigateToChat(id) },
        onOptions = onNavigateToWhatsAppSettings,
        onBackToDashboard = onNavigateBack,
    )
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun WhatsAppChatListScreenLightPreview() {
    WhatsAppChatListScreen(rows = sampleRows)
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun WhatsAppChatListScreenDarkPreview() {
    // Note: dark mode picker requires CompositionLocalProvider(LocalWhatsAppColors provides WhatsAppColors.dark)
    WhatsAppChatListScreen(rows = sampleRows)
}

private val sampleRows = listOf(
    WaChatRowData(id = "c1", name = "Jenny ❤️", avatarFallback = "J", online = true, preview = "You reacted 😘 to \"That's good advice, Marty.\"", timestamp = "16:14", pinned = true),
    WaChatRowData(id = "c2", name = "Mom 💕", avatarFallback = "M", preview = "is typing...", previewItalic = true, timestamp = "19:45", unreadCount = 1, mentioned = true),
    WaChatRowData(id = "c3", name = "Daddy", avatarFallback = "D", preview = "I mean he wrecked it! 😭", previewLeading = WaPreviewLeading.CheckRead, timestamp = "19:42"),
    WaChatRowData(id = "c4", name = "Biff Tannen", avatarFallback = "B", preview = "Say hi to your mom for me.", timestamp = "18:23"),
    WaChatRowData(id = "c5", name = "Clocktower Lady", avatarFallback = "C", preview = "Save the clock tower?", previewLeading = WaPreviewLeading.Voice, timestamp = "16:15"),
    WaChatRowData(id = "c6", name = "Mr. Strickland", avatarFallback = "M", preview = "You deleted this message.", previewLeading = WaPreviewLeading.Deleted, previewItalic = true, timestamp = "08:57"),
    WaChatRowData(id = "c7", name = "Emmett \"Doc\" Brown", avatarFallback = "E", preview = "Location", previewLeading = WaPreviewLeading.Location, timestamp = "08:24"),
    WaChatRowData(id = "c8", name = "Dave", avatarFallback = "D", preview = "Thanks bro!", timestamp = "08:01"),
    WaChatRowData(id = "c9", name = "Lynda", avatarFallback = "L", preview = "Ok!", previewLeading = WaPreviewLeading.CheckSent, timestamp = "Yesterday"),
    WaChatRowData(id = "c10", name = "The time travelers ⏰", avatarFallback = "T", isGroup = true, preview = "Titor: ...until the clock hits 2:17 AM, March 14th, 2036.", timestamp = "Yesterday"),
)
