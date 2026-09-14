package org.hogwarts.android.feature.messaging.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.messaging.ui.shell.MessagesShellNavigation
import org.hogwarts.android.feature.messaging.ui.shell.MessagesShellRoute
import org.hogwarts.android.feature.messaging.ui.thread.ThreadRoute

/** Web `/messages`: the five-tab shell. */
@Serializable data object Messaging

/** Web `/messages?conversation=<id>`: one thread, over the shell. */
@Serializable data class Chat(val conversationId: String)

/**
 * The Messages destinations. The shell's tab set is state inside [Messaging]
 * (the web keeps it in component state too); a thread is its own destination
 * so system back returns to the list, as the web's back disc does.
 *
 * @param onOpenDashboard the inbox's back disc and Settings → Dashboard.
 * @param onOpenHref Settings → Profile / Notifications, as web paths.
 */
fun NavGraphBuilder.messagesGraph(
    onOpenChat: (conversationId: String) -> Unit,
    onCloseChat: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenHref: (String) -> Unit,
) {
    composable<Messaging> {
        MessagesShellRoute(
            navigation = MessagesShellNavigation(
                onOpenChat = onOpenChat,
                onOpenDashboard = onOpenDashboard,
                onOpenHref = onOpenHref,
            ),
        )
    }
    composable<Chat> {
        ThreadRoute(onBack = onCloseChat)
    }
}
