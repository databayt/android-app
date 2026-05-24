package org.hogwarts.android.feature.guardian.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.guardian.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentFormsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConsentFormsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSignatureDialog by remember { mutableStateOf(false) }
    var selectedFormId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guardian_consent_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.guardian_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        val pendingLabel = stringResource(R.string.guardian_consent_pending)
        val signedLabel = stringResource(R.string.guardian_consent_signed)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pending forms
            item {
                Text(pendingLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(uiState.pendingForms) { form ->
                ConsentFormCard(
                    title = form.title,
                    description = form.description,
                    status = pendingLabel,
                    statusColor = MaterialTheme.colorScheme.tertiary,
                    onSign = {
                        selectedFormId = form.id
                        showSignatureDialog = true
                    }
                )
            }

            // Signed forms
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(signedLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(uiState.signedForms) { form ->
                ConsentFormCard(
                    title = form.title,
                    description = form.description,
                    status = signedLabel,
                    statusColor = MaterialTheme.colorScheme.primary,
                    onSign = null
                )
            }
        }
    }

    // Signature dialog
    if (showSignatureDialog) {
        SignatureDialog(
            onDismiss = { showSignatureDialog = false },
            onSign = { signatureData ->
                selectedFormId?.let { viewModel.signForm(it, signatureData) }
                showSignatureDialog = false
            }
        )
    }
}

@Composable
private fun ConsentFormCard(
    title: String,
    description: String,
    status: String,
    statusColor: Color,
    onSign: (() -> Unit)?
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            onSign?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = it, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.guardian_consent_sign_form))
                }
            }
        }
    }
}

@Composable
private fun SignatureDialog(
    onDismiss: () -> Unit,
    onSign: (List<Offset>) -> Unit
) {
    val points = remember { mutableStateListOf<Offset>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.guardian_consent_draw_signature)) },
        text = {
            Column {
                Text(stringResource(R.string.guardian_consent_sign_below), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outline
                    )
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    points.add(change.position)
                                }
                            }
                    ) {
                        if (points.size > 1) {
                            val path = Path()
                            path.moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                path.lineTo(points[i].x, points[i].y)
                            }
                            drawPath(path, Color.Black, style = Stroke(width = 3f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { points.clear() }) {
                    Text(stringResource(R.string.guardian_consent_clear))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSign(points.toList()) },
                enabled = points.size > 5
            ) { Text(stringResource(R.string.guardian_consent_submit_signature)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.guardian_consent_cancel)) }
        }
    )
}
