package org.hogwarts.android.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Log

/**
 * Opens an encrypted preferences file, and never lets it crash the app.
 *
 * The file's keyset is sealed by a key in the phone's Android Keystore. When the file comes back
 * without that key — Android Auto Backup restoring it after a reinstall, a device transfer, a
 * Keystore reset — opening it throws (AEADBadTagException / KeyStoreException), and because this
 * runs while the app starts, the app closed right after its logo on every launch, reinstall or not
 * (two Al-Ibda staff, 2026-11-23/25). A file we can't read is worthless: delete it and start
 * empty — the person signs in again instead of being locked out of the app.
 */
fun openSecurePrefs(context: Context, name: String): SharedPreferences {
    fun create(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            name,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }
    return try {
        create()
    } catch (e: Exception) {
        Log.w("SecurePrefs", "Encrypted prefs $name unreadable (restored without its key?), resetting", e)
        context.deleteSharedPreferences(name)
        try {
            create()
        } catch (again: Exception) {
            // The Keystore itself is broken on this phone: run without persistence rather than crash.
            Log.e("SecurePrefs", "Encrypted prefs $name still unavailable, using a non-persisted store", again)
            context.getSharedPreferences("${name}_volatile", Context.MODE_PRIVATE).also { it.edit().clear().apply() }
        }
    }
}
