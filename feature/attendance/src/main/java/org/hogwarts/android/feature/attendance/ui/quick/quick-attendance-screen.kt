package org.hogwarts.android.feature.attendance.ui.quick

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.ChoiceChip
import org.hogwarts.android.core.designsystem.kit.ChoiceChipRow
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.kit.SearchPill
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.attendance.R
import org.hogwarts.android.feature.attendance.domain.model.MarkStatus
import org.hogwarts.android.feature.attendance.domain.model.QuickSection
import org.hogwarts.android.feature.attendance.domain.model.RosterStudent
import org.hogwarts.android.feature.attendance.ui.AttendancePage
import org.hogwarts.android.feature.attendance.ui.attendanceFormat
import org.hogwarts.android.feature.attendance.ui.components.LoadFailedNote
import org.hogwarts.android.feature.attendance.ui.components.SkeletonBlock

@Composable
fun QuickAttendanceScreen(
    nav: @Composable () -> Unit,
    onOpenMessages: () -> Unit,
    viewModel: QuickAttendanceViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    QuickAttendanceView(
        state = state,
        nav = nav,
        onSelectSection = viewModel::selectSection,
        onCycle = viewModel::cycle,
        onSearch = viewModel::search,
        onSave = viewModel::save,
        onMarkAnother = viewModel::markAnother,
        onMessageGuardian = onOpenMessages,
        onRetry = viewModel::load,
    )
}

