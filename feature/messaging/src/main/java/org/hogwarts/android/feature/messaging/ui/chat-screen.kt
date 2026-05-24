package org.hogwarts.android.feature.messaging.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors
import org.hogwarts.android.core.designsystem.theme.WhatsAppTheme
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.domain.model.ConversationType
import org.hogwarts.android.feature.messaging.ui.component.ConversationOverflowMenu
import org.hogwarts.android.feature.messaging.ui.component.DateSeparator
import org.hogwarts.android.feature.messaging.ui.component.EmojiPickerSheet
import org.hogwarts.android.feature.messaging.ui.component.MessageActionSheet
import org.hogwarts.android.feature.messaging.ui.component.MessageActionSheetState
import org.hogwarts.android.feature.messaging.ui.component.MessageBubble
import org.hogwarts.android.feature.messaging.ui.component.MessageInput
import org.hogwarts.android.feature.messaging.ui.component.OnlineIndicator
import org.hogwarts.android.feature.messaging.ui.component.ScrollToBottomFab
import org.hogwarts.android.feature.messaging.ui.component.TypingIndicator
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onNavigateToInfo: (String) -> Unit = {},
) {
    WhatsAppTheme {
        val uiState by viewModel.uiState.collectAsState()
        val waColors = LocalWhatsAppColors.current
        val listState = rememberLazyListState()
        val messages = uiState.messages
        val reversedMessages = remember(messages) { messages.reversed() }
        val isGroupChat = uiState.conversationType.let {
            it == ConversationType.GROUP.name || it == ConversationType.CLASS.name
        }
        val conversationId = remember(uiState.messages) { uiState.messages.firstOrNull()?.conversationId ?: "" }

        val context = LocalContext.current
        LaunchedEffect(uiState.toastResId) {
            val id = uiState.toastResId ?: return@LaunchedEffect
            Toast.makeText(context, context.getString(id), Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }

        // Track scroll position
        val isAtBottom by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset < 50
            }
        }
        LaunchedEffect(isAtBottom) {
            if (isAtBottom) viewModel.onScrolledToBottom() else viewModel.onScrolledAway()
        }

        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.messaging_ui_back),
                            )
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                UserAvatar(name = uiState.conversationTitle, size = 36.dp)
                                OnlineIndicator(
                                    isOnline = uiState.isOtherOnline,
                                    modifier = Modifier.align(Alignment.BottomEnd),
                                    size = 10.dp,
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = uiState.conversationTitle.ifEmpty {
                                        stringResource(R.string.messaging_ui_chat_title)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                // Subtitle: typing or presence
                                val subtitle = when {
                                    uiState.typingUsers.isNotEmpty() -> {
                                        if (isGroupChat) {
                                            stringResource(
                                                R.string.messaging_ui_typing,
                                                uiState.typingUsers.joinToString(", "),
                                            )
                                        } else {
                                            stringResource(R.string.messaging_ui_is_typing)
                                        }
                                    }
                                    uiState.isOtherOnline -> stringResource(R.string.messaging_ui_online)
                                    uiState.lastSeenAt != null ->
                                        stringResource(R.string.messaging_ui_last_seen, uiState.lastSeenAt!!)
                                    else -> null
                                }
                                subtitle?.let {
                                    Text(
                                        text = it,
                                        fontSize = 12.sp,
                                        color = if (uiState.isOtherOnline || uiState.typingUsers.isNotEmpty())
                                            waColors.surfaceProduct
                                        else waColors.textSecondary,
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        if (uiState.isWhatsAppEnabled) {
                            Text(
                                text = "W",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = waColors.textProduct,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                        }
                        ConversationOverflowMenu(
                            isPinned = uiState.isPinned,
                            isMuted = uiState.isMuted,
                            onPinToggle = viewModel::onTogglePin,
                            onMuteToggle = viewModel::onToggleMute,
                            onArchive = viewModel::onRequestArchive,
                            onLeave = viewModel::onRequestLeave,
                            onViewInfo = {
                                if (conversationId.isNotEmpty()) onNavigateToInfo(conversationId)
                            },
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = waColors.surfacePanel,
                    ),
                )
            },
            bottomBar = {
                MessageInput(
                    text = uiState.messageText,
                    onTextChanged = viewModel::onMessageTextChanged,
                    onSend = viewModel::onSendMessage,
                    isSending = uiState.isSending,
                    replyTo = uiState.replyToMessage,
                    onCancelReply = viewModel::onCancelReply,
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(waColors.surfacePrimary),
            ) {
                if (uiState.isLoading && messages.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (messages.isEmpty()) {
                    EmptyState(
                        icon = HogwartsIcons.Messages,
                        title = stringResource(R.string.messaging_no_messages_title),
                        subtitle = stringResource(R.string.messaging_no_messages_subtitle),
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        reverseLayout = true,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 8.dp, vertical = 8.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        // Typing indicator at bottom (first item in reverse)
                        if (uiState.typingUsers.isNotEmpty()) {
                            item(key = "typing") {
                                TypingIndicator(
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                                )
                            }
                        }

                        itemsIndexed(
                            items = reversedMessages,
                            key = { _, msg -> msg.id },
                        ) { index, message ->
                            val isOwnMessage = message.senderId == uiState.currentUserId
                            val prevMessage = reversedMessages.getOrNull(index + 1)
                            val showSenderName = prevMessage == null ||
                                prevMessage.senderId != message.senderId

                            // Date separator when day changes
                            val prevDate = prevMessage?.sentAt
                                ?.atZone(ZoneId.systemDefault())?.toLocalDate()
                            val currentDate = message.sentAt
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            if (prevDate == null || prevDate != currentDate) {
                                DateSeparator(date = message.sentAt)
                            }

                            MessageBubble(
                                message = message,
                                isOwnMessage = isOwnMessage,
                                showSenderName = showSenderName,
                                isGroupChat = isGroupChat,
                                currentUserId = uiState.currentUserId,
                                onLongPress = viewModel::onMessageLongPress,
                                onReactionToggle = viewModel::onToggleReaction,
                            )
                        }
                    }
                }

                // Scroll to bottom FAB
                AnimatedVisibility(
                    visible = !isAtBottom,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 8.dp),
                ) {
                    ScrollToBottomFab(
                        onClick = {
                            viewModel.onScrolledToBottom()
                            // Scroll is handled via LaunchedEffect below
                        },
                        unreadCount = uiState.newUnreadCount,
                    )
                }

                // Auto-scroll when new messages arrive and at bottom
                LaunchedEffect(messages.size) {
                    if (isAtBottom && messages.isNotEmpty()) {
                        listState.animateScrollToItem(0)
                    }
                }
            }
        }

        // Action sheet (long-press on a bubble)
        uiState.actionTarget?.let { target ->
            val canEdit = target.senderId == uiState.currentUserId &&
                !target.isDeleted &&
                Duration.between(target.sentAt, Instant.now()).toMinutes() < 15
            val canDelete = target.senderId == uiState.currentUserId && !target.isDeleted
            MessageActionSheet(
                state = MessageActionSheetState(
                    canEdit = canEdit,
                    canDelete = canDelete,
                    isStarred = target.id in uiState.starredMessageIds,
                ),
                onDismiss = viewModel::onDismissActionSheet,
                onReply = { viewModel.onReplyToMessage(target) },
                onQuickReact = { emoji -> viewModel.onToggleReaction(target, emoji) },
                onMoreReactions = { viewModel.onOpenEmojiPicker(target) },
                onCopy = { viewModel.onDismissActionSheet() },
                onForward = { viewModel.onDismissActionSheet() },
                onEdit = { viewModel.onBeginEdit(target) },
                onDelete = { viewModel.onConfirmDelete(target) },
                onToggleStar = { viewModel.onToggleStar(target) },
                onPin = { viewModel.onDismissActionSheet() },
            )
        }

        // Emoji picker (More reactions)
        uiState.emojiPickerTarget?.let { target ->
            EmojiPickerSheet(
                title = stringResource(R.string.messaging_actions_react),
                onDismiss = viewModel::onDismissEmojiPicker,
                onPick = { emoji -> viewModel.onToggleReaction(target, emoji) },
            )
        }

        // Delete confirmation
        uiState.confirmingDelete?.let {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = viewModel::onDismissDelete,
                title = { Text(stringResource(R.string.messaging_confirmation_delete_message)) },
                text = { Text(stringResource(R.string.messaging_confirmation_delete_message_description)) },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = viewModel::onDeleteConfirmed) {
                        Text(
                            text = stringResource(R.string.messaging_actions_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = viewModel::onDismissDelete) {
                        Text(stringResource(R.string.messaging_actions_cancel))
                    }
                },
            )
        }

        // Archive confirmation
        if (uiState.confirmingArchive) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = viewModel::onDismissArchive,
                title = { Text(stringResource(R.string.messaging_confirmation_archive_conversation)) },
                text = { Text(stringResource(R.string.messaging_confirmation_archive_conversation_description)) },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        viewModel.onArchiveConfirmed()
                        onNavigateBack()
                    }) {
                        Text(stringResource(R.string.messaging_actions_archive))
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = viewModel::onDismissArchive) {
                        Text(stringResource(R.string.messaging_actions_cancel))
                    }
                },
            )
        }

        // Leave confirmation
        if (uiState.confirmingLeave) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = viewModel::onDismissLeave,
                title = { Text(stringResource(R.string.messaging_confirmation_leave_conversation)) },
                text = { Text(stringResource(R.string.messaging_confirmation_leave_conversation_description)) },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        viewModel.onLeaveConfirmed()
                        onNavigateBack()
                    }) {
                        Text(
                            text = stringResource(R.string.messaging_actions_leave),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = viewModel::onDismissLeave) {
                        Text(stringResource(R.string.messaging_actions_cancel))
                    }
                },
            )
        }
    }
}
