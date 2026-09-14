package org.hogwarts.android.shell

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import timber.log.Timber

/**
 * Opens a school page on the web for destinations the app has no native
 * screen for (school configuration, sales, compliance, billing…), keeping the
 * menu an exact mirror of the web's.
 *
 * Pages are school-scoped by subdomain: `https://{domain}.balqalam.com/{lang}{href}`.
 */
object WebHandoff {
    const val ROOT_DOMAIN = "balqalam.com"

    fun url(schoolDomain: String?, lang: String, href: String): Uri {
        val host = if (schoolDomain.isNullOrBlank()) ROOT_DOMAIN else "$schoolDomain.$ROOT_DOMAIN"
        return Uri.parse("https://$host/$lang$href")
    }

    fun open(context: Context, uri: Uri) {
        try {
            CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, uri)
        } catch (e: ActivityNotFoundException) {
            Timber.w(e, "No Custom Tabs provider; falling back to a browser")
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
