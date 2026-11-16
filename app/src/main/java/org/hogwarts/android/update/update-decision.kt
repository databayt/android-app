package org.hogwarts.android.update

/** What the update check asks of the screen. */
enum class UpdateDecision { None, Optional, Required }

/** An optional "update available" prompt is shown at most once per this long. */
const val OPTIONAL_PROMPT_INTERVAL_MS: Long = 24L * 60 * 60 * 1000

/** The version check is repeated at most this often (a cold start always checks). */
const val CHECK_INTERVAL_MS: Long = 30L * 60 * 1000

/**
 * Pure decision: [installed] below [min] must update; below [latest] may update, but only if the
 * optional prompt was not shown in the last 24 h ([lastOptionalShownMs], 0 = never). Never while
 * the user is in a live class room — blocking there would end the class.
 */
fun decideUpdate(
    installed: Int,
    latest: Int,
    min: Int,
    lastOptionalShownMs: Long,
    nowMs: Long,
    inRoom: Boolean,
): UpdateDecision = when {
    inRoom -> UpdateDecision.None
    installed < min -> UpdateDecision.Required
    installed < latest && (lastOptionalShownMs <= 0L || nowMs - lastOptionalShownMs >= OPTIONAL_PROMPT_INTERVAL_MS) ->
        UpdateDecision.Optional
    else -> UpdateDecision.None
}
