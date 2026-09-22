package org.hogwarts.android.feature.live.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.icon.LucideIcons
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.live.R
import org.hogwarts.android.feature.live.domain.model.LandingPhase
import org.hogwarts.android.feature.live.domain.model.LandingSession
import org.hogwarts.android.feature.live.domain.model.LandingViewer

/** The strip's two weights — `size` in `session-row.tsx`. */
enum class RowSize { LEAD, BRIEF }

/**
 * Where a class sits, in the words this reader needs — `rowContext` in
 * `viewer.ts`. A reader across sections gets the section; a student, whose
 * rows are all one section, gets the grade.
 */
private fun rowContext(session: LandingSession, viewer: LandingViewer): String? =
    if (viewer.showsSection) session.sectionName ?: session.gradeName
    else session.gradeName ?: session.sectionName

/** Live goes straight into the room; anything else opens the session. */
private fun joinHref(session: LandingSession, viewer: LandingViewer): String =
    if (session.isLive && viewer.canJoin) "/live/${session.id}/room" else "/live/${session.id}"

/**
 * One class beside its picture — `session-row.tsx`.
 *
 * The art is an 80dp square in a 104dp column (12dp either side), the copy
 * padded 8dp. The lead is top-aligned on a phone, since its stacked rows run
 * taller than the picture; a brief row is shorter than it, so it is centred.
 *
 * Lead: subject + badge, chapter (two lines), lesson, teacher with portrait,
 * then the clock. Brief: subject + badge, then one line of teacher · clock.
 * Rows a session has nothing for are dropped, never left blank.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LandingSessionRow(
    session: LandingSession,
    viewer: LandingViewer,
    size: RowSize,
    onOpenHref: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    val isLead = size == RowSize.LEAD
    val context = rowContext(session, viewer)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button) { onOpenHref(joinHref(session, viewer)) }
            .padding(4.dp),
        verticalAlignment = if (isLead) Alignment.Top else Alignment.CenterVertically,
    ) {
        Box(Modifier.padding(horizontal = 12.dp)) {
            Art(
                session = session,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }

        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            FlowRow(
                modifier = Modifier.padding(bottom = if (isLead) 8.dp else 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = session.subjectName ?: session.title,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.foreground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (context != null) SecondaryBadge(context)
            }

            if (isLead) {
                session.chapterName?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = colors.foreground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                session.lessonName?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = colors.mutedForeground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (viewer.showsTeacher && session.teacherName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Portrait(session.teacherName, session.teacherPhotoUrl)
                            Text(
                                text = session.teacherName,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.foreground,
                            )
                        }
                    }
                    Status(session = session, compact = false)
                }
            } else {
                BriefMeta(session = session, viewer = viewer)
            }
        }
    }
}

/** The brief row's one meta line: teacher · clock, the name giving way first. */
@Composable
private fun BriefMeta(session: LandingSession, viewer: LandingViewer) {
    val colors = HogwartsTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (viewer.showsTeacher && session.teacherName.isNotBlank()) {
            Text(
                text = session.teacherName,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Text("·", fontSize = 12.sp, color = colors.mutedForeground.copy(alpha = 0.6f))
        }
        Status(session = session, compact = true)
    }
}

/**
 * Where the class is in its own clock. Running: the radio mark and "started"
 * (or "about to finish"), with the minute count on the lead only. Soon: the
 * word beside the time. Otherwise the time alone — a date, for a past class.
 */
@Composable
private fun Status(session: LandingSession, compact: Boolean) {
    val colors = HogwartsTheme.colors
    val running = session.phase.isRunning
    val number: String? = when {
        running && !compact && session.progressDone != null && session.progressTotal != null ->
            stringResource(R.string.live_phase_progress, session.progressDone, session.progressTotal)
        running -> null
        else -> session.scheduledStart.ifBlank { null }
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (running) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = LucideIcons.Radio,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(
                        if (session.phase == LandingPhase.ENDING) R.string.live_phase_ending else R.string.live_phase_started,
                    ),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    maxLines = 1,
                )
            }
        }
        if (session.phase == LandingPhase.SOON) {
            Text(
                text = stringResource(R.string.live_phase_soon),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.mutedForeground,
                maxLines = 1,
            )
        }
        if (number != null) {
            Text(
                text = number,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colors.mutedForeground,
                maxLines = 1,
            )
        }
    }
}

