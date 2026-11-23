package org.hogwarts.android.core.push

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_GENERAL = "hogwarts_general"
        const val CHANNEL_ATTENDANCE = "hogwarts_attendance"
        const val CHANNEL_GRADES = "hogwarts_grades"
        const val CHANNEL_FEES = "hogwarts_fees"
        const val CHANNEL_MESSAGES = "hogwarts_messages"
        const val CHANNEL_ANNOUNCEMENTS = "hogwarts_announcements"
        const val CHANNEL_LIVE_CLASS = "hogwarts_live"

        /**
         * The channel a notification type posts on, by prefix, matching the `channel_id` the server
         * sets on background pushes (`live_class_*` -> hogwarts_live) so foreground and background land together.
         */
        fun channelForType(type: String): String = when {
            type.startsWith("live_class_") -> CHANNEL_LIVE_CLASS
            type.startsWith("attendance") || type.startsWith("absence") -> CHANNEL_ATTENDANCE
            type == "grade" || type == "grade_posted" || type == "assignment_graded" -> CHANNEL_GRADES
            type.startsWith("fee") -> CHANNEL_FEES
            type.startsWith("message") -> CHANNEL_MESSAGES
            type == "announcement" -> CHANNEL_ANNOUNCEMENTS
            else -> CHANNEL_GENERAL
        }
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() = NotificationChannels.ensure(context)

    fun handleRemoteMessage(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: "general"
        val channelId = channelForType(type)

        val title = message.notification?.title ?: data["title"] ?: "Hogwarts"
        val body = message.notification?.body ?: data["body"] ?: ""

        showNotification(
            channelId = channelId,
            title = title,
            body = body,
            data = data
        )
    }

    private fun showNotification(
        channelId: String,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        val pendingIntent = launch?.let {
            PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .apply { pendingIntent?.let(::setContentIntent) }
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
