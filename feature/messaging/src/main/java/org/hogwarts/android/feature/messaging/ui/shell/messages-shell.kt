package org.hogwarts.android.feature.messaging.ui.shell

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.GlassCapsuleTab
import org.hogwarts.android.core.designsystem.kit.GlassCapsuleTabBar
import org.hogwarts.android.core.designsystem.kit.rememberGlassBackdrop
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.ui.chats.ChatListActions
import org.hogwarts.android.feature.messaging.ui.chats.ChatListModel
import org.hogwarts.android.feature.messaging.ui.chats.ChatListPane
import org.hogwarts.android.feature.messaging.ui.chats.RowLabels
import org.hogwarts.android.feature.messaging.ui.chats.filterChats
import org.hogwarts.android.feature.messaging.ui.chats.toRowData
import org.hogwarts.android.feature.messaging.ui.common.wa
import org.hogwarts.android.feature.messaging.ui.format.MessagingFormat
import org.hogwarts.android.feature.messaging.ui.tabs.CallsTab
import org.hogwarts.android.feature.messaging.ui.tabs.CommunitiesTab
import org.hogwarts.android.feature.messaging.ui.tabs.SettingsTab
import org.hogwarts.android.feature.messaging.ui.tabs.UpdatesTab

/** Where the shell leads outside Messages, as locale-less web paths. */
class MessagesShellNavigation(
    val onOpenChat: (String) -> Unit,
    /** The back disc and Settings → Dashboard: `router.push('/dashboard')`. */
    val onOpenDashboard: () -> Unit,
    /** `/profile`, `/notifications`. */
    val onOpenHref: (String) -> Unit,
)

@Composable
fun MessagesShellRoute(
    navigation: MessagesShellNavigation,
    viewModel: MessagesShellViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var noticeDismissed by rememberSaveable { mutableStateOf(context.noticeDismissed()) }
    var granted by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        noticeDismissed = true
        context.rememberNoticeDismissed()
    }
    MessagesShellContent(
        state = state,
        showNotice = !granted && !noticeDismissed,
        onSelectTab = viewModel::selectTab,
        chatActions = ChatListActions(
            onOpenChat = navigation.onOpenChat,
            onExit = navigation.onOpenDashboard,
            onQuery = viewModel::setQuery,
            onFilter = viewModel::setFilter,
            onStartSelecting = viewModel::startSelecting,
            onStopSelecting = viewModel::stopSelecting,
            onToggleSelected = viewModel::toggleSelected,
            onReadSelected = viewModel::readSelected,
            onReadAll = viewModel::readAll,
            onNoticeAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    noticeDismissed = true
                    context.rememberNoticeDismissed()
                }
            },
            onNoticeDismiss = {
                noticeDismissed = true
                context.rememberNoticeDismissed()
            },
        ),
        onOpenProfile = { navigation.onOpenHref("/profile") },
        onOpenNotifications = { navigation.onOpenHref("/notifications") },
        onOpenDashboard = navigation.onOpenDashboard,
    )
}

/**
 * Port of `ios-shell.tsx`: owns the current tab, draws the floating glass tab
 * bar once, and swaps the page beneath it. Pages are composed only once
 * opened and then kept (`Pane`), so each keeps its scroll across switches.
 */
