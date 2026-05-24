package org.hogwarts.android.feature.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.feature.profile.domain.model.ChildOverview
import org.hogwarts.android.feature.profile.domain.model.UserProfile

/**
 * Web reference: profile/parent.tsx — children list + family info.
 */
@Composable
fun ParentProfileView(
    profile: UserProfile,
    isOwner: Boolean,
    modifier: Modifier = Modifier
) {
    val guardian = profile.guardian ?: return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
    ) {
        InfoCard(title = "Family") {
            InfoRow("Relationship", guardian.relationship ?: "—")
            InfoRow("Phone", guardian.phone ?: "—")
            InfoRow("Occupation", guardian.occupation ?: "—")
        }
        InfoCard(title = "Children") {
            if (guardian.children.isEmpty()) {
                Text(
                    text = "No linked children",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                guardian.children.forEach { ChildRow(it) }
            }
        }
    }
}

@Composable
private fun ChildRow(child: ChildOverview) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = child.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = listOfNotNull(child.grade, child.section).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${(child.attendanceRate * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
