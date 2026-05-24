package org.hogwarts.android.feature.guardian.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.guardian.R
import org.hogwarts.android.feature.guardian.domain.model.Child

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildrenScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChild: (String) -> Unit,
    viewModel: ChildrenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guardian_children_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.guardian_back)
                        )
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
            if (uiState.children.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    icon = Icons.Default.People,
                    title = stringResource(R.string.guardian_no_children_title),
                    subtitle = stringResource(R.string.guardian_no_children_subtitle),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    items(uiState.children, key = { it.id }) { child ->
                        ChildCard(
                            child = child,
                            onClick = { onNavigateToChild(child.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChildCard(
    child: Child,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                name = child.displayName,
                imageUrl = child.avatarUrl,
                size = 56.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = child.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = child.classDisplay,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(
                        text = "${(child.attendanceRate * 100).toInt()}%",
                        color = when {
                            child.attendanceRate >= 0.9f -> MaterialTheme.colorScheme.primary
                            child.attendanceRate >= 0.75f -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )

                    child.latestGrade?.let { grade ->
                        StatusBadge(
                            text = grade,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (child.feeBalance > 0) {
                        StatusBadge(
                            text = "$${String.format("%.0f", child.feeBalance)}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun ChildCardPreview() {
    HogwartsTheme {
        Surface {
            ChildCard(
                child = Child(
                    id = "1",
                    studentId = "s1",
                    givenName = "Ahmed",
                    familyName = "Al-Rashid",
                    grade = "Grade 10",
                    section = "A",
                    attendanceRate = 0.92f,
                    latestGrade = "A",
                    feeBalance = 1500.0
                ),
                onClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