@Composable
fun MessagesShellContent(
    state: ShellUiState,
    showNotice: Boolean,
    onSelectTab: (MessagesTab) -> Unit,
    chatActions: ChatListActions,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDashboard: () -> Unit,
    modifier: Modifier = Modifier,
    format: MessagingFormat = MessagingFormat(LocalConfiguration.current.locales[0]),
) {
    val backdrop = rememberGlassBackdrop()
    var opened by rememberSaveable { mutableStateOf(setOf(state.tab.name)) }
    if (state.tab.name !in opened) opened = opened + state.tab.name

    val rowLabels = RowLabels(
        groupFallback = stringResource(R.string.messages_group_fallback),
        directFallback = stringResource(R.string.messages_direct_fallback),
        yesterday = stringResource(R.string.messages_relative_yesterday),
    )
    val schoolName = state.viewer?.let {
        if (LocalConfiguration.current.locales[0].language == "en") it.schoolNameEn ?: it.schoolName else it.schoolName
    }

    Box(modifier.fillMaxSize().background(wa.surfacePrimary)) {
        fun shown(tab: MessagesTab) = state.tab == tab
        if (MessagesTab.Chats.name in opened) {
            Pane(shown(MessagesTab.Chats)) {
                ChatListPane(
                    model = ChatListModel(
                        rows = filterChats(state.chats, state.filter, state.query, rowLabels)
                            .map { it.toRowData(state.viewer?.username, format, rowLabels) },
                        // The list route leaves archived conversations out, so there is no count to show.
                        archivedCount = 0,
                        query = state.query,
                        filter = state.filter,
                        selecting = state.selecting,
                        selected = state.selected,
                        showNotice = showNotice,
                    ),
                    actions = chatActions,
                    backdrop = if (shown(MessagesTab.Chats)) backdrop else null,
                )
            }
        }
        if (MessagesTab.Updates.name in opened) Pane(shown(MessagesTab.Updates)) { UpdatesTab() }
        if (MessagesTab.Calls.name in opened) Pane(shown(MessagesTab.Calls)) { CallsTab() }
        if (MessagesTab.Communities.name in opened) {
            Pane(shown(MessagesTab.Communities)) {
                CommunitiesTab(schoolName = schoolName, chats = state.chats, onOpen = chatActions.onOpenChat)
            }
        }
        if (MessagesTab.Settings.name in opened) {
            Pane(shown(MessagesTab.Settings)) {
                SettingsTab(
                    name = state.viewer?.username.orEmpty(),
                    status = state.viewer?.bio,
                    avatarUrl = state.viewer?.avatarUrl,
                    onOpenProfile = onOpenProfile,
                    onOpenNotifications = onOpenNotifications,
                    onOpenChats = { onSelectTab(MessagesTab.Chats) },
                    onOpenDashboard = onOpenDashboard,
                )
            }
        }

        GlassCapsuleTabBar(
            tabs = listOf(
                GlassCapsuleTab(MessagesTab.Updates.name, stringResource(R.string.messages_tab_updates), painterResource(R.drawable.ic_wa_tab_updates_32), painterResource(R.drawable.ic_wa_tab_updates_fill_32)),
                GlassCapsuleTab(MessagesTab.Calls.name, stringResource(R.string.messages_tab_calls), painterResource(R.drawable.ic_wa_tab_calls_32), painterResource(R.drawable.ic_wa_tab_calls_fill_32)),
                GlassCapsuleTab(MessagesTab.Communities.name, stringResource(R.string.messages_tab_communities), painterResource(R.drawable.ic_wa_tab_communities_32), painterResource(R.drawable.ic_wa_tab_communities_fill_32)),
                GlassCapsuleTab(MessagesTab.Chats.name, stringResource(R.string.messages_tab_chats), painterResource(R.drawable.ic_wa_tab_chats_32), painterResource(R.drawable.ic_wa_tab_chats_fill_32), badge = state.totalUnread),
                GlassCapsuleTab(MessagesTab.Settings.name, stringResource(R.string.messages_tab_settings), painterResource(R.drawable.ic_wa_tab_settings_32), painterResource(R.drawable.ic_wa_tab_settings_fill_32)),
            ),
            selectedKey = state.tab.name,
            onSelect = { key -> onSelectTab(MessagesTab.valueOf(key)) },
            backdrop = if (state.tab == MessagesTab.Chats) backdrop else null,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** A tab page kept composed while hidden, the way `Pane` sets `display: none`. */
@Composable
private fun Pane(visible: Boolean, content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .then(if (visible) Modifier else Modifier.hiddenPane()),
    ) { content() }
}

/** Measured but never placed: not drawn, not touchable, state kept. */
private fun Modifier.hiddenPane(): Modifier = this
    .clearAndSetSemantics { }
    .layout { measurable, constraints ->
        measurable.measure(constraints)
        layout(0, 0) {}
    }

private const val NOTICE_PREFS = "messages_notice"
private const val NOTICE_DISMISSED = "dismissed"

/** `wa-notice-dismissed`: the card is per device, so the dismissal is too. */
private fun Context.noticeDismissed(): Boolean =
    getSharedPreferences(NOTICE_PREFS, Context.MODE_PRIVATE).getBoolean(NOTICE_DISMISSED, false)

private fun Context.rememberNoticeDismissed() =
    getSharedPreferences(NOTICE_PREFS, Context.MODE_PRIVATE).edit().putBoolean(NOTICE_DISMISSED, true).apply()
