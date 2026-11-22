package org.hogwarts.android.feature.live.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import org.hogwarts.android.feature.live.R

/**
 * Keeps a class alive while the phone is out of sight.
 *
 * Without a foreground service Android silences the microphone as soon as the app goes to the
 * background (a teacher opening a PDF mid-class) and freezes the process a few minutes later, which
 * drops the room. This service runs for as long as the phone is connected to a class room: the
 * room's view-model starts it once connected and stops it on leave, on any disconnect and when it is
 * cleared. Type `microphone` while the microphone permission is held, `mediaPlayback` for a student
 * who only listens. It also holds a partial wake lock and a Wi-Fi lock so the audio keeps flowing
 * with the screen off.
 */
class LiveClassService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(EXTRA_TITLE)?.takeIf { it.isNotBlank() }
            ?: getString(R.string.live_service_title_fallback)
        createChannel()
        // A second start (the microphone permission arrived late) re-enters foreground with the new type.
        if (!enterForeground(buildNotification(title))) {
            stopSelf()
            return START_NOT_STICKY
        }
        acquireLocks()
        // Never restarted by the system: a restarted service would have no room behind it.
        return START_NOT_STICKY
    }

    /** The user swiped the app away: the room goes with it, so does the service. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    override fun onDestroy() {
        runCatching { wakeLock?.takeIf { it.isHeld }?.release() }
        wakeLock = null
        runCatching { wifiLock?.takeIf { it.isHeld }?.release() }
        wifiLock = null
        runCatching { ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE) }
        super.onDestroy()
    }

    private fun foregroundType(): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return 0
        val micGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        return if (micGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        } else {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        }
    }

    /** True once the service is in the foreground. Never throws: a refused start must not crash the room. */
    private fun enterForeground(notification: Notification): Boolean {
        val type = foregroundType()
        try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
            return true
        } catch (e: RuntimeException) {
            // SecurityException (type not allowed right now), ForegroundServiceStartNotAllowedException (API 31+,
            // an IllegalStateException) or a foreground-service-type exception (API 34+).
            Log.w(TAG, "startForeground(type=$type) refused", e)
        }
        if (type == ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE) {
            try {
                ServiceCompat.startForeground(
                    this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
                )
                return true
            } catch (e: RuntimeException) {
                Log.w(TAG, "startForeground(mediaPlayback) refused", e)
            }
        }
        return false
    }

    private fun acquireLocks() {
        if (wakeLock?.isHeld != true) {
            wakeLock = runCatching {
                getSystemService(PowerManager::class.java)
                    .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
                    .apply {
                        setReferenceCounted(false)
                        // A leak must not drain a phone: a class never runs this long.
                        acquire(WAKE_LOCK_TIMEOUT_MS)
                    }
            }.onFailure { Log.w(TAG, "wake lock not taken", it) }.getOrNull()
        }
        if (wifiLock?.isHeld != true) {
            wifiLock = runCatching {
                val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    WifiManager.WIFI_MODE_FULL_LOW_LATENCY
                } else {
                    @Suppress("DEPRECATION")
                    WifiManager.WIFI_MODE_FULL_HIGH_PERF
                }
                applicationContext.getSystemService(WifiManager::class.java)
                    .createWifiLock(mode, WAKE_LOCK_TAG)
                    .apply {
                        setReferenceCounted(false)
                        acquire()
                    }
            }.onFailure { Log.w(TAG, "wifi lock not taken", it) }.getOrNull()
        }
    }

    private fun createChannel() {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(getString(R.string.live_service_channel))
            .setShowBadge(false)
            .setVibrationEnabled(false)
            .setSound(null, null)
            .build()
        runCatching { NotificationManagerCompat.from(this).createNotificationChannel(channel) }
    }

    private fun buildNotification(title: String): Notification {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val tap = launch?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            // A flat, literal-colour vector of this module: the system draws the status-bar icon outside
            // the app's theme, and a notification it cannot draw crashes a foreground service's app.
            .setSmallIcon(R.drawable.ic_live_class)
            .setContentTitle(title)
            .setContentText(getString(R.string.live_service_text))
            .setContentIntent(tap)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        private const val TAG = "LiveClassService"
        private const val CHANNEL_ID = "live_class"
        private const val NOTIFICATION_ID = 7301
        private const val EXTRA_TITLE = "title"
        private const val WAKE_LOCK_TAG = "hogwarts:live-class"
        private const val WAKE_LOCK_TIMEOUT_MS = 4 * 60 * 60 * 1000L

        /** The class this service is running for, so one room's cleanup never stops another room's service. */
        @Volatile private var holder: String? = null

        /**
         * Starts (or refreshes) the service for [sessionId]; calling again after the microphone permission
         * arrives re-enters foreground with the microphone type. True when Android accepted the start;
         * never throws.
         */
        fun start(context: Context, sessionId: String, title: String): Boolean = try {
            // startService, NOT startForegroundService: the latter is a promise to call startForeground
            // within five seconds, and Android kills the whole app when the promise is broken — which is
            // exactly what a refused foreground type would do (onStartCommand stops itself then). The room
            // starts this while its screen is in front, where a plain start is allowed; from the
            // background it throws IllegalStateException, caught below, and the room simply goes on
            // without the service.
            context.startService(
                Intent(context, LiveClassService::class.java).putExtra(EXTRA_TITLE, title),
            )
            holder = sessionId
            true
        } catch (e: RuntimeException) {
            // IllegalStateException (started from the background) or SecurityException.
            Log.w(TAG, "the class service could not start", e)
            false
        }

        /** Stops the service if it is running for [sessionId]. */
        fun stop(context: Context, sessionId: String) {
            if (holder != sessionId) return
            holder = null
            runCatching { context.stopService(Intent(context, LiveClassService::class.java)) }
        }
    }
}
