package org.hogwarts.android.core.push

import android.app.NotificationChannel
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
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "General notifications" },
            NotificationChannel(
                CHANNEL_ATTENDANCE,
                "Attendance",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Attendance alerts" },
            NotificationChannel(
                CHANNEL_GRADES,
                "Grades",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Grade updates" },
            NotificationChannel(
                CHANNEL_FEES,
                "Fees",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Fee reminders" },
            NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Chat messages" },
            NotificationChannel(
                CHANNEL_ANNOUNCEMENTS,
                "Announcements",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "School announcements" },
        )

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    fun onTokenRefreshed(token: String) {
        Timber.d("FCM token should be sent to backend")
        // TODO: Send token to backend via API
    }

    fun handleRemoteMessage(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: "general"
        val channelId = when (type) {
            "attendance" -> CHANNEL_ATTENDANCE
            "grade" -> CHANNEL_GRADES
            "fee" -> CHANNEL_FEES
            "message" -> CHANNEL_MESSAGES
            "announcement" -> CHANNEL_ANNOUNCEMENTS
            else -> CHANNEL_GENERAL
        }

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
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
