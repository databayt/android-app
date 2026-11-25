package org.hogwarts.android.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
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
        // Silent path first: a PackageInstaller session. Any failure falls back to the visible system installer.
        if (installViaSession(file)) return InstallLaunch.Launched
        return launchView(file)
    }

    private fun installViaSession(file: File): Boolean {
        val installer = context.packageManager.packageInstaller
        var sessionId = -1
        return try {
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                setAppPackageName(context.packageName)
                setSize(file.length())
                if (Build.VERSION.SDK_INT >= 31) {
                    setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
                if (Build.VERSION.SDK_INT >= 34) setRequestUpdateOwnership(true)
            }
            sessionId = installer.createSession(params)
            installer.openSession(sessionId).use { session ->
                file.inputStream().use { input ->
                    session.openWrite("base.apk", 0, file.length()).use { out ->
                        input.copyTo(out)
                        session.fsync(out)
                    }
                }
                val callback = Intent(context, UpdateInstallReceiver::class.java)
                    .setAction(UpdateInstallReceiver.ACTION)
                // Mutable on 31+: the system fills in the status extras. Explicit component, so it is safe on 34.
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0)
                val pending = PendingIntent.getBroadcast(context, sessionId, callback, flags)
                session.commit(pending.intentSender)
            }
            true
        } catch (e: Exception) {
            Log.w("ApkInstaller", "session install failed, falling back to the system installer", e)
            if (sessionId != -1) runCatching { installer.abandonSession(sessionId) }
            false
        }
    }

    private fun launchView(file: File): InstallLaunch = try {
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

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
