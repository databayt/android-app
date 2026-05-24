package org.hogwarts.android.feature.admin.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.admin.R
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.HogwartsSearchBar
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.feature.admin.domain.model.StaffMember
import org.hogwarts.android.feature.admin.domain.model.StaffRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDirectoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: StaffDirectoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_staff_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.admin_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HogwartsSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearch,
                    placeholder = stringResource(R.string.admin_staff_search_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role filter chips
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedRole == null,
                            onClick = { viewModel.selectRole(null) },
                            label = { Text(stringResource(R.string.admin_staff_filter_all)) }
                        )
                    }
                    items(StaffRole.entries.toList()) { role ->
                        FilterChip(
                            selected = uiState.selectedRole == role,
                            onClick = { viewModel.selectRole(role) },
                            label = { Text(role.displayName()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.filteredStaff.isEmpty() && !uiState.isLoading) {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = stringResource(R.string.admin_staff_no_results_title),
                        subtitle = stringResource(R.string.admin_staff_no_results_subtitle),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                    ) {
                        items(uiState.filteredStaff, key = { it.id }) { member ->
                            StaffCard(
                                member = member,
                                onCallClick = {
                                    member.phone?.let { phone ->
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                    }
                                },
                                onEmailClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${member.email}"))
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffCard(
    member: StaffMember,
    onCallClick: () -> Unit,
    onEmailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                name = member.displayName,
                imageUrl = member.avatarUrl
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = member.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusBadge(
                        text = member.role.displayName(),
                        color = member.role.color()
                    )
                }
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                member.department?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Contact actions
            if (member.phone != null) {
                IconButton(onClick = onCallClick) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = stringResource(R.string.admin_staff_call),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onEmailClick) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = stringResource(R.string.admin_staff_email),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StaffRole.displayName(): String = when (this) {
    StaffRole.ADMIN -> stringResource(R.string.admin_staff_role_admin)
    StaffRole.TEACHER -> stringResource(R.string.admin_staff_role_teacher)
    StaffRole.COUNSELOR -> stringResource(R.string.admin_staff_role_counselor)
    StaffRole.STAFF -> stringResource(R.string.admin_staff_role_staff)
}

@Composable
private fun StaffRole.color() = when (this) {
    StaffRole.ADMIN -> MaterialTheme.colorScheme.error
    StaffRole.TEACHER -> MaterialTheme.colorScheme.primary
    StaffRole.COUNSELOR -> MaterialTheme.colorScheme.tertiary
    StaffRole.STAFF -> MaterialTheme.colorScheme.secondary
}
