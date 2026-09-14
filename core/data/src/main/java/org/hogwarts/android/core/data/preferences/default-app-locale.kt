package org.hogwarts.android.core.data.preferences

/**
 * The app speaks Arabic until told otherwise, like the web (`ar` is its
 * default locale). On the first launch with no per-app language chosen, the
 * app locale is set to Arabic — once: a later choice of English, or of the
 * system language in Android settings, is never overridden.
 *
 * The platform calls are passed in so the rule is testable on the JVM.
 */
class DefaultAppLocale(
    private val appLocalesEmpty: () -> Boolean,
    private val setAppLocale: (String) -> Unit,
    private val alreadyDefaulted: suspend () -> Boolean,
    private val markDefaulted: suspend (String) -> Unit,
) {
    /** Returns true when it applied the default. */
    suspend fun apply(): Boolean {
        if (!appLocalesEmpty()) return false
        if (alreadyDefaulted()) return false
        setAppLocale(DEFAULT_LANGUAGE)
        markDefaulted(DEFAULT_LANGUAGE)
        return true
    }

    companion object {
        const val DEFAULT_LANGUAGE = "ar"
    }
}
