package org.hogwarts.android.feature.dashboard.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.core.push.NotificationStatus
import org.hogwarts.android.feature.dashboard.R

/**
 * The persistent "notifications are off" strip: it stays while the system says off and is gone on
 * the first resume after the user turns them on (the screen re-reads the state on every resume).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationOffBanner(status: NotificationStatus, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    if (status == NotificationStatus.On) return
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .background(colors.muted)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(
                if (status == NotificationStatus.LiveChannelOff) R.string.notif_live_off_banner else R.string.notif_off_banner,
            ),
            style = type.body,
            color = colors.foreground,
        )
        FlowRow {
            PillButton(stringResource(R.string.notif_open_settings), onClick = onOpenSettings)
        }
    }
}

/** The one-time reason, shown before the system dialog: «السماح» opens it, «لاحقاً» waits a week. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationPromptSheet(onAllow: () -> Unit, onLater: () -> Unit) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    ModalBottomSheet(onDismissRequest = onLater, containerColor = colors.background) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.notif_prompt_title), style = type.bodyMedium, color = colors.foreground)
            Text(stringResource(R.string.notif_prompt_body), style = type.body, color = colors.mutedForeground)
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PillButton(stringResource(R.string.notif_prompt_allow), onClick = onAllow)
                PillButton(stringResource(R.string.notif_prompt_later), onClick = onLater, variant = PillVariant.Muted)
            }
        }
    }
}

/** Launches the system POST_NOTIFICATIONS request (Android 13+; a no-op answer "granted" below). */
@Composable
fun rememberNotificationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult)
    return {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onResult(true)
        }
    }
}
