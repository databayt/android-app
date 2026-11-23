package org.hogwarts.android.core.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import org.hogwarts.android.core.push.NotificationHandler.Companion.CHANNEL_ANNOUNCEMENTS
import org.hogwarts.android.core.push.NotificationHandler.Companion.CHANNEL_ATTENDANCE
import org.hogwarts.android.core.push.NotificationHandler.Companion.CHANNEL_FEES
import org.hogwarts.android.core.push.NotificationHandler.Companion.CHANNEL_GENERAL
import org.hogwarts.android.core.push.NotificationHandler.Companion.CHANNEL_GRADES
import org.hogwarts.android.core.push.NotificationHandler.Companion.CHANNEL_LIVE_CLASS
import org.hogwarts.android.core.push.NotificationHandler.Companion.CHANNEL_MESSAGES

/**
 * The channels pushes are posted on. Created at app start (not on the first message) so the
 * system settings page and [NotificationSettings] can always open them. Channel ids are
 * permanent: Android keeps the user's settings per id, so never rename one.
 *
 * (`live_class` is a different channel: the quiet foreground-service notification of a class in
 * progress, owned by feature:live. [CHANNEL_LIVE_CLASS] is the alert: "starting soon", make-up
 * classes, schedule changes.)
 */
object NotificationChannels {
    fun ensure(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channels = listOf(
            NotificationChannel(CHANNEL_GENERAL, "General", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "General notifications" },
            NotificationChannel(CHANNEL_ATTENDANCE, "Attendance", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Attendance alerts" },
            NotificationChannel(CHANNEL_GRADES, "Grades", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Grade updates" },
            NotificationChannel(CHANNEL_FEES, "Fees", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Fee reminders" },
            NotificationChannel(CHANNEL_MESSAGES, "Messages", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Chat messages" },
            NotificationChannel(CHANNEL_ANNOUNCEMENTS, "Announcements", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "School announcements" },
            // IMPORTANCE_HIGH: heads-up + sound, the whole point of a "class starts in 10 minutes" push.
            NotificationChannel(
                CHANNEL_LIVE_CLASS,
                context.getString(R.string.push_channel_live_class),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = context.getString(R.string.push_channel_live_class_desc) },
        )
        channels.forEach { manager.createNotificationChannel(it) }
    }
}
