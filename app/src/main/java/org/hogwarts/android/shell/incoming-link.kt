package org.hogwarts.android.shell

import android.content.Intent
import android.net.Uri

/**
 * A school page the app was opened with: `https://<school>.balqalam.com/<lang>/live/<id>/room`
 * from the web room's "open in the app" on Android. The web's link carries the locale; the
 * app's router takes locale-less paths ([routeForHref]).
 */
object IncomingLink {
    fun path(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_VIEW) return null
        return path(intent.data ?: return null)
    }

    fun path(uri: Uri): String? = path(uri.scheme, uri.host, uri.path)

    internal fun path(scheme: String?, host: String?, path: String?): String? {
        if (scheme != "https" || host == null || path == null) return null
        if (host != "balqalam.com" && !host.endsWith(".balqalam.com")) return null
        val segments = path.split('/').filter { it.isNotEmpty() }
        val rest = if (segments.firstOrNull() in LOCALES) segments.drop(1) else segments
        return "/" + rest.joinToString("/")
    }

    private val LOCALES = setOf("ar", "en")
}
