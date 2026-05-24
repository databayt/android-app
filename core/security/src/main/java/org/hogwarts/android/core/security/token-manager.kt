package org.hogwarts.android.core.security

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.hogwarts.android.core.common.api.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages JWT tokens securely using EncryptedSharedPreferences.
 *
 * Implements TokenProvider interface for network layer.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenProvider {

    private companion object {
        const val PREFS_FILE_NAME = "hogwarts_secure_prefs"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_TOKEN_EXPIRY = "token_expiry"
        const val REFRESH_THRESHOLD_MS = 5 * 60 * 1000L // 5 minutes
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

    private val _isAuthenticated = MutableStateFlow(hasTokens)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    override val accessToken: String?
        get() = encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)

    override val refreshToken: String?
        get() = encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)

    override val hasTokens: Boolean
        get() = !accessToken.isNullOrEmpty()

    val tokenExpiry: Long
        get() = encryptedPrefs.getLong(KEY_TOKEN_EXPIRY, 0)

    /**
     * Check if token needs refresh.
     * Refreshes when less than 5 minutes remain before expiry.
     */
    val needsRefresh: Boolean
        get() {
            val expiry = tokenExpiry
            if (expiry == 0L) return false

            val now = System.currentTimeMillis()
            val remaining = expiry - now
            return remaining < REFRESH_THRESHOLD_MS
        }

    /**
     * Save tokens after successful authentication
     */
    fun saveTokens(accessToken: String, refreshToken: String?, expiryMillis: Long) {
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_TOKEN_EXPIRY, expiryMillis)
            .apply()

        _isAuthenticated.value = true
    }

    /**
     * Clear all tokens (on logout)
     */
    fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .apply()

        _isAuthenticated.value = false
    }

    /**
     * Called when API returns 401 Unauthorized
     */
    override fun onUnauthorized() {
        // Could trigger token refresh here, or clear tokens
        // For now, just clear and force re-login
        clearTokens()
    }
}
