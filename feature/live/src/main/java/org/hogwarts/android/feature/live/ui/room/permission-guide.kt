package org.hogwarts.android.feature.live.ui.room

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.feature.live.R

/** Which device the guide is about. */
enum class PermissionKind { MIC, CAMERA }

/** The app's own settings page, where a permission denied for good is switched back on. */
fun openAppSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

/**
 * One dialog for a denied microphone or camera. [PermissionState.RATIONALE] explains why and
 * re-asks; [PermissionState.BLOCKED] walks to the app's settings. [forShare] adds that a screen
 * share needs the microphone.
 */
@Composable
fun PermissionGuide(
    kind: PermissionKind,
    state: PermissionState,
    forShare: Boolean,
    onAllow: () -> Unit,
    onOpenSettings: () -> Unit,
    onLater: () -> Unit,
) {
    val mic = kind == PermissionKind.MIC
    if (state == PermissionState.BLOCKED) {
        AlertDialog(
            onDismissRequest = onLater,
            title = { Text(stringResource(if (mic) R.string.live_perm_mic_blocked_title else R.string.live_perm_camera_blocked_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier) {
                    if (forShare) Text(stringResource(R.string.live_perm_share_needs_mic))
                    Text(stringResource(R.string.live_perm_step_1))
                    Text(stringResource(R.string.live_perm_step_2))
                    Text(stringResource(if (mic) R.string.live_perm_step_3_mic else R.string.live_perm_step_3_camera))
                    Text(stringResource(R.string.live_perm_then_return))
                }
            },
            confirmButton = { TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.live_perm_open_settings)) } },
            dismissButton = { TextButton(onClick = onLater) { Text(stringResource(R.string.live_perm_later)) } },
        )
    } else {
        AlertDialog(
            onDismissRequest = onLater,
            title = { Text(stringResource(if (mic) R.string.live_perm_mic_rationale_title else R.string.live_perm_camera_rationale_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (forShare) Text(stringResource(R.string.live_perm_share_needs_mic))
                    Text(stringResource(if (mic) R.string.live_perm_mic_rationale else R.string.live_perm_camera_rationale))
                }
            },
            confirmButton = { TextButton(onClick = onAllow) { Text(stringResource(R.string.live_perm_allow)) } },
            dismissButton = { TextButton(onClick = onLater) { Text(stringResource(R.string.live_perm_later)) } },
        )
    }
}