/** Teacher view of `/attendance` — mirrors `attendance/quick/content.tsx` at phone width. */
@Composable
fun QuickAttendanceView(
    state: QuickAttendanceUiState,
    nav: @Composable () -> Unit,
    onSelectSection: (String) -> Unit,
    onCycle: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSave: () -> Unit,
    onMarkAnother: () -> Unit,
    onMessageGuardian: () -> Unit,
    onRetry: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val format = attendanceFormat()

    if (state.sections != null && state.sections.isEmpty()) {
        AttendancePage(nav = nav) {
            if (state.loadFailed) {
                LoadFailedNote(onRetry)
            } else {
                BorderedNote(stringResource(R.string.attendance_quick_no_sections), verticalPadding = 48)
            }
        }
        return
    }

    AttendancePage(
        nav = nav,
        bottomBar = if (state.showSaveBar) {
            { SaveBar(state, onSave) }
        } else {
            null
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Title + date
            Column {
                Text(stringResource(R.string.attendance_quick_title), style = type.section, color = colors.foreground)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(format.longDay(state.today), style = type.body, color = colors.mutedForeground)
                    if (!state.isSchoolDay) {
                        Icon(
                            Icons.Outlined.EventBusy,
                            contentDescription = null,
                            tint = colors.mutedForeground,
                            modifier = Modifier.padding(start = 8.dp, end = 4.dp).size(14.dp),
                        )
                        Text(stringResource(R.string.attendance_quick_no_school_today), style = type.caption, color = colors.mutedForeground)
                    }
                }
            }

            // Section chips — current period first
            if (state.sections == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBlock(Modifier.width(112.dp).height(36.dp))
                    SkeletonBlock(Modifier.width(112.dp).height(36.dp))
                }
            } else {
                ChoiceChipRow {
                    state.sections.forEach { section ->
                        SectionChip(section, selected = section.id == state.selectedSectionId, onClick = { onSelectSection(section.id) })
                    }
                }
            }

            val saved = state.saved
            if (saved != null) {
                SavedCard(saved, onMarkAnother, onMessageGuardian)
            } else {
                Text(stringResource(R.string.attendance_quick_subtitle), style = type.caption, color = colors.mutedForeground)
                SearchPill(
                    value = state.search,
                    onValueChange = onSearch,
                    placeholder = stringResource(R.string.attendance_quick_search_placeholder),
                )
                when {
                    state.roster == null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(8) { SkeletonBlock(Modifier.fillMaxWidth().height(52.dp)) }
                    }
                    state.roster.isEmpty() -> BorderedNote(stringResource(R.string.attendance_quick_no_students), verticalPadding = 40)
                    else -> Column {
                        state.visibleRoster.forEachIndexed { index, student ->
                            if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                            RosterRow(student, onClick = { onCycle(student.studentId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionChip(section: QuickSection, selected: Boolean, onClick: () -> Unit) {
    val colors = HogwartsTheme.colors
    val format = attendanceFormat()
    ChoiceChip(
        label = section.name,
        selected = selected,
        onClick = onClick,
        labelMaxWidth = 144.dp,
        leading = if (section.isCurrent) {
            { Box(Modifier.size(8.dp).clip(CircleShape).background(BrandColors.Present)) }
        } else {
            null
        },
        trailing = {
            if (section.periodName != null) {
                Text(
                    text = if (section.isCurrent) stringResource(R.string.attendance_quick_now) else section.periodStart?.let(format::clock).orEmpty(),
                    style = HogwartsTheme.type.caption,
                    color = if (selected) LocalContentColor.current.copy(alpha = 0.8f) else colors.mutedForeground,
                )
            }
            if (section.fullyMarked) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.attendance_quick_marked),
                    tint = LocalContentColor.current,
                    modifier = Modifier.size(14.dp),
                )
            }
        },
    )
}

/** Phone roster row: a plain line with hairlines, the avatar and badge carry the mark. */
@Composable
private fun RosterRow(student: RosterStudent, onClick: () -> Unit) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val haptics = LocalHapticFeedback.current
    val statusLabel = when (student.status) {
        MarkStatus.Present -> stringResource(R.string.attendance_quick_present)
        MarkStatus.Absent -> stringResource(R.string.attendance_quick_absent)
        MarkStatus.Late -> stringResource(R.string.attendance_quick_late)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .semantics { stateDescription = statusLabel }
            .clickable(role = Role.Button) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val (ground, ink) = when (student.status) {
            MarkStatus.Absent -> BrandColors.Absent to Color.White
            MarkStatus.Late -> BrandColors.Late to Color.White
            MarkStatus.Present -> colors.muted to colors.mutedForeground
        }
        Box(Modifier.size(36.dp).clip(CircleShape).background(ground), contentAlignment = Alignment.Center) {
            when (student.status) {
                MarkStatus.Absent -> Icon(Icons.Outlined.PersonOff, contentDescription = null, tint = ink, modifier = Modifier.size(16.dp))
                MarkStatus.Late -> Icon(Icons.Outlined.Schedule, contentDescription = null, tint = ink, modifier = Modifier.size(16.dp))
                MarkStatus.Present -> Text(student.name.take(1), style = type.bodyMedium, color = ink)
            }
        }
        Text(
            student.name,
            style = type.bodyMedium,
            color = colors.foreground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        when (student.status) {
            MarkStatus.Absent -> LabelBadge(statusLabel, variant = BadgeVariant.Destructive)
            MarkStatus.Late -> LabelBadge(statusLabel, variant = BadgeVariant.Outline, tint = colors.warning)
            MarkStatus.Present -> Unit
        }
    }
}

@Composable
private fun SaveBar(state: QuickAttendanceUiState, onSave: () -> Unit) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val format = attendanceFormat()
    val counts = state.counts
    val present = stringResource(R.string.attendance_quick_present)
    val absent = stringResource(R.string.attendance_quick_absent)
    val late = stringResource(R.string.attendance_quick_late)
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.background.copy(alpha = 0.95f)),
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        if (state.saveError != null) {
            Text(
                stringResource(R.string.attendance_quick_save_failed),
                style = type.caption,
                color = colors.destructive,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val figure = SpanStyle(fontWeight = FontWeight.Medium)
            Text(
                buildAnnotatedString {
                    withStyle(figure.copy(color = colors.positive)) { append(format.count(counts.present)) }
                    withStyle(SpanStyle(color = colors.mutedForeground)) { append(" $present") }
                    if (counts.absent > 0) {
                        append(" · ")
                        withStyle(figure.copy(color = colors.destructive)) { append(format.count(counts.absent)) }
                        withStyle(SpanStyle(color = colors.mutedForeground)) { append(" $absent") }
                    }
                    if (counts.late > 0) {
                        append(" · ")
                        withStyle(figure.copy(color = colors.warning)) { append(format.count(counts.late)) }
                        withStyle(SpanStyle(color = colors.mutedForeground)) { append(" $late") }
                    }
                },
                style = type.body,
                color = colors.foreground,
                modifier = Modifier.weight(1f),
            )
            PillButton(
                label = stringResource(if (state.saving) R.string.attendance_quick_saving else R.string.attendance_quick_save),
                onClick = onSave,
                enabled = !state.saving,
                modifier = Modifier.height(44.dp),
            )
        }
    }
}

@Composable
private fun SavedCard(saved: SavedPanel, onMarkAnother: () -> Unit, onMessageGuardian: () -> Unit) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val format = attendanceFormat()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .background(colors.positive.copy(alpha = 0.06f))
            .border(1.dp, colors.positive.copy(alpha = 0.3f), HogwartsShapes.Card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = colors.positive, modifier = Modifier.size(20.dp))
            Text(
                stringResource(if (saved.queued) R.string.attendance_quick_queued_title else R.string.attendance_quick_saved_title),
                style = type.rowTitle.copy(fontWeight = FontWeight.Medium),
                color = colors.foreground,
            )
        }
        if (saved.queued) {
            Text(stringResource(R.string.attendance_quick_queued_hint), style = type.caption, color = colors.mutedForeground)
        }
        Text(
            stringResource(R.string.attendance_quick_saved_summary, format.count(saved.present), format.count(saved.absent), format.count(saved.late)),
            style = type.body,
            color = colors.foreground,
        )
        if (!saved.queued && saved.guardiansNotified != null) {
            Text(
                if (saved.guardiansNotified > 0) {
                    stringResource(R.string.attendance_quick_guardians_notified, format.count(saved.guardiansNotified))
                } else {
                    stringResource(R.string.attendance_quick_no_guardians_notified)
                },
                style = type.caption,
                color = colors.mutedForeground,
            )
        }
        if (saved.absentNames.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    stringResource(R.string.attendance_quick_absent_today),
                    style = type.caption.copy(fontWeight = FontWeight.Medium),
                    color = colors.foreground,
                )
                saved.absentNames.forEach { name ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(HogwartsShapes.Md)
                            .background(colors.background)
                            .border(1.dp, colors.border, HogwartsShapes.Md)
                            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(name, style = type.body, color = colors.foreground, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .clip(HogwartsShapes.Md)
                                .clickable(role = Role.Button, onClick = onMessageGuardian)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null, tint = colors.foreground, modifier = Modifier.size(14.dp))
                            Text(stringResource(R.string.attendance_quick_message_guardian), style = type.caption.copy(fontWeight = FontWeight.Medium), color = colors.foreground)
                        }
                    }
                }
            }
        }
        PillButton(
            label = stringResource(R.string.attendance_quick_mark_another),
            onClick = onMarkAnother,
            variant = PillVariant.Outline,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BorderedNote(text: String, verticalPadding: Int) {
    val colors = HogwartsTheme.colors
    Text(
        text,
        style = HogwartsTheme.type.body,
        color = colors.mutedForeground,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .border(1.dp, colors.border, HogwartsShapes.Card)
            .padding(horizontal = 16.dp, vertical = verticalPadding.dp),
    )
}
