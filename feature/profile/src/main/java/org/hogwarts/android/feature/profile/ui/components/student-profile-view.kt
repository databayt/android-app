package org.hogwarts.android.feature.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.feature.profile.domain.model.UserProfile

/**
 * Web reference: profile/student.tsx
 * Stats grid + key academic blocks. Hardcoded values from web are placeholders;
 * real data comes via separate academic endpoints (out of scope here).
 */
@Composable
fun StudentProfileView(
    profile: UserProfile,
    isOwner: Boolean,
    modifier: Modifier = Modifier
) {
    val student = profile.student ?: return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
        ) {
            StatCard("GPA", "—", Modifier.weight(1f))
            StatCard("Attendance", "—", Modifier.weight(1f))
            StatCard("Assignments", "—", Modifier.weight(1f))
        }
        InfoCard(title = "Enrollment") {
            InfoRow("Section", student.sectionName ?: "—")
            InfoRow("Grade", student.gradeName ?: "—")
            InfoRow("Status", student.status ?: "—")
        }
    }
}

@Composable
internal fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(AppleSpacing.Standard),
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
internal fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(AppleSpacing.Standard),
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
internal fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
