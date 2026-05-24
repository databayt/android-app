package org.hogwarts.android.feature.admission.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.admission.R
import org.hogwarts.android.feature.admission.domain.model.AdmissionApplication
import org.hogwarts.android.feature.admission.domain.model.ApplicationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToApplication: (String) -> Unit,
    onNavigateToNewApplication: () -> Unit,
    viewModel: ApplicationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admission_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.admission_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNewApplication) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.admission_new_application))
            }
        }
    ) { innerPadding ->
        if (uiState.applications.isEmpty() && !uiState.isLoading) {
            EmptyState(
                icon = Icons.Default.Description,
                title = stringResource(R.string.admission_no_applications_title),
                subtitle = stringResource(R.string.admission_no_applications_subtitle),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(uiState.applications, key = { it.id }) { application ->
                    ApplicationCard(
                        application = application,
                        onClick = { onNavigateToApplication(application.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplicationCard(
    application: AdmissionApplication,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fallbackName = stringResource(R.string.admission_new_application_fallback)
                Text(
                    text = "${application.personalInfo.givenNameEn} ${application.personalInfo.familyNameEn}".ifBlank { fallbackName },
                    style = MaterialTheme.typography.titleSmall
                )
                StatusBadge(
                    text = application.status.displayName(),
                    color = application.status.color()
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            application.trackingNumber?.let {
                Text(
                    text = stringResource(R.string.admission_tracking, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (application.status == ApplicationStatus.DRAFT) {
                Text(
                    text = stringResource(
                        R.string.admission_step_progress,
                        application.currentStep.number,
                        application.currentStep.title
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            application.academicHistory.applyingGrade.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = stringResource(R.string.admission_applying_for, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ApplicationStatus.displayName(): String = stringResource(
    when (this) {
        ApplicationStatus.DRAFT -> R.string.admission_status_draft
        ApplicationStatus.SUBMITTED -> R.string.admission_status_submitted
        ApplicationStatus.UNDER_REVIEW -> R.string.admission_status_under_review
        ApplicationStatus.INTERVIEW_SCHEDULED -> R.string.admission_status_interview
        ApplicationStatus.ACCEPTED -> R.string.admission_status_accepted
        ApplicationStatus.REJECTED -> R.string.admission_status_rejected
        ApplicationStatus.WAITLISTED -> R.string.admission_status_waitlisted
    }
)

@Composable
private fun ApplicationStatus.color() = when (this) {
    ApplicationStatus.DRAFT -> MaterialTheme.colorScheme.onSurfaceVariant
    ApplicationStatus.SUBMITTED -> MaterialTheme.colorScheme.primary
    ApplicationStatus.UNDER_REVIEW -> MaterialTheme.colorScheme.tertiary
    ApplicationStatus.INTERVIEW_SCHEDULED -> MaterialTheme.colorScheme.secondary
    ApplicationStatus.ACCEPTED -> MaterialTheme.colorScheme.primary
    ApplicationStatus.REJECTED -> MaterialTheme.colorScheme.error
    ApplicationStatus.WAITLISTED -> MaterialTheme.colorScheme.outline
}
