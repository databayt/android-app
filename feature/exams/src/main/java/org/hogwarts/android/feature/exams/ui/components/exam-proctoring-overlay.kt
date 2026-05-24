package org.hogwarts.android.feature.exams.ui.components

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.hogwarts.android.feature.exams.domain.model.ViolationType

/**
 * Proctoring overlay that monitors for exam violations.
 *
 * Attaches to the Activity lifecycle to detect:
 * - FLAG_SECURE: prevents screenshots and screen recording
 * - App-switch monitoring: detects when the user leaves the app (onPause/onStop)
 * - 3-strike warning system: calls [onViolation] for each detected violation
 *
 * Usage:
 * ```kotlin
 * ExamProctoringOverlay(
 *     enabled = true,
 *     onViolation = { type, description ->
 *         viewModel.reportViolation(type, description)
 *     }
 * )
 * ```
 */
@Composable
fun ExamProctoringOverlay(
    enabled: Boolean,
    onViolation: (ViolationType, String) -> Unit
) {
    if (!enabled) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasBeenResumedOnce by remember { mutableStateOf(false) }
    var appSwitchCount by remember { mutableIntStateOf(0) }

    // FLAG_SECURE: prevent screenshots and screen recording
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // App-switch monitoring via lifecycle observer
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (hasBeenResumedOnce) {
                        // Returning from an app switch
                        appSwitchCount++
                        onViolation(
                            ViolationType.APP_SWITCH,
                            "App switch detected (violation #$appSwitchCount). Student left the exam app."
                        )
                    }
                    hasBeenResumedOnce = true
                }
                Lifecycle.Event.ON_PAUSE -> {
                    if (hasBeenResumedOnce) {
                        // The user is leaving the app
                        // Violation will be reported on ON_RESUME
                    }
                }
                else -> { /* no-op */ }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
