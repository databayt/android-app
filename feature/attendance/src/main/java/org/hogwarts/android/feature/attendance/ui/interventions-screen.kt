package org.hogwarts.android.feature.attendance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.attendance.R
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.attendance.domain.model.AttendanceIntervention
import org.hogwarts.android.feature.attendance.domain.model.InterventionStatus
import org.hogwarts.android.feature.attendance.domain.model.InterventionType

/**
 * Interventions screen for monitoring students with low attendance.
 *
 * Shows a list of at-risk students with threshold visualization
 * and action buttons for parent alerts and counselor referrals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterventionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InterventionsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                    ) {
                        Text(stringResource(R.string.attendance_interventions_title))
                        if (uiState.criticalCount > 0) {
                            StatusBadge(
                                text = stringResource(R.string.attendance_critical_count, uiState.criticalCount),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = HogwartsIcons.Back,
                            contentDescription = stringResource(R.string.attendance_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null && uiState.interventions.isEmpty() -> {
                EmptyState(
                    icon = HogwartsIcons.Attendance,
                    title = stringResource(R.string.attendance_error_title),
                    subtitle = uiState.error ?: stringResource(R.string.attendance_something_went_wrong),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppleSpacing.Standard, vertical = AppleSpacing.Compact),
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                    ) {
                        InterventionFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = uiState.selectedFilter == filter,
                                onClick = { viewModel.setFilter(filter) },
                                label = {
                                    Text(
                                        text = filter.name.replace("_", " ").lowercase()
                                            .replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }

                    val filtered = uiState.filteredInterventions

                    if (filtered.isEmpty()) {
                        EmptyState(
                            icon = HogwartsIcons.Attendance,
                            title = stringResource(R.string.attendance_no_interventions_title),
                            subtitle = stringResource(R.string.attendance_no_interventions_subtitle),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = AppleSpacing.Standard,
                                vertical = AppleSpacing.Compact
                            )
                        ) {
                            items(
                                items = filtered,
                                key = { it.id }
                            ) { intervention ->
                                InterventionCard(intervention = intervention)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InterventionCard(
    intervention: AttendanceIntervention,
    modifier: Modifier = Modifier
) {
    val isCritical = intervention.deficit > 15f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCritical) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(AppleSpacing.Standard),
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
        ) {
            // Header: student name + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                ) {
                    if (isCritical) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = stringResource(R.string.attendance_critical_icon_desc),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = intervention.studentName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusBadge(
                    text = intervention.status.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    color = intervention.status.toColor()
                )
            }

            // Intervention type
            Text(
                text = intervention.type.toDisplayName(
                    parentAlert = stringResource(R.string.attendance_intervention_parent_alert),
                    counselorReferral = stringResource(R.string.attendance_intervention_counselor_referral),
                    adminReview = stringResource(R.string.attendance_intervention_admin_review)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Attendance rate progress bar
            Column(
                verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.attendance_attendance_rate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.1f%% / %.1f%% threshold".format(
                            intervention.currentRate,
                            intervention.threshold
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCritical) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Progress bar showing current rate vs threshold
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Background (threshold indicator)
                    LinearProgressIndicator(
                        progress = { intervention.threshold / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    // Current rate
                    LinearProgressIndicator(
                        progress = { intervention.currentRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (isCritical) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f),
                    )
                }
            }

            // Action buttons (for pending interventions)
            if (intervention.status == InterventionStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: Alert parent action */ },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = stringResource(R.string.attendance_alert_parent_icon_desc),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.attendance_alert_parent))
                    }
                    Spacer(modifier = Modifier.width(AppleSpacing.Compact))
                    Button(
                        onClick = { /* TODO: Refer counselor action */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.attendance_refer_counselor_icon_desc),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.attendance_refer_counselor))
                    }
                }
            }
        }
    }
}

@Composable
private fun InterventionStatus.toColor() = when (this) {
    InterventionStatus.PENDING -> MaterialTheme.colorScheme.error
    InterventionStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
    InterventionStatus.RESOLVED -> MaterialTheme.colorScheme.primary
}

private fun InterventionType.toDisplayName(
    parentAlert: String,
    counselorReferral: String,
    adminReview: String
): String = when (this) {
    InterventionType.PARENT_ALERT -> parentAlert
    InterventionType.COUNSELOR_REFERRAL -> counselorReferral
    InterventionType.ADMIN_REVIEW -> adminReview
}
