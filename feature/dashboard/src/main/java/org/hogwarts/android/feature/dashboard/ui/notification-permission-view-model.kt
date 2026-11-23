package org.hogwarts.android.feature.dashboard.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.hogwarts.android.core.push.DeviceTokenRegistrar
import org.hogwarts.android.core.push.NotificationPolicy
import org.hogwarts.android.core.push.NotificationSettings
import org.hogwarts.android.core.push.NotificationStatus
import org.hogwarts.android.core.push.PushStore
import javax.inject.Inject

/** What the dashboard shows about notifications: the one-time explain sheet, and the "they're off" banner. */
data class NotificationUiState(
    val status: NotificationStatus = NotificationStatus.On,
    val showPrompt: Boolean = false,
)

@HiltViewModel
class NotificationPermissionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val store: PushStore,
    private val registrar: DeviceTokenRegistrar,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state.asStateFlow()

    // Set while the sheet is up or the system dialog is open, so a resume mid-request does not re-offer.
    private var promptHandled = false

    /** On every resume: the user may have flipped the setting while the app was in the background. */
    fun onResume(nowMs: Long = System.currentTimeMillis()) {
        val status = NotificationSettings.status(context)
        val show = !promptHandled && NotificationPolicy.shouldShowPrompt(
            sdkInt = Build.VERSION.SDK_INT,
            permissionGranted = permissionGranted(),
            lastDeclinedAtMs = store.promptDeclinedAtMs,
            nowMs = nowMs,
        )
        if (show) promptHandled = true
        _state.value = NotificationUiState(status = status, showPrompt = show || _state.value.showPrompt)
    }

    /** «لاحقاً»: remember it, ask again in a week. */
    fun onLater(nowMs: Long = System.currentTimeMillis()) {
        store.promptDeclinedAtMs = nowMs
        _state.value = _state.value.copy(showPrompt = false)
    }

    /** The sheet is dismissed because the system dialog is about to open. */
    fun onAllowTapped() {
        _state.value = _state.value.copy(showPrompt = false)
    }

    /** The system dialog answered. A grant sends the token at once; a refusal counts as declined. */
    fun onPermissionResult(granted: Boolean, nowMs: Long = System.currentTimeMillis()) {
        if (granted) {
            store.promptDeclinedAtMs = null
            registrar.registerCurrentToken()
        } else {
            store.promptDeclinedAtMs = nowMs
        }
        _state.value = _state.value.copy(status = NotificationSettings.status(context), showPrompt = false)
    }

    fun openSettings() = NotificationSettings.open(context, _state.value.status)

    private fun permissionGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
