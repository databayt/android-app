package org.hogwarts.android.feature.live.ui.room

/** What to do about one runtime permission (microphone or camera) right now. */
enum class PermissionState { GRANTED, ASK, RATIONALE, BLOCKED }

/**
 * [granted] wins. A system rationale means a denial the system will still let us ask
 * about again (explain first). A permission asked for before with no rationale left is
 * denied for good: only the app's settings can change it. Never asked: just ask.
 */
fun permissionState(granted: Boolean, askedBefore: Boolean, shouldShowRationale: Boolean): PermissionState = when {
    granted -> PermissionState.GRANTED
    shouldShowRationale -> PermissionState.RATIONALE
    askedBefore -> PermissionState.BLOCKED
    else -> PermissionState.ASK
}
