package org.hogwarts.android.feature.profile.ui

import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.profile.domain.model.ActivityFeedItem
import org.hogwarts.android.feature.profile.domain.model.ContributionGraphData
import org.hogwarts.android.feature.profile.domain.model.PinnedItem
import org.hogwarts.android.feature.profile.domain.model.UserProfile

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val contributions: ContributionGraphData? = null,
    val activity: List<ActivityFeedItem> = emptyList(),
    val pinned: List<PinnedItem> = emptyList(),
    val isOwner: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val viewerUserId: String? = null,
    val viewerRole: UserRole? = null,
    val viewerSchoolId: String? = null,
    val error: String? = null
)
