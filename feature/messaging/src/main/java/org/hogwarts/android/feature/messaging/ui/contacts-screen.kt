package org.hogwarts.android.feature.messaging.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors
import org.hogwarts.android.core.designsystem.theme.WhatsAppTheme
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.domain.model.SidebarFilter
import org.hogwarts.android.feature.messaging.ui.component.ContactRow
import org.hogwarts.android.feature.messaging.ui.component.ContactSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToWhatsAppSettings: () -> Unit = {},
    viewModel: ContactsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    WhatsAppTheme {
        val uiState by viewModel.uiState.collectAsState()
        val waColors = LocalWhatsAppColors.current
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.messaging_ui_title)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.messaging_ui_back),
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToWhatsAppSettings) {
                            Text(
                                text = "W",
                                style = MaterialTheme.typography.titleMedium,
                                color = waColors.textProduct,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = waColors.surfacePanel,
                    ),
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                AnimatedVisibility(visible = !uiState.isConnected && !uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.messaging_ui_connecting),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }

                ContactSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.availableFilters) { filter ->
                        val isSelected = filter == uiState.selectedFilter
                        val label = filterLabel(filter, uiState.totalUnreadCount)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onFilterChanged(filter) },
                            label = {
                                Text(text = label, style = MaterialTheme.typography.labelMedium)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD9FDD4),
                                selectedLabelColor = Color(0xFF15603E),
                            ),
                        )
                    }
                }

                when {
                    uiState.isLoading && uiState.contacts.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.contacts.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = HogwartsIcons.Messages,
                                title = stringResource(R.string.messaging_contacts_empty_title),
                                subtitle = stringResource(R.string.messaging_contacts_empty_subtitle),
                            )
                        }
                    }
                    else -> {
                        LazyColumn(Modifier.fillMaxSize(), state = listState) {
                            items(uiState.contacts, key = { it.id }) { contact ->
                                ContactRow(
                                    contact = contact,
                                    isOnline = contact.id in uiState.onlineUsers,
                                    isActive = false,
                                    onClick = {
                                        scope.launch {
                                            val convoId = contact.conversationId
                                                ?: runCatching {
                                                    viewModel.getOrCreateDirectConversationFor(contact.id)
                                                }.getOrNull()
                                            if (convoId != null) {
                                                onNavigateToChat(convoId)
                                            }
                                        }
                                    },
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 76.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun filterLabel(filter: SidebarFilter, totalUnread: Int): String = when (filter) {
    SidebarFilter.All -> stringResource(R.string.messaging_contacts_filter_all)
    SidebarFilter.Unread ->
        if (totalUnread > 0) stringResource(R.string.messaging_contacts_filter_unread_count, totalUnread)
        else stringResource(R.string.messaging_contacts_filter_unread)
    SidebarFilter.Favourites -> stringResource(R.string.messaging_contacts_filter_favourites)
    is SidebarFilter.Category -> stringResource(categoryStringRes(filter))
}

@Composable
private fun categoryStringRes(filter: SidebarFilter.Category): Int = when (filter.category) {
    org.hogwarts.android.feature.messaging.domain.model.ContactCategory.TEACHERS ->
        R.string.messaging_contacts_category_teachers
    org.hogwarts.android.feature.messaging.domain.model.ContactCategory.STUDENTS ->
        R.string.messaging_contacts_category_students
    org.hogwarts.android.feature.messaging.domain.model.ContactCategory.PARENTS ->
        R.string.messaging_contacts_category_parents
    org.hogwarts.android.feature.messaging.domain.model.ContactCategory.STAFF ->
        R.string.messaging_contacts_category_staff
    org.hogwarts.android.feature.messaging.domain.model.ContactCategory.ADMIN ->
        R.string.messaging_contacts_category_admin
    org.hogwarts.android.feature.messaging.domain.model.ContactCategory.ACCOUNTANTS ->
        R.string.messaging_contacts_category_accountants
    org.hogwarts.android.feature.messaging.domain.model.ContactCategory.MY_STUDENTS ->
        R.string.messaging_contacts_category_my_students
    org.hogwarts.android.feature.messaging.domain.model.ContactCategory.MY_TEACHERS ->
        R.string.messaging_contacts_category_my_teachers
    org.hogwarts.android.feature.messaging.domain.model.ContactCategory.CLASSMATES ->
        R.string.messaging_contacts_category_classmates
    org.hogwarts.android.feature.messaging.domain.model.ContactCategory.MY_CHILDREN_TEACHERS ->
        R.string.messaging_contacts_category_my_children_teachers
}
