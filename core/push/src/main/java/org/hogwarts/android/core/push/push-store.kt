package org.hogwarts.android.core.push

import android.content.Context
import androidx.core.content.edit
import androidx.core.content.pm.PackageInfoCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** What the push flow remembers between launches (small, so SharedPreferences rather than DataStore). */
@Singleton
class PushStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("hogwarts_push", Context.MODE_PRIVATE)

    /** When the user last answered « لاحقاً » or refused the system dialog; null = never answered. */
    var promptDeclinedAtMs: Long?
        get() = if (prefs.contains(KEY_DECLINED)) prefs.getLong(KEY_DECLINED, 0L) else null
        set(value) = prefs.edit { if (value == null) remove(KEY_DECLINED) else putLong(KEY_DECLINED, value) }

    /** The app versionCode that last got its token to the server; null = never. */
    var registeredVersionCode: Long?
        get() = if (prefs.contains(KEY_VERSION)) prefs.getLong(KEY_VERSION, 0L) else null
        set(value) = prefs.edit { if (value == null) remove(KEY_VERSION) else putLong(KEY_VERSION, value) }

    private companion object {
        const val KEY_DECLINED = "prompt_declined_at"
        const val KEY_VERSION = "registered_version_code"
    }
}

/** The installed app's versionCode (0 if the package manager cannot say). */
internal fun installedVersionCode(context: Context): Long = runCatching {
    PackageInfoCompat.getLongVersionCode(context.packageManager.getPackageInfo(context.packageName, 0))
}.getOrDefault(0L)
