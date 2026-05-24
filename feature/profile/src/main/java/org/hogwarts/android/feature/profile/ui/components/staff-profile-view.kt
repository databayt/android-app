package org.hogwarts.android.feature.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.feature.profile.domain.model.UserProfile

/**
 * Web reference: profile/staff.tsx
 */
@Composable
fun StaffProfileView(
    profile: UserProfile,
    isOwner: Boolean,
    modifier: Modifier = Modifier
) {
    val staff = profile.staff ?: return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
        ) {
            StatCard("Tasks", "—", Modifier.weight(1f))
            StatCard("Projects", "—", Modifier.weight(1f))
        }
        InfoCard(title = "Employment") {
            InfoRow("Employee ID", staff.employeeId ?: "—")
            InfoRow("Department", staff.department ?: "—")
            InfoRow("Designation", staff.designation ?: "—")
            InfoRow("Type", staff.employmentType ?: "—")
        }
    }
}
