package org.hogwarts.android.core.common.api

/**
 * The browser hand-off rides on the link: `https://<school>.balqalam.com/<lang>/<path>?app_login=<code>`.
 * [split] takes the in-app path (locale already stripped) and the raw query, and returns the href to
 * route (every other query parameter kept) plus the one-time code.
 */
object AppLoginLink {
    const val PARAM = "app_login"

    data class Parsed(val href: String, val code: String?)

    fun split(basePath: String, encodedQuery: String?): Parsed {
        var code: String? = null
        val kept = mutableListOf<String>()
        for (pair in encodedQuery.orEmpty().split('&').filter { it.isNotEmpty() }) {
            if (pair.substringBefore('=') == PARAM) {
                if (code == null) code = decode(pair.substringAfter('=', "")).takeIf { it.isNotBlank() }
            } else {
                kept += pair
            }
        }
        return Parsed(if (kept.isEmpty()) basePath else basePath + "?" + kept.joinToString("&"), code)
    }

    private fun decode(value: String): String =
        try {
            java.net.URLDecoder.decode(value.replace("+", "%2B"), "UTF-8")
        } catch (_: IllegalArgumentException) {
            value
        }
}
