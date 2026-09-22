package org.hogwarts.android.core.designsystem.component

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The absolute web address of a locale-less school path (`/subjects/x`), on
 * the school's own subdomain and in the UI language — what a screen hands to
 * the share sheet when the web shares its own URL. Provided by the shell.
 */
fun interface WebLink {
    fun url(href: String): String
}

val LocalWebLink = staticCompositionLocalOf<WebLink> { WebLink { it } }
