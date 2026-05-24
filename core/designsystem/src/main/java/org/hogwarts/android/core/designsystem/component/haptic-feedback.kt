package org.hogwarts.android.core.designsystem.component

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

enum class HapticIntensity {
    LIGHT, MEDIUM, HEAVY
}

enum class HapticNotificationType {
    SUCCESS, WARNING, ERROR
}

class HapticFeedbackHelper(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val isHapticEnabled: Boolean
        get() = try {
            Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 1
        } catch (_: Exception) { true }

    fun impact(intensity: HapticIntensity = HapticIntensity.MEDIUM) {
        if (!isHapticEnabled) return
        val (amplitude, duration) = when (intensity) {
            HapticIntensity.LIGHT -> 50 to 10L
            HapticIntensity.MEDIUM -> 128 to 20L
            HapticIntensity.HEAVY -> 255 to 30L
        }
        vibrate(amplitude, duration)
    }

    fun notification(type: HapticNotificationType) {
        if (!isHapticEnabled) return
        when (type) {
            HapticNotificationType.SUCCESS -> {
                vibrate(128, 15)
                Thread.sleep(50)
                vibrate(200, 20)
            }
            HapticNotificationType.WARNING -> {
                vibrate(180, 20)
                Thread.sleep(80)
                vibrate(180, 20)
            }
            HapticNotificationType.ERROR -> {
                vibrate(255, 25)
                Thread.sleep(60)
                vibrate(255, 25)
                Thread.sleep(60)
                vibrate(255, 25)
            }
        }
    }

    fun selection() {
        if (!isHapticEnabled) return
        vibrate(30, 8)
    }

    private fun vibrate(amplitude: Int, duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
}

@Composable
fun rememberHapticFeedback(): HapticFeedbackHelper {
    val context = LocalContext.current
    return remember { HapticFeedbackHelper(context) }
}
