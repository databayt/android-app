package org.hogwarts.android.feature.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.admin.R
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.admin.domain.model.SchoolInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolInfoScreen(
    onNavigateBack: () -> Unit,
    viewModel: SchoolInfoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_school_info_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.admin_back))
                    }
                },
                actions = {
                    if (uiState.schoolInfo != null) {
                        IconButton(onClick = viewModel::toggleEdit) {
                            Icon(
                                imageVector = if (uiState.isEditing) Icons.Default.Save else Icons.Default.Edit,
                                contentDescription = if (uiState.isEditing) stringResource(R.string.admin_save) else stringResource(R.string.admin_edit)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.schoolInfo != null -> {
                    if (uiState.isEditing) {
                        SchoolInfoEditContent(
                            info = uiState.schoolInfo!!,
                            isSaving = uiState.isSaving,
                            onSave = viewModel::saveSchoolInfo
                        )
                    } else {
                        SchoolInfoDisplayContent(info = uiState.schoolInfo!!)
                    }
                }
                uiState.error != null -> {
                    EmptyState(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.admin_error_title),
                        subtitle = uiState.error ?: stringResource(R.string.admin_error_load_school_info),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun SchoolInfoDisplayContent(
    info: SchoolInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = info.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = info.domain,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusBadge(
                    text = info.subscription,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        // Contact
        InfoSection(title = stringResource(R.string.admin_section_contact)) {
            InfoRow(label = stringResource(R.string.admin_label_email), value = info.contactEmail)
            InfoRow(label = stringResource(R.string.admin_label_phone), value = info.contactPhone)
            InfoRow(label = stringResource(R.string.admin_label_address), value = info.address)
        }

        // Academic
        InfoSection(title = stringResource(R.string.admin_section_academic)) {
            InfoRow(label = stringResource(R.string.admin_label_academic_year), value = info.academicYear)
            InfoRow(label = stringResource(R.string.admin_label_active_terms), value = info.activeTerms.joinToString(", "))
        }
    }
}

@Composable
private fun SchoolInfoEditContent(
    info: SchoolInfo,
    isSaving: Boolean,
    onSave: (SchoolInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(info.name) }
    var contactEmail by remember { mutableStateOf(info.contactEmail) }
    var contactPhone by remember { mutableStateOf(info.contactPhone) }
    var address by remember { mutableStateOf(info.address) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.admin_label_school_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contactEmail,
            onValueChange = { contactEmail = it },
            label = { Text(stringResource(R.string.admin_label_contact_email)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contactPhone,
            onValueChange = { contactPhone = it },
            label = { Text(stringResource(R.string.admin_label_contact_phone)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text(stringResource(R.string.admin_label_address)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            androidx.compose.material3.Button(
                onClick = {
                    onSave(
                        info.copy(
                            name = name,
                            contactEmail = contactEmail,
                            contactPhone = contactPhone,
                            address = address
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.admin_save_changes))
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
