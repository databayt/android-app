package org.hogwarts.android.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages encrypted credential storage for biometric unlock.
 *
 * After a successful email/password login, credentials are stored encrypted.
 * When the user authenticates with biometrics, stored credentials are used
 * to re-authenticate with the backend (maintaining valid server sessions).
 */
@Singleton
class CredentialManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object {
        const val PREFS_FILE_NAME = "hogwarts_biometric_creds"
        const val KEY_EMAIL = "biometric_email"
        const val KEY_PASSWORD = "biometric_password"
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Whether biometric login has been set up with stored credentials.
     */
    val hasSavedCredentials: Boolean
        get() = encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false) &&
                !encryptedPrefs.getString(KEY_EMAIL, null).isNullOrEmpty()

    /**
     * Save credentials after successful login for biometric unlock.
     */
    fun saveCredentials(email: String, password: String) {
        encryptedPrefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .putBoolean(KEY_BIOMETRIC_ENABLED, true)
            .apply()
    }

    /**
     * Retrieve stored credentials for biometric re-authentication.
     */
    fun getCredentials(): Pair<String, String>? {
        val email = encryptedPrefs.getString(KEY_EMAIL, null) ?: return null
        val password = encryptedPrefs.getString(KEY_PASSWORD, null) ?: return null
        return email to password
    }

    /**
     * Clear stored credentials (on logout or biometric disable).
     */
    fun clearCredentials() {
        encryptedPrefs.edit()
            .remove(KEY_EMAIL)
            .remove(KEY_PASSWORD)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    /**
     * Whether biometric login is enabled by the user.
     */
    val isBiometricLoginEnabled: Boolean
        get() = encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    /**
     * Enable or disable biometric login preference (without clearing credentials).
     */
    fun setBiometricLoginEnabled(enabled: Boolean) {
        encryptedPrefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
    }
}
