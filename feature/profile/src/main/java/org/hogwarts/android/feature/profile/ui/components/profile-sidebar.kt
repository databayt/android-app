package org.hogwarts.android.feature.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.feature.profile.domain.model.UserProfile

/**
 * Mirrors web `profile/sidebar.tsx` — avatar + identity + meta block.
 * On mobile this becomes a stacked header above tabs.
 */
@Composable
fun ProfileSidebar(
    profile: UserProfile,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppleSpacing.Standard),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserAvatar(
            name = profile.displayName,
            imageUrl = profile.avatarUrl,
            size = 96.dp
        )
        Text(
            text = profile.displayName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "@${profile.username ?: profile.email.substringBefore('@')}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        profile.bio?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        ProfileMetaRow(profile)
        actions()
    }
}

@Composable
private fun ProfileMetaRow(profile: UserProfile) {
    val parts = listOfNotNull(
        profile.role.name.lowercase().replaceFirstChar { it.uppercaseChar() },
        profile.school?.name,
        profile.student?.gradeName?.let { "Grade $it" },
        profile.teacher?.department,
        profile.staff?.designation
    )
    if (parts.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = parts.joinToString(" · "),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
