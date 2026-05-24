package org.hogwarts.android.feature.profile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.feature.profile.R
import org.hogwarts.android.feature.profile.domain.model.SchoolInfo

@Composable
fun SchoolSwitcherDialog(
    schools: List<SchoolInfo>,
    onSchoolSelected: (SchoolInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedId by remember { mutableStateOf(schools.find { it.isCurrentSchool }?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_switch_school)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)) {
                schools.forEach { school ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedId = school.id }
                            .padding(vertical = AppleSpacing.Compact),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                    ) {
                        RadioButton(
                            selected = selectedId == school.id,
                            onClick = { selectedId = school.id }
                        )
                        Column {
                            Text(school.name, style = MaterialTheme.typography.bodyMedium)
                            if (school.isCurrentSchool) {
                                Text(
                                    stringResource(R.string.profile_current_school),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    schools.find { it.id == selectedId }?.let(onSchoolSelected)
                }
            ) { Text(stringResource(R.string.profile_switch)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_cancel)) }
        }
    )
}
