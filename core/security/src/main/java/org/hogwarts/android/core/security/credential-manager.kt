package org.hogwarts.android.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import org.hogwarts.android.core.common.api.RememberedLogin
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages encrypted credential storage: the remembered login and biometric unlock.
 *
 * After every successful password sign-in the login is remembered (encrypted), so a refused
 * refresh token costs a silent re-sign-in instead of a retyped PIN. Whether biometric unlock is
 * on is a separate flag over the same stored login.
 */
@Singleton
class CredentialManager internal constructor(
    private val encryptedPrefs: SharedPreferences
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(createPrefs(context))

    private companion object {
        const val PREFS_FILE_NAME = "hogwarts_biometric_creds"
        const val KEY_EMAIL = "biometric_email"
        const val KEY_PASSWORD = "biometric_password"
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        const val KEY_SCHOOL = "login_school"

        fun createPrefs(context: Context): SharedPreferences = openSecurePrefs(context, PREFS_FILE_NAME)
    }

    /**
     * Whether biometric login has been set up with stored credentials.
     */
    val hasSavedCredentials: Boolean
        get() = encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false) &&
                !encryptedPrefs.getString(KEY_EMAIL, null).isNullOrEmpty()

    /**
     * The school a login code last signed in to (its subdomain). Not a
     * secret, and kept across sign-outs: a teacher types it once.
     */
    var lastSchool: String?
        get() = encryptedPrefs.getString(KEY_SCHOOL, null)
        set(value) {
            encryptedPrefs.edit().putString(KEY_SCHOOL, value).apply()
        }

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
     * A password sign-in succeeded: remember the login on every phone. Biometric unlock is
     * switched on only where the hardware can offer it ([enableBiometric]).
     */
    fun rememberSignIn(identifier: String, password: String, school: String?, enableBiometric: Boolean) {
        val edit = encryptedPrefs.edit()
            .putString(KEY_EMAIL, identifier)
            .putString(KEY_PASSWORD, password)
        if (school != null) edit.putString(KEY_SCHOOL, school)
        if (enableBiometric) edit.putBoolean(KEY_BIOMETRIC_ENABLED, true)
        edit.apply()
    }

    /** The username kept for the login screen's prefill, even after the password is forgotten. */
    val rememberedIdentifier: String?
        get() = encryptedPrefs.getString(KEY_EMAIL, null)?.takeIf { it.isNotBlank() }

    /** The login a silent re-sign-in sends, or null without a stored password. */
    val rememberedLogin: RememberedLogin?
        get() {
            val identifier = rememberedIdentifier ?: return null
            val password = encryptedPrefs.getString(KEY_PASSWORD, null)?.takeIf { it.isNotEmpty() } ?: return null
            val school = if (identifier.contains('@')) null else lastSchool?.takeIf { it.isNotBlank() }
            return RememberedLogin(identifier, password, school)
        }

    /** The password stopped working: drop it (and biometric unlock with it), keep the username. */
    fun forgetPassword() {
        encryptedPrefs.edit()
            .remove(KEY_PASSWORD)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
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
     * Clear the whole remembered login (explicit sign-out): username, password, biometric.
     * The school stays: a teacher types it once.
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