/**
 * One class under its picture — `session-card.tsx`, drawn by the catch-up
 * shelf and the recordings pair alike. A recording is offered only to a
 * reader allowed to watch one; the card then opens the recordings, not the
 * session.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LandingSessionCard(
    session: LandingSession,
    viewer: LandingViewer,
    onOpenHref: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    val context = rowContext(session, viewer)
    val watchable = session.hasRecording && viewer.canViewRecordings
    val href = if (watchable) "/live/${session.id}/recordings" else "/live/${session.id}"

    Column(modifier.clickable(role = Role.Button) { onOpenHref(href) }) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(HogwartsShapes.Card),
        ) {
            Art(session = session, modifier = Modifier.fillMaxSize())
            if (watchable) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(HogwartsShapes.Pill)
                        .background(colors.muted)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = LucideIcons.PlayFilled,
                        contentDescription = null,
                        tint = colors.foreground,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = stringResource(R.string.live_catch_up_recording),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = colors.foreground,
                    )
                }
            }
        }

        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = session.subjectName ?: session.title,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (context != null) SecondaryBadge(context)
            }
            session.lessonName?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = colors.mutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (viewer.showsTeacher && session.teacherName.isNotBlank()) {
                    Text(
                        text = session.teacherName,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.foreground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text("·", fontSize = 12.sp, color = colors.mutedForeground.copy(alpha = 0.6f))
                }
                Text(
                    text = session.scheduledStart,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = colors.mutedForeground,
                    maxLines = 1,
                )
            }
        }
    }
}

/** `<Badge variant="secondary" className="font-normal">`. */
@Composable
private fun SecondaryBadge(text: String) {
    val colors = HogwartsTheme.colors
    Text(
        text = text,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = colors.foreground,
        maxLines = 1,
        modifier = Modifier
            .clip(HogwartsShapes.Pill)
            .background(colors.muted)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/**
 * The subject's catalog artwork, or its own colour when there is none — a
 * normal state (no thumbnail, or no CDN), so the ground is a real fallback.
 */
@Composable
private fun Art(session: LandingSession, modifier: Modifier) {
    val fallback = session.color?.let { hex ->
        runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
    } ?: Color(0xFFE5E7EB)
    Box(modifier.background(fallback)) {
        if (!session.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = session.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** The byline's 24dp portrait: the photo, or two initials on a muted disc. */
@Composable
private fun Portrait(name: String, photoUrl: String?) {
    val colors = HogwartsTheme.colors
    Box(
        modifier = Modifier.size(24.dp).clip(CircleShape).background(colors.muted),
        contentAlignment = Alignment.Center,
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = name.split(Regex("\\s+")).filter { it.isNotBlank() }.take(2)
                    .joinToString("") { it.take(1) },
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.mutedForeground,
            )
        }
    }
}

/** One door on the role guide. */
data class GuideCardSpec(
    val key: String,
    val icon: ImageVector,
    val href: String,
    val title: Int,
    val description: Int,
)

/**
 * `cardsFor` in `role-guide.tsx`: only the doors this role can open, four at
 * most, in the web's order.
 */
fun guideCardsFor(viewer: LandingViewer): List<GuideCardSpec> = buildList {
    add(GuideCardSpec("sessions", LucideIcons.Table2, "/live/dashboard",
        R.string.live_guide_sessions_title, R.string.live_guide_sessions_description))
    if (viewer.canSchedule) {
        add(GuideCardSpec("schedule", LucideIcons.CalendarPlus, "/live/schedule",
            R.string.live_guide_schedule_title, R.string.live_guide_schedule_description))
    }
    // A student or guardian reaches a class from their own timetable far more
    // often than from this list, so that is the card they get.
    if (!viewer.canSchedule && viewer.canJoin) {
        val guardian = viewer.role == "GUARDIAN"
        add(GuideCardSpec("timetable",
            if (guardian) LucideIcons.ClipboardCheck else LucideIcons.Video,
            if (guardian) "/parent" else "/timetable",
            R.string.live_guide_timetable_title, R.string.live_guide_timetable_description))
    }
    if (viewer.canViewRecordings) {
        add(GuideCardSpec("recordings", LucideIcons.SquarePlay, "/live/dashboard?status=ended",
            R.string.live_guide_recordings_title, R.string.live_guide_recordings_description))
    }
    if (viewer.canConfigure) {
        add(GuideCardSpec("settings", LucideIcons.Settings, "/live/settings",
            R.string.live_guide_settings_title, R.string.live_guide_settings_description))
        add(GuideCardSpec("network", LucideIcons.Activity, "/live/network-test",
            R.string.live_guide_network_title, R.string.live_guide_network_description))
    }
}.take(4)

/** A bordered door: the icon, the title, one line on what is behind it. */
@Composable
fun GuideCard(card: GuideCardSpec, onOpenHref: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = HogwartsTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .border(1.dp, colors.border, HogwartsShapes.Card)
            .clickable(role = Role.Button) { onOpenHref(card.href) }
            .padding(20.dp),
    ) {
        Icon(
            imageVector = card.icon,
            contentDescription = null,
            tint = colors.foreground,
            modifier = Modifier.padding(bottom = 16.dp).size(24.dp),
        )
        Text(
            text = stringResource(card.title),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.foreground,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Text(
            text = stringResource(card.description),
            style = HogwartsTheme.type.body,
            color = colors.mutedForeground,
        )
    }
}
