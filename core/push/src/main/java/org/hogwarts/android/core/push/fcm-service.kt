package org.hogwarts.android.core.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HogwartsFcmService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHandler: NotificationHandler

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM token refreshed")
        notificationHandler.onTokenRefreshed(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM message received: ${message.messageId}")
        notificationHandler.handleRemoteMessage(message)
    }
}
