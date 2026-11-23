package org.hogwarts.android.core.push

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/** Reads the live system state of this app's notifications and opens the right settings page. */
object NotificationSettings {

    /** Checked on every resume: the user can flip it in system settings at any time. */
    fun status(context: Context): NotificationStatus {
        val manager = NotificationManagerCompat.from(context)
        return NotificationPolicy.status(
            appEnabled = manager.areNotificationsEnabled(),
            liveChannelImportance = manager.getNotificationChannel(NotificationHandler.CHANNEL_LIVE_CLASS)?.importance,
        )
    }

    /** The page that fixes [status]: the live-class channel's when only it is off, else the app's. */
    fun open(context: Context, status: NotificationStatus) {
        val primary = when (status) {
            NotificationStatus.LiveChannelOff -> Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, NotificationHandler.CHANNEL_LIVE_CLASS)
            else -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        for (intent in listOf(primary, fallback)) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (_: ActivityNotFoundException) {
                // try the next page
            } catch (_: SecurityException) {
            }
        }
    }
}
