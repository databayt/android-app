package org.hogwarts.android.feature.dashboard.ui

import org.hogwarts.android.feature.dashboard.data.repository.RefreshFailure

/** The one line (if any) the dashboard shows about the last refresh. */
enum class RefreshNotice {
    None,

    /** «غير متصل — تُعرض آخر نسخة محفوظة»: the phone really has no connection. */
    Offline,

    /** «ما قدرنا نحدّث — اسحب لتحت للتحديث»: online, but the refresh failed; neutral, content kept. */
    CouldNotRefresh,
}

/**
 * Which message to show. A refresh that succeeded ([lastFailure] null) shows nothing, whatever the
 * connectivity flag says — a working request is the best proof the phone is online (some devices never
 * mark a working network "validated"). After a failure, "offline" needs a connectivity failure or a
 * device without a validated network; a server error or timeout on an online phone is only a neutral note.
 */
fun refreshNotice(lastFailure: RefreshFailure?, deviceOnline: Boolean): RefreshNotice = when {
    lastFailure == null -> RefreshNotice.None
    lastFailure == RefreshFailure.Connectivity || !deviceOnline -> RefreshNotice.Offline
    else -> RefreshNotice.CouldNotRefresh
}

/** Wait before automatic retry number [attempt] (0-based): 5 s, 15 s, 45 s, then stop (null). */
fun retryDelayMs(attempt: Int): Long? = RETRY_DELAYS_MS.getOrNull(attempt)

private val RETRY_DELAYS_MS = listOf(5_000L, 15_000L, 45_000L)

/** A resume re-fetches after a failure almost at once, otherwise at most every 30 s. */
fun shouldRefreshOnResume(lastStartMs: Long, nowMs: Long, hadFailure: Boolean): Boolean {
    val since = nowMs - lastStartMs
    return if (hadFailure) since >= RESUME_AFTER_FAILURE_MS else since >= RESUME_MIN_GAP_MS
}

private const val RESUME_AFTER_FAILURE_MS = 5_000L
private const val RESUME_MIN_GAP_MS = 30_000L
