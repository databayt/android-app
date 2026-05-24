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
 * Web reference: profile/teacher.tsx
 */
@Composable
fun TeacherProfileView(
    profile: UserProfile,
    isOwner: Boolean,
    modifier: Modifier = Modifier
) {
    val teacher = profile.teacher ?: return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
        ) {
            StatCard("Classes", "—", Modifier.weight(1f))
            StatCard("Students", "—", Modifier.weight(1f))
            StatCard("Pass rate", "—", Modifier.weight(1f))
        }
        InfoCard(title = "Department") {
            InfoRow("Department", teacher.department ?: "—")
            InfoRow("Status", teacher.status ?: "—")
            InfoRow("Email", teacher.email ?: profile.email)
        }
    }
}
