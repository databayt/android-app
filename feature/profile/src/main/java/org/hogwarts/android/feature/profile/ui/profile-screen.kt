package org.hogwarts.android.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.feature.profile.R
import org.hogwarts.android.feature.profile.domain.model.ProfileRole
import org.hogwarts.android.feature.profile.domain.model.UserProfile
import org.hogwarts.android.feature.profile.domain.validation.ProfileUpdateForm
import org.hogwarts.android.feature.profile.ui.components.ActivityFeed
import org.hogwarts.android.feature.profile.ui.components.ContributionGraph
import org.hogwarts.android.feature.profile.ui.components.ParentProfileView
import org.hogwarts.android.feature.profile.ui.components.PinnedItemsList
import org.hogwarts.android.feature.profile.ui.components.ProfileForm
import org.hogwarts.android.feature.profile.ui.components.ProfileSidebar
import org.hogwarts.android.feature.profile.ui.components.StaffProfileView
import org.hogwarts.android.feature.profile.ui.components.StudentProfileView
import org.hogwarts.android.feature.profile.ui.components.TeacherProfileView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.profile_back))
                    }
                },
                actions = {
                    if (uiState.isOwner && uiState.profile != null && !uiState.isEditing) {
                        TextButton(onClick = { viewModel.setEditing(true) }) {
                            Text(stringResource(R.string.profile_edit))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> Loading(modifier = Modifier.fillMaxSize().padding(innerPadding))
            uiState.profile == null -> ErrorState(
                message = uiState.error ?: "—",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
            uiState.isEditing -> ProfileForm(
                initial = uiState.profile!!.toForm(),
                isSubmitting = uiState.isSaving,
                onSubmit = viewModel::save,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
            else -> ProfileBody(
                uiState = uiState,
                listState = listState,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ProfileBody(
    uiState: ProfileUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    val profile = uiState.profile ?: return
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(AppleSpacing.Standard),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
    ) {
        item { ProfileSidebar(profile = profile) }

        item { SectionHeader(stringResource(R.string.profile_section_overview)) }
        item {
            when (profile.role) {
                ProfileRole.STUDENT -> StudentProfileView(profile, uiState.isOwner)
                ProfileRole.TEACHER -> TeacherProfileView(profile, uiState.isOwner)
                ProfileRole.PARENT -> ParentProfileView(profile, uiState.isOwner)
                ProfileRole.STAFF -> StaffProfileView(profile, uiState.isOwner)
                ProfileRole.ADMIN -> {} // admin gets only the sidebar + contributions
            }
        }

        uiState.contributions?.let { data ->
            item { SectionHeader(stringResource(R.string.profile_section_contributions)) }
            item { ContributionGraph(data) }
        }

        if (uiState.pinned.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.profile_section_pinned)) }
            item { PinnedItemsList(uiState.pinned) }
        }

        item { SectionHeader(stringResource(R.string.profile_section_activity)) }
        item { ActivityFeed(uiState.activity) }

        if (uiState.error != null) {
            item {
                Text(
                    text = uiState.error,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(AppleSpacing.Standard)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    )
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(AppleSpacing.Standard), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun UserProfile.toForm(): ProfileUpdateForm = ProfileUpdateForm(
    displayName = username ?: displayName,
    bio = bio,
    website = socialLinks.website,
    github = socialLinks.github,
    twitter = socialLinks.twitter,
    linkedin = socialLinks.linkedin
)
