package org.hogwarts.android.feature.announcements.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.InfoRow
import org.hogwarts.android.core.designsystem.kit.InfoRows
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.announcements.R
import org.hogwarts.android.feature.announcements.domain.model.Announcement

@Composable
fun AnnouncementDetailScreen(
    onBack: () -> Unit,
    viewModel: AnnouncementDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AnnouncementDetailContent(state = state, onBack = onBack, onRetry = viewModel::load)
}

/**
 * The phone branch of `announcements/detail.tsx`: read like the library's book
 * page — the meta line small over a large title, the body at reading size, the
 * facts as label/value rows.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnnouncementDetailContent(
    state: AnnouncementDetailUiState,
    onBack: () -> Unit,
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
            .padding(top = 16.dp, bottom = 40.dp),
    ) {
        BackButton(onBack)
        when (state) {
            AnnouncementDetailUiState.Loading -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Skeleton(Modifier.fillMaxWidth(0.5f).height(16.dp))
                Skeleton(Modifier.fillMaxWidth().height(36.dp))
                Spacer(Modifier.height(20.dp))
                Skeleton(Modifier.fillMaxWidth().height(192.dp))
            }
            is AnnouncementDetailUiState.Ready -> Reading(state.announcement, state.isOffline)
            AnnouncementDetailUiState.NotFound -> ErrorAlert(
                title = stringResource(R.string.announcements_error),
                description = stringResource(R.string.announcements_not_found),
            )
            is AnnouncementDetailUiState.Failed -> {
                ErrorAlert(
                    title = stringResource(R.string.announcements_not_saved_title),
                    description = stringResource(R.string.announcements_not_saved_hint),
                )
                PillButton(
                    label = stringResource(R.string.announcements_retry),
                    onClick = onRetry,
                    variant = PillVariant.Muted,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Reading(announcement: Announcement, isOffline: Boolean) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val format = announcementsFormat()
    val scope = scopeLabel(announcement.scope)
    val priority = detailPriorityLabel(announcement.priority)
    val status = stringResource(if (announcement.isPublished) R.string.announcements_published else R.string.announcements_draft)

    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        Column {
            Text(
                listOf(scope, format.longDate(announcement.createdAt)).filter { it.isNotEmpty() }.joinToString(" · "),
                style = type.body,
                color = colors.mutedForeground,
            )
            Text(
                announcement.title,
                style = type.bannerHeadline.copy(fontSize = 28.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold),
                color = colors.foreground,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (!announcement.isPublished || announcement.isNotablePriority) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    if (!announcement.isPublished) {
                        LabelBadge(stringResource(R.string.announcements_draft), variant = BadgeVariant.Outline)
                    }
                    if (announcement.isNotablePriority) {
                        // detail.tsx: both high and urgent take the destructive badge.
                        LabelBadge(priority, variant = BadgeVariant.Destructive)
                    }
                }
            }
            if (isOffline) {
                Text(
                    stringResource(R.string.announcements_stale_copy),
                    style = type.caption,
                    color = colors.mutedForeground,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }

        if (announcement.body.isNotBlank()) {
            Text(
                announcement.body,
                style = type.body.copy(fontSize = 17.sp, lineHeight = 32.sp),
                color = colors.foreground,
            )
        } else {
            Text(
                stringResource(R.string.announcements_no_content),
                style = type.body.copy(fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 24.sp),
                color = colors.mutedForeground,
            )
        }

        InfoRows(
            heading = stringResource(R.string.announcements_details),
            rows = listOf(
                InfoRow(stringResource(R.string.announcements_scope), scope, key = "scope"),
                InfoRow(stringResource(R.string.announcements_target_role), announcement.targetRole?.let { roleLabel(it) }, key = "role"),
                InfoRow(stringResource(R.string.announcements_priority), priority, key = "priority"),
                InfoRow(stringResource(R.string.announcements_status), status, key = "status"),
                InfoRow(stringResource(R.string.announcements_created_at), format.shortDate(announcement.createdAt), key = "created"),
                InfoRow(stringResource(R.string.announcements_updated_at), format.shortDate(announcement.updatedAt), key = "updated"),
            ),
        )
    }
}

/** `Button variant="ghost" size="sm"`: muted, rounded-full, h-9, pulled to the edge (`-ms-3`), mb-3. */
@Composable
private fun BackButton(onBack: () -> Unit) {
    val colors = HogwartsTheme.colors
    Row(
        modifier = Modifier
            .offset(x = (-12).dp)
            .padding(bottom = 12.dp)
            .height(36.dp)
            .clip(HogwartsShapes.Pill)
            .clickable(role = Role.Button, onClick = onBack)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(16.dp))
        Text(stringResource(R.string.announcements_back), style = HogwartsTheme.type.bodyMedium, color = colors.mutedForeground)
    }
}

/** `Alert variant="destructive"`: bordered box, destructive ink, icon, title over description. */
@Composable
private fun ErrorAlert(title: String, description: String) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Lg)
            .border(1.dp, colors.border, HogwartsShapes.Lg)
            .background(colors.card)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = colors.destructive, modifier = Modifier.padding(top = 2.dp).size(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = type.bodyMedium, color = colors.destructive)
            Text(description, style = type.body, color = colors.destructive.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun Skeleton(modifier: Modifier) {
    Box(modifier.clip(HogwartsShapes.Md).background(HogwartsTheme.colors.muted))
}
