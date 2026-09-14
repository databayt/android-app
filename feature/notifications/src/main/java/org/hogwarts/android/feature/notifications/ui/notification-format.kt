package org.hogwarts.android.feature.notifications.ui

import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong

/** Where a card leads: a locale-less web path the shell resolves, or a third-party page. */
sealed interface NotificationTarget {
    data class Path(val href: String) : NotificationTarget
    data class External(val url: String) : NotificationTarget
}

/** The school platform's hosts — hogwarts `lib/root-domain.ts`. */
private val ROOT_DOMAINS = listOf("databayt.org", "balqalam.com")
private val LOCALE_PREFIX = Regex("^/(en|ar)(?=/|$)")

/**
 * `card.tsx` `navigateToTarget`: stored URLs are relative now, but older rows
 * carry an absolute URL on one of our own hosts. Those reduce to their path;
 * only a genuinely third-party link leaves the app.
 */
internal fun notificationTarget(raw: String?): NotificationTarget? {
    val value = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val uri = runCatching { URI(value) }.getOrNull() ?: return null
    if (uri.isAbsolute) {
        val host = uri.host?.lowercase() ?: return null
        val ours = ROOT_DOMAINS.any { host == it || host.endsWith(".$it") }
        if (!ours) {
            return if (uri.scheme == "http" || uri.scheme == "https") NotificationTarget.External(value) else null
        }
    }
    val path = (uri.rawPath ?: "").let { if (it.startsWith("/")) it else "/$it" }
        .replace(LOCALE_PREFIX, "")
        .ifEmpty { "/" }
    return NotificationTarget.Path(path + (uri.rawQuery?.let { "?$it" } ?: ""))
}

/** date-fns `formatDistanceToNow` buckets, as the card shows them under a week. */
internal sealed interface Ago {
    data object LessThanMinute : Ago
    data class Minutes(val count: Int) : Ago
    data class Hours(val count: Int) : Ago
    data class Days(val count: Int) : Ago
    /** A week or older: the date itself. */
    data object Date : Ago
}

internal fun ago(created: Instant, now: Instant): Ago {
    val elapsed = Duration.between(created, now).coerceAtLeast(Duration.ZERO)
    if (elapsed.toDays() >= 7) return Ago.Date
    val minutes = (elapsed.seconds / 60.0).roundToLong()
    return when {
        minutes == 0L -> Ago.LessThanMinute
        minutes < 45 -> Ago.Minutes(minutes.toInt())
        minutes < 90 -> Ago.Hours(1)
        minutes < 1440 -> Ago.Hours((minutes / 60.0).roundToLong().toInt())
        minutes < 2520 -> Ago.Days(1)
        else -> Ago.Days((minutes / 1440.0).roundToLong().toInt())
    }
}

/** `format(date, ar ? "d MMMM yyyy" : "MMM d, yyyy")` — date-fns keeps Latin digits. */
internal fun notificationDate(created: Instant, language: String, zone: ZoneId = ZoneId.systemDefault()): String {
    val pattern = if (language == "ar") "d MMMM yyyy" else "MMM d, yyyy"
    return DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag(language)).withZone(zone).format(created)
}
