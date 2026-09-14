package org.hogwarts.android.feature.announcements.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.ItemCard
import org.hogwarts.android.core.designsystem.kit.ItemGrid
import org.hogwarts.android.core.designsystem.kit.ItemGridMore
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.PageNav
import org.hogwarts.android.core.designsystem.kit.PageNavItem
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.kit.SearchPill
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.announcements.R
import org.hogwarts.android.feature.announcements.domain.model.Announcement

@Composable
fun AnnouncementsScreen(
    onOpenAnnouncement: (id: String) -> Unit,
    onOpenHref: (href: String) -> Unit,
    viewModel: AnnouncementsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AnnouncementsContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onOpenAnnouncement = onOpenAnnouncement,
        onOpenHref = onOpenHref,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::retry,
    )
}

/**
 * The phone branch of `/announcements`: the page heading, the role's tabs
 * (`permissions.ts` `getTabsForRole`, shown only when there is more than one),
 * the search, then `table.tsx`'s grid — two cards across with a load-more pill.
 */
@Composable
fun AnnouncementsContent(
    state: AnnouncementsUiState,
    onQueryChange: (String) -> Unit,
    onOpenAnnouncement: (id: String) -> Unit,
    onOpenHref: (href: String) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp),
    ) {
        // PageHeadingDisplay: `text-4xl font-semibold tracking-tight`, mb-6.
        Text(
            stringResource(R.string.announcements_title),
            style = type.bannerHeadline.copy(fontSize = 36.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold),
            color = colors.foreground,
        )
        Spacer(Modifier.height(24.dp))

        val tabs = tabsFor(state)
        if (tabs.size > 1) {
            PageNav(
                items = tabs.map { it.item },
                selectedKey = TAB_ALL,
                onSelect = { picked -> tabs.firstOrNull { it.item.key == picked.key && it.href != null }?.href?.let(onOpenHref) },
            )
            Spacer(Modifier.height(24.dp))
        }

        SearchPill(
            value = state.query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.announcements_search),
        )

        if (state.isOffline) {
            Text(
                stringResource(R.string.announcements_stale_copy),
                style = type.caption,
                color = colors.mutedForeground,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Spacer(Modifier.height(8.dp))
        when {
            state.isLoading && state.items.isEmpty() -> SkeletonGrid()
            state.items.isEmpty() -> EmptyGrid(showRetry = state.failed, onRetry = onRetry)
            else -> {
                ItemGrid(count = state.items.size) { index, modifier ->
                    AnnouncementCard(state.items[index], modifier, onClick = { onOpenAnnouncement(state.items[index].id) })
                }
                if (state.hasMore) {
                    ItemGridMore(
                        label = stringResource(R.string.announcements_load_more),
                        loadingLabel = stringResource(R.string.announcements_loading),
                        loading = state.isLoadingMore,
                        onClick = onLoadMore,
                    )
                }
            }
        }
    }
}

private const val TAB_ALL = "all"

private data class Tab(val item: PageNavItem, val href: String?)

@Composable
private fun tabsFor(state: AnnouncementsUiState): List<Tab> = buildList {
    add(Tab(PageNavItem(TAB_ALL, stringResource(R.string.announcements_nav_all)), null))
    if (state.canWrite) {
        add(Tab(PageNavItem("templates", stringResource(R.string.announcements_nav_templates)), "/announcements/templates"))
        add(Tab(PageNavItem("archived", stringResource(R.string.announcements_nav_archived)), "/announcements/archived"))
    }
    if (state.isAdmin) {
        add(Tab(PageNavItem("settings", stringResource(R.string.announcements_nav_settings)), "/announcements/settings"))
    }
}

/** One `ItemCard` from `table.tsx`: scope eyebrow, title, status and notable-priority chips, created date. */
@Composable
private fun AnnouncementCard(announcement: Announcement, modifier: Modifier, onClick: () -> Unit) {
    val colors = HogwartsTheme.colors
    val format = announcementsFormat()
    // On a grey card a quiet chip sits on the page's own white (`bg-background`).
    val onPage = Modifier.clip(HogwartsShapes.Md).background(colors.background)
    ItemCard(
        title = announcement.title,
        eyebrow = scopeLabel(announcement.scope),
        meta = format.shortDate(announcement.createdAt),
        modifier = modifier,
        onClick = onClick,
        badges = {
            if (announcement.isPublished) {
                LabelBadge(stringResource(R.string.announcements_published), variant = BadgeVariant.Default)
            } else {
                LabelBadge(stringResource(R.string.announcements_draft), variant = BadgeVariant.Outline, modifier = onPage)
            }
            when (announcement.priority.lowercase()) {
                "urgent" -> LabelBadge(cardPriorityLabel(announcement.priority), variant = BadgeVariant.Destructive)
                "high" -> LabelBadge(cardPriorityLabel(announcement.priority), variant = BadgeVariant.Outline, modifier = onPage)
            }
        },
    )
}

/** `GridEmptyState`: icon, `font-medium` title, muted description, `py-12` centred. */
@Composable
private fun EmptyGrid(showRetry: Boolean, onRetry: () -> Unit) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(
        Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.Newspaper, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.announcements_empty_title),
            style = type.body.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
            color = colors.foreground,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.announcements_empty_description),
            style = type.body,
            color = colors.mutedForeground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (showRetry) {
            PillButton(
                label = stringResource(R.string.announcements_retry),
                onClick = onRetry,
                variant = PillVariant.Muted,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun SkeletonGrid() {
    val colors = HogwartsTheme.colors
    ItemGrid(count = 4) { _, modifier ->
        Box(modifier.heightIn(min = 128.dp).clip(HogwartsShapes.Card).background(colors.muted))
    }
}
