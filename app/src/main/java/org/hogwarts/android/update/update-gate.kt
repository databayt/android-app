package org.hogwarts.android.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.R
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
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { openApk(context, ui.apkUrl) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.update_now))
                    }
                }
            }
        }
        UpdateDecision.Optional -> AlertDialog(
            onDismissRequest = viewModel::dismissOptional,
            title = { Text(stringResource(R.string.update_available_title)) },
            text = { Text(notes ?: stringResource(R.string.update_available_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissOptional(); openApk(context, ui.apkUrl) }) {
                    Text(stringResource(R.string.update_action))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissOptional) { Text(stringResource(R.string.update_later)) }
            },
        )
    }
}

/** Browser download — no install permission needed. */
private fun openApk(context: Context, url: String) {
    if (url.isBlank()) return
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        // No browser: nothing to do.
    }
}
