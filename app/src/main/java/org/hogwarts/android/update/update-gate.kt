package org.hogwarts.android.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.R
import java.text.NumberFormat
import java.util.Locale

/** Draws the update prompt over the whole app. Nothing is drawn while there is nothing to ask. */
@Composable
fun UpdateGate(
    viewModel: UpdateViewModel,
    onExit: () -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val arabic = Locale.getDefault().language == "ar"
    val notes = (if (arabic) ui.notesAr else ui.notesEn)?.takeIf { it.isNotBlank() }
    val apkUrl by rememberUpdatedState(ui.apkUrl)

    // Back from the settings page or the installer: the view-model decides what comes next.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.onPause() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                UpdateEvent.OpenBrowser -> openApk(context, apkUrl)
            }
        }
    }

    // The phone must not sleep mid-download: a locked screen suspends the connection.
    val busy = ui.stage is UpdateStage.Downloading || ui.stage is UpdateStage.Verifying
    val view = LocalView.current
    DisposableEffect(busy) {
        view.keepScreenOn = busy
        onDispose { view.keepScreenOn = false }
    }

    when (ui.decision) {
        UpdateDecision.None -> Unit
        UpdateDecision.Required -> {
            // Not dismissable: Back closes the app instead.
            BackHandler(onBack = onExit)
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.update_required_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        notes ?: stringResource(R.string.update_required_body),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    StageStatus(ui.stage)
                    Spacer(Modifier.height(24.dp))
                    PrimaryAction(
                        stage = ui.stage,
                        idleLabel = stringResource(R.string.update_now),
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextButton(onClick = { openApk(context, ui.apkUrl) }) {
                        Text(stringResource(R.string.update_open_browser))
                    }
                }
            }
        }
        // Optional updates are silent (downloaded and installed by the view-model): nothing to draw.
        UpdateDecision.Optional -> Unit
    }
}

/** The line(s) under the notes: progress, the permission sentence, the Play Protect hint or a failure. */
@Composable
private fun StageStatus(stage: UpdateStage) {
    when (stage) {
        UpdateStage.Idle -> Unit
        is UpdateStage.Downloading -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (stage.percent >= 0) {
                Text(
                    stringResource(R.string.update_downloading, formatPercent(stage.percent)),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { stage.percent / 100f }, modifier = Modifier.fillMaxWidth())
            } else {
                Text(
                    stringResource(R.string.update_downloading_plain),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        UpdateStage.Verifying -> Text(
            stringResource(R.string.update_verifying),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        UpdateStage.NeedsPermission -> Text(
            stringResource(R.string.update_allow_install),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        UpdateStage.Installing -> Text(
            stringResource(R.string.update_installing),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        is UpdateStage.ReadyToInstall -> if (stage.afterBackOut) {
            Text(
                stringResource(R.string.update_protect_hint),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        is UpdateStage.Failed -> Text(
            stringResource(
                when (stage.reason) {
                    FailReason.Network -> R.string.update_failed_network
                    FailReason.Incomplete -> R.string.update_failed_incomplete
                    FailReason.NoSpace -> R.string.update_failed_space
                    FailReason.Installer -> R.string.update_failed_installer
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
    }
}

/** The one button that matches the stage; none while a download or the installer is busy. */
@Composable
private fun PrimaryAction(
    stage: UpdateStage,
    idleLabel: String,
    viewModel: UpdateViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    when (stage) {
        UpdateStage.Idle -> Button(onClick = viewModel::startUpdate, modifier = modifier) { Text(idleLabel) }
        is UpdateStage.Downloading, UpdateStage.Verifying, UpdateStage.Installing -> Unit
        UpdateStage.NeedsPermission -> Button(
            onClick = { openInstallPermissionSettings(context) },
            modifier = modifier,
        ) { Text(stringResource(R.string.update_allow_button)) }
        is UpdateStage.ReadyToInstall -> Button(onClick = viewModel::install, modifier = modifier) {
            Text(stringResource(R.string.update_install))
        }
        is UpdateStage.Failed -> Button(onClick = viewModel::startUpdate, modifier = modifier) {
            Text(stringResource(R.string.update_retry))
        }
    }
}

private fun formatPercent(percent: Int): String = NumberFormat.getIntegerInstance(Locale.getDefault()).format(percent)

/** The system page where the user lets this app install packages. */
private fun openInstallPermissionSettings(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    } catch (_: ActivityNotFoundException) {
        // OEM build without that page: the app's own settings page is the nearest thing.
        try {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        } catch (_: ActivityNotFoundException) {
            // Nothing to open.
        }
    }
}

/** Browser download — the escape hatch, no install permission needed. */
private fun openApk(context: Context, url: String) {
    if (url.isBlank()) return
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        // No browser: nothing to do.
    }
}
