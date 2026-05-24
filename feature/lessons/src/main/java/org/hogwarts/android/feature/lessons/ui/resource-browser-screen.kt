package org.hogwarts.android.feature.lessons.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.feature.lessons.R
import org.hogwarts.android.feature.lessons.domain.model.LessonResource
import org.hogwarts.android.feature.lessons.domain.model.ResourceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceBrowserScreen(
    onNavigateBack: () -> Unit,
    viewModel: ResourceBrowserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lessons_resources_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.lessons_back))
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
                // Type filter chips
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedType == null,
                            onClick = { viewModel.selectType(null) },
                            label = { Text(stringResource(R.string.lessons_resources_all)) }
                        )
                    }
                    items(ResourceType.entries.toList()) { type ->
                        FilterChip(
                            selected = uiState.selectedType == type,
                            onClick = { viewModel.selectType(type) },
                            label = { Text(type.displayName()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.filteredResources.isEmpty() && !uiState.isLoading) {
                    EmptyState(
                        icon = Icons.Default.MenuBook,
                        title = "No Resources",
                        subtitle = "No teaching resources found for the selected filter.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                    ) {
                        items(uiState.filteredResources, key = { it.id }) { resource ->
                            ResourceCard(
                                resource = resource,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                                    context.startActivity(intent)
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
private fun ResourceCard(
    resource: LessonResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = resource.type.icon(),
                contentDescription = null,
                tint = resource.type.iconColor(),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = resource.type.displayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    resource.fileSize?.let { size ->
                        Text(
                            text = formatResourceFileSize(size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.lessons_resources_open_cd),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun ResourceType.displayName(): String = when (this) {
    ResourceType.PDF -> "PDF"
    ResourceType.VIDEO -> "Video"
    ResourceType.LINK -> "Link"
    ResourceType.IMAGE -> "Image"
    ResourceType.DOCUMENT -> "Document"
}

private fun ResourceType.icon(): ImageVector = when (this) {
    ResourceType.PDF -> Icons.Default.InsertDriveFile
    ResourceType.VIDEO -> Icons.Default.PlayCircle
    ResourceType.LINK -> Icons.Default.Share
    ResourceType.IMAGE -> Icons.Default.Image
    ResourceType.DOCUMENT -> Icons.Default.Description
}

@Composable
private fun ResourceType.iconColor() = when (this) {
    ResourceType.PDF -> MaterialTheme.colorScheme.error
    ResourceType.VIDEO -> MaterialTheme.colorScheme.primary
    ResourceType.LINK -> MaterialTheme.colorScheme.tertiary
    ResourceType.IMAGE -> MaterialTheme.colorScheme.secondary
    ResourceType.DOCUMENT -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun formatResourceFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    }
}
