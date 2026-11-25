package org.hogwarts.android.update

/**
 * What the update check asks of the app. [Optional] is silent: no dialog, no "later" — the app
 * downloads and installs it on its own. [Required] keeps the blocking gate (it also installs by itself).
 */
enum class UpdateDecision { None, Optional, Required }

/** The version check is repeated at most this often (a cold start always checks). */
const val CHECK_INTERVAL_MS: Long = 30L * 60 * 1000

/**
 * Pure decision: [installed] below [min] must update; below [latest] updates quietly. Never while
 * the user is in a live class room — an install restarts the app and would end the class.
 */
fun decideUpdate(
    installed: Int,
    latest: Int,
    min: Int,
    inRoom: Boolean,
): UpdateDecision = when {
    inRoom -> UpdateDecision.None
    installed < min -> UpdateDecision.Required
    installed < latest -> UpdateDecision.Optional
    else -> UpdateDecision.None
}

/** The next step of the automatic update, given what is on disk and whether something is in flight. */
enum class UpdateAction { Nothing, Download, Install }

/**
 * Pure: with a decision other than None the app downloads the APK (no prompt), then installs it as
 * soon as it is verified. [busy] = a download or install is already running; [inRoom] holds everything.
 */
fun nextUpdateAction(
    decision: UpdateDecision,
    inRoom: Boolean,
    downloaded: Boolean,
    busy: Boolean,
): UpdateAction = when {
    inRoom || busy || decision == UpdateDecision.None -> UpdateAction.Nothing
    downloaded -> UpdateAction.Install
    else -> UpdateAction.Download
}

/** Where the installer's "confirm" screen goes when the system asks for the user. */
enum class ConfirmRoute { StartActivity, Notify }

/** Pure: open the confirm screen only in front of a user who is not in a class; otherwise one notification. */
fun routeConfirm(appInForeground: Boolean, inRoom: Boolean): ConfirmRoute =
    if (appInForeground && !inRoom) ConfirmRoute.StartActivity else ConfirmRoute.Notify
