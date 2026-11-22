package org.hogwarts.android.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The provider that exposes `cacheDir/updates/` to the system installer (authority
 * `<applicationId>.updates`, paths in `res/xml/update_paths.xml`). A subclass of its own so it can
 * never collide with another library's `androidx.core.content.FileProvider` entry in the merged manifest.
 */
class UpdateFileProvider : FileProvider()

/** What launching the system installer did. */
enum class InstallLaunch {
    Launched,

    /** "Install unknown apps" is off for this app: send the user to its settings page. */
    NeedsPermission,

    /** No activity handled the intent (or the URI could not be built): fall back to the browser link. */
    NoInstaller,
}

/** Hands a verified APK to the system package installer. */
@Singleton
class ApkInstaller @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** minSdk is 26, so `canRequestPackageInstalls()` is always available. */
    fun canInstall(): Boolean = context.packageManager.canRequestPackageInstalls()

    fun launch(file: File): InstallLaunch {
        if (!canInstall()) return InstallLaunch.NeedsPermission
        return try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", file)
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, APK_MIME_TYPE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            InstallLaunch.Launched
        } catch (_: ActivityNotFoundException) {
            InstallLaunch.NoInstaller
        } catch (_: IllegalArgumentException) {
            // getUriForFile: file outside the configured paths.
            InstallLaunch.NoInstaller
        } catch (_: SecurityException) {
            InstallLaunch.NeedsPermission
        }
    }

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
