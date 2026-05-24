package org.hogwarts.android.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for biometric authentication.
 *
 * Usage:
 * ```kotlin
 * biometricHelper.authenticate(
 *     activity = this,
 *     onSuccess = { /* unlock app */ },
 *     onError = { message -> /* show error */ }
 * )
 * ```
 */
@Singleton
class BiometricHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val biometricManager = BiometricManager.from(context)

    /**
     * Check if biometric authentication is available
     */
    val isBiometricAvailable: Boolean
        get() = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }

    /**
     * Check if any biometric hardware exists
     */
    val hasBiometricHardware: Boolean
        get() = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> true
            else -> false
        }

    /**
     * Get reason why biometric is not available
     */
    fun getBiometricStatus(): BiometricStatus {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.SecurityUpdateRequired
            else -> BiometricStatus.Unknown
        }
    }

    /**
     * Show biometric authentication prompt
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock Hogwarts",
        subtitle: String = "Use your fingerprint or face to unlock",
        negativeButtonText: String = "Use password",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        if (!isBiometricAvailable) {
            onError("Biometric authentication is not available")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        // User chose to use password instead
                        onError("Authentication cancelled")
                    }
                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        onError("Too many attempts. Try again later.")
                    }
                    else -> {
                        onError(errString.toString())
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

/**
 * Biometric availability status
 */
sealed class BiometricStatus {
    data object Available : BiometricStatus()
    data object NoHardware : BiometricStatus()
    data object HardwareUnavailable : BiometricStatus()
    data object NoneEnrolled : BiometricStatus()
    data object SecurityUpdateRequired : BiometricStatus()
    data object Unknown : BiometricStatus()
}
