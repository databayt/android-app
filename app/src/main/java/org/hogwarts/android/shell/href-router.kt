package org.hogwarts.android.shell

import androidx.compose.runtime.staticCompositionLocalOf
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.announcements.navigation.AnnouncementDetail
import org.hogwarts.android.feature.dashboard.navigation.Dashboard
import org.hogwarts.android.feature.events.navigation.EventsList
import org.hogwarts.android.feature.guardian.navigation.GuardianChildren
import org.hogwarts.android.feature.messaging.navigation.Messaging
import org.hogwarts.android.feature.notifications.navigation.NotificationPreferences
import org.hogwarts.android.feature.notifications.navigation.Notifications
import org.hogwarts.android.feature.notifications.navigation.NotificationsUnread

/**
 * Opens a web path (`/attendance`, `/finance/invoice`, `/messages`…) the way
 * the web would: a native screen when one mirrors it, else the page itself.
 * Provided by [AppShell]; screens that carry server-given links use it.
 */
fun interface HrefOpener {
    fun open(href: String)
}

val LocalHrefOpener = staticCompositionLocalOf<HrefOpener> { HrefOpener { } }

/** Native route for a locale-less web path, or null to hand off to the web. */
internal fun routeForHref(href: String, role: UserRole?): Any? {
    val path = href.substringBefore('?').substringBefore('#').trimEnd('/').ifEmpty { "/dashboard" }
    when (path) {
        "/dashboard" -> return Dashboard
        "/notifications" -> return Notifications
        "/notifications/unread" -> return NotificationsUnread
        "/notifications/preferences" -> return NotificationPreferences
        "/messages" -> return Messaging
        "/events" -> return EventsList
        "/parents" -> return if (role == UserRole.GUARDIAN) GuardianChildren else null
    }
    nestedRoute(path)?.let { return it }
    // Only a menu page itself opens natively. A deeper page (/exams/result,
    // /announcements/templates) is a different screen: it hands off to the web
    // unless [nestedRoute] names the native screen that mirrors it.
    val item = platformNav.firstOrNull { path == it.href } ?: return null
    return nativeRoute(item.key, role)
}

/** Web sub-pages with a native mirror, matched segment by segment. */
private fun nestedRoute(path: String): Any? {
    val segments = path.trimStart('/').split('/')
    return when {
        segments.size == 2 && segments[0] == "announcements" && segments[1] !in ANNOUNCEMENT_WEB_PAGES ->
            AnnouncementDetail(segments[1])
        else -> null
    }
}

/** Writer pages under /announcements that stay on the web. */
private val ANNOUNCEMENT_WEB_PAGES = setOf("add", "templates", "archived", "settings", "config")
