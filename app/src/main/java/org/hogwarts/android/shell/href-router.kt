package org.hogwarts.android.shell

import androidx.compose.runtime.staticCompositionLocalOf
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.dashboard.navigation.Dashboard
import org.hogwarts.android.feature.events.navigation.EventsList
import org.hogwarts.android.feature.guardian.navigation.GuardianChildren
import org.hogwarts.android.feature.messaging.navigation.Messaging
import org.hogwarts.android.feature.notifications.navigation.Notifications

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
        "/messages" -> return Messaging
        "/events" -> return EventsList
        "/parents" -> return if (role == UserRole.GUARDIAN) GuardianChildren else null
    }
    // Longest menu href that prefixes the path wins, as the web's PageNav scores it.
    val item = platformNav
        .filter { path == it.href || path.startsWith(it.href + "/") }
        .maxByOrNull { it.href.length }
        ?: return null
    return nativeRoute(item.key, role)
}
