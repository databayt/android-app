package org.hogwarts.android.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.feature.profile.R
import org.hogwarts.android.feature.profile.domain.ProfileAccess
import org.hogwarts.android.feature.profile.domain.model.ProfileRole
import org.hogwarts.android.feature.profile.ui.components.ActivityFeed
import org.hogwarts.android.feature.profile.ui.components.ContributionGraph
import org.hogwarts.android.feature.profile.ui.components.ParentProfileView
import org.hogwarts.android.feature.profile.ui.components.PinnedItemsList
import org.hogwarts.android.feature.profile.ui.components.ProfileSidebar
import org.hogwarts.android.feature.profile.ui.components.StaffProfileView
import org.hogwarts.android.feature.profile.ui.components.StudentProfileView
import org.hogwarts.android.feature.profile.ui.components.TeacherProfileView

/**
 * Read-only view of someone else's profile.
 * Web reference: profile/detail/content.tsx
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val permissions = remember(uiState.profile, uiState.viewerUserId, uiState.viewerRole) {
        uiState.profile?.let { target ->
            ProfileAccess.resolve(
                viewerRole = uiState.viewerRole,
                viewerUserId = uiState.viewerUserId,
                target = target,
                sameSchool = uiState.viewerSchoolId == target.schoolId
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.profile?.displayName ?: stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.profile_detail_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.profile == null -> ErrorMessage(
                text = uiState.error ?: "—",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            permissions?.canView != true -> ErrorMessage(
                text = stringResource(R.string.profile_detail_no_permission),
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            else -> {
                val profile = uiState.profile!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    state = listState,
                    contentPadding = PaddingValues(AppleSpacing.Standard),
                    verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
                ) {
                    item { ProfileSidebar(profile = profile) }
                    item {
                        when (profile.role) {
                            ProfileRole.STUDENT -> StudentProfileView(profile, isOwner = false)
                            ProfileRole.TEACHER -> TeacherProfileView(profile, isOwner = false)
                            ProfileRole.PARENT -> ParentProfileView(profile, isOwner = false)
                            ProfileRole.STAFF -> StaffProfileView(profile, isOwner = false)
                            ProfileRole.ADMIN -> {}
                        }
                    }
                    uiState.contributions?.let { item { ContributionGraph(it) } }
                    if (uiState.pinned.isNotEmpty()) item { PinnedItemsList(uiState.pinned) }
                    item { ActivityFeed(uiState.activity) }
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(AppleSpacing.Standard), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
