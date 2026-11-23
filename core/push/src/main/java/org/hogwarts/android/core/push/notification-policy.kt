package org.hogwarts.android.core.push

/**
 * The pure decisions behind the notification flow, kept free of Android types so they run on the JVM.
 */
object NotificationPolicy {
    /** A declined prompt is offered again after a week, never sooner. */
    const val REOFFER_AFTER_MS = 7L * 24 * 60 * 60 * 1000

    /** `Build.VERSION_CODES.TIRAMISU`: the first release with the runtime POST_NOTIFICATIONS grant. */
    const val RUNTIME_PERMISSION_SDK = 33

    /** `NotificationManager.IMPORTANCE_NONE`: a channel the user switched off. */
    private const val IMPORTANCE_NONE = 0

    /**
     * Whether to show the "turn on notifications" explanation (which leads to the system dialog).
     *
     * Only on Android 13+ and only while the permission is not granted. Never asked: yes. Declined
     * (« لاحقاً » or the system dialog's "Don't allow"): again only once a week has passed.
     */
    fun shouldShowPrompt(
        sdkInt: Int,
        permissionGranted: Boolean,
        lastDeclinedAtMs: Long?,
        nowMs: Long,
    ): Boolean {
        if (sdkInt < RUNTIME_PERMISSION_SDK) return false
        if (permissionGranted) return false
        if (lastDeclinedAtMs == null) return true
        return nowMs - lastDeclinedAtMs >= REOFFER_AFTER_MS
    }

    /**
     * What the dashboard banner should say, from the live system state:
     * [appEnabled] is `areNotificationsEnabled()`, [liveChannelImportance] is the live-class channel's
     * importance (null when the channel does not exist yet).
     */
    fun status(appEnabled: Boolean, liveChannelImportance: Int?): NotificationStatus = when {
        !appEnabled -> NotificationStatus.AppOff
        liveChannelImportance == IMPORTANCE_NONE -> NotificationStatus.LiveChannelOff
        else -> NotificationStatus.On
    }

    /**
     * Whether to send the FCM token again at app start: only when signed in, and only when the app
     * version differs from the one that last registered successfully (a failed or never-made
     * registration leaves it unset, so it retries on the next start). Sign-in, a rotated token and a
     * fresh permission grant register unconditionally, without asking this.
     */
    fun shouldRegisterOnStart(signedIn: Boolean, registeredVersionCode: Long?, currentVersionCode: Long): Boolean =
        signedIn && registeredVersionCode != currentVersionCode
}

enum class NotificationStatus { On, AppOff, LiveChannelOff }
