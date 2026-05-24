package org.hogwarts.android.feature.messaging.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.messaging.ui.ChatScreen
import org.hogwarts.android.feature.messaging.ui.ContactsScreen
import org.hogwarts.android.feature.messaging.ui.ConversationInfoScreen
import org.hogwarts.android.feature.messaging.ui.MessageSearchScreen
import org.hogwarts.android.feature.messaging.ui.WhatsAppChatListRoute
import org.hogwarts.android.feature.messaging.ui.whatsapp.WhatsAppQRScreen
import org.hogwarts.android.feature.messaging.ui.whatsapp.WhatsAppSettingsDialog

@Serializable data object Messaging
@Serializable data class Chat(val conversationId: String)
@Serializable data class ConversationInfo(val conversationId: String)
@Serializable data object MessageSearch
@Serializable data class ConversationMessageSearch(val conversationId: String)
@Serializable data object WhatsAppSettings
@Serializable data object WhatsAppQR

fun NavGraphBuilder.messagingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToWhatsAppSettings: () -> Unit,
) {
    composable<Messaging> {
        WhatsAppChatListRoute(
            onNavigateBack = onNavigateBack,
            onNavigateToChat = onNavigateToChat,
            onNavigateToWhatsAppSettings = onNavigateToWhatsAppSettings,
        )
    }
}

fun NavGraphBuilder.chatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInfo: (String) -> Unit = {},
) {
    composable<Chat> {
        ChatScreen(onNavigateBack = onNavigateBack, onNavigateToInfo = onNavigateToInfo)
    }
}

fun NavGraphBuilder.conversationInfoScreen(
    onNavigateBack: () -> Unit,
    onAfterExit: () -> Unit = onNavigateBack,
) {
    composable<ConversationInfo> {
        ConversationInfoScreen(
            onNavigateBack = onNavigateBack,
            onAfterExit = onAfterExit,
        )
    }
}

fun NavGraphBuilder.messageSearchScreen(
    onNavigateBack: () -> Unit,
    onResultClick: (conversationId: String, messageId: String) -> Unit,
) {
    composable<MessageSearch> {
        MessageSearchScreen(
            onNavigateBack = onNavigateBack,
            onResultClick = onResultClick,
        )
    }
    composable<ConversationMessageSearch> {
        MessageSearchScreen(
            onNavigateBack = onNavigateBack,
            onResultClick = onResultClick,
        )
    }
}

fun NavGraphBuilder.whatsAppSettingsScreen(
    onDismiss: () -> Unit,
    onNavigateToQR: () -> Unit,
) {
    composable<WhatsAppSettings> {
        WhatsAppSettingsDialog(
            onDismiss = onDismiss,
            onNavigateToQR = onNavigateToQR,
        )
    }
}

fun NavGraphBuilder.whatsAppQRScreen(
    onNavigateBack: () -> Unit,
) {
    composable<WhatsAppQR> {
        WhatsAppQRScreen(onNavigateBack = onNavigateBack)
    }
}
