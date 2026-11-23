package org.hogwarts.android.core.push

import android.content.Intent

/**
 * The page a push notification opens. The server puts a web path in `data.deep_link`
 * (`/live/join/<timetableId>`, `/live/<sessionId>`, `/live/<sessionId>/cover`); the tapped
 * notification carries it as an extra on the launch intent (the system adds the FCM data of a
 * background notification-message the same way), and the app's href router takes it from there.
 */
object PushDeepLink {
    const val EXTRA = "deep_link"

    fun fromIntent(intent: Intent?): String? = fromExtras { key -> intent?.getStringExtra(key) }

    fun fromExtras(read: (String) -> String?): String? = normalize(read(EXTRA))

    /**
     * A locale-less web path, or null. Accepts `/live/x` and `/ar/live/x`; anything that is not a
     * plain in-app path (empty, a URL, `//host`) is ignored so a push can never open arbitrary targets.
     */
    fun normalize(raw: String?): String? {
        val value = raw?.trim().orEmpty()
        if (!value.startsWith("/") || value.startsWith("//")) return null
        val segments = value.substringBefore('#').split('/').filter { it.isNotEmpty() }
        if (segments.isEmpty()) return null
        val rest = if (segments.first() in LOCALES) segments.drop(1) else segments
        if (rest.isEmpty()) return null
        val query = value.substringBefore('#').substringAfter('?', "").let { if (it.isEmpty()) "" else "?$it" }
        return "/" + rest.joinToString("/").substringBefore('?') + query
    }

    private val LOCALES = setOf("ar", "en")
}
