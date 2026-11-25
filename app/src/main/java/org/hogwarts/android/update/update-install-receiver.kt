package org.hogwarts.android.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import org.hogwarts.android.core.common.brand.LiveRoomPresence
import java.util.Locale

/** True while an activity of the app is resumed (set by [UpdateViewModel]; false in the background). */
object UpdateForeground {
    @Volatile
    var resumed: Boolean = false
}

/**
 * Receives the [PackageInstaller] session result. A silent update finishes with STATUS_SUCCESS (the
 * process is replaced, so this rarely even runs). When the system needs the user (first update, or
 * an installer that is not ours) the confirm screen is shown only to a user outside a live class;
 * otherwise ONE notification waits for a tap. Failures are logged — the next app start retries.
 */
class UpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = confirmIntent(intent) ?: return
                when (routeConfirm(UpdateForeground.resumed, LiveRoomPresence.inRoom.value)) {
                    ConfirmRoute.StartActivity -> try {
                        context.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (e: RuntimeException) {
                        Log.w(TAG, "confirm screen blocked, notifying", e)
                        notifyReady(context, confirm)
                    }
                    ConfirmRoute.Notify -> notifyReady(context, confirm)
                }
            }
            PackageInstaller.STATUS_SUCCESS -> Unit
            else -> Log.w(
                TAG,
                "install failed status=$status message=${intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)}",
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun confirmIntent(intent: Intent): Intent? =
        if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            intent.getParcelableExtra(Intent.EXTRA_INTENT)
        }

    private fun notifyReady(context: Context, confirm: Intent) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                if (arabic()) "تحديث التطبيق" else "App update",
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        val tap = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = if (arabic()) "التحديث جاهز — اضغط للتثبيت" else "Update ready — tap to install"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentText(text)
            .setContentIntent(tap)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
        try {
            // Same id every time: ONE notification, however often the system asks again.
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "notifications not allowed", e)
        }
    }

    private fun arabic() = Locale.getDefault().language == "ar"

    companion object {
        const val ACTION = "org.hogwarts.android.update.INSTALL_RESULT"
        private const val TAG = "UpdateInstall"
        private const val CHANNEL_ID = "app_update"
        private const val NOTIFICATION_ID = 7301
    }
}
