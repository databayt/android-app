package org.hogwarts.android.feature.live.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.kit.BrandBanner
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.locale.currentLocale
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.live.R
import org.hogwarts.android.feature.live.domain.model.LiveSession
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * `/live` — what is on now, and what was.
 *
 * The landing's whole subject is NOW: a class running, one nearly over, one
 * about to begin. So today leads, and the past follows as the place a student
 * looks for the class they missed.
 *
 * Joining is not this screen's job. A card hands its href to the shell, which
 * resolves it the way it resolves every server-given link — the room page
 * mints its own ticket from `/api/mobile/conference/:id/join`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveHomeScreen(
    onOpenHref: (String) -> Unit,
    viewModel: LiveHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HogwartsTheme.colors

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item(key = "hero") {
                BrandBanner(
                    headline = liveHeadline(),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    actions = {
                        PillButton(
                            label = stringResource(R.string.live_sessions),
                            onClick = { onOpenHref("/live/sessions") },
                            variant = PillVariant.BrandWhite,
                        )
                        PillButton(
                            label = stringResource(R.string.live_schedule),
                            onClick = { onOpenHref("/live/schedule") },
                            variant = PillVariant.BrandGhost,
                        )
                    },
                )
            }

            if (state.isLoading) {
                item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (state.today.isNotEmpty()) {
                item(key = "today-heading") {
                    SectionHeading(stringResource(R.string.live_today))
                }
                items(state.today, key = { "today-${it.id}" }) { session ->
                    SessionRow(session = session, onOpen = { onOpenHref("/live/${session.id}") })
                }
            } else if (!state.isLoading && state.error == null) {
                item(key = "empty-today") {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                        Text(
                            text = stringResource(R.string.live_nothing_today),
                            style = HogwartsTheme.type.body,
                            color = colors.mutedForeground,
                        )
                    }
                }
            }

            if (state.past.isNotEmpty()) {
                item(key = "past-heading") {
                    SectionHeading(stringResource(R.string.live_recordings))
                }
                items(state.past, key = { "past-${it.id}" }) { session ->
                    SessionRow(session = session, onOpen = { onOpenHref("/live/${session.id}") })
                }
            }

            state.error?.let { message ->
                item(key = "error") {
                    Text(
                        text = message,
                        style = HogwartsTheme.type.caption,
                        color = colors.destructive,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String) {
    Text(
        text = title,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
        color = HogwartsTheme.colors.foreground,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

/**
 * One session, as the landing's strip draws it: the subject's own artwork, the
 * subject over its section, the teacher, and where the class is in its hour.
 */
@Composable
private fun SessionRow(session: LiveSession, onOpen: () -> Unit) {
    val colors = HogwartsTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(HogwartsShapes.Card)
            .border(1.dp, colors.border, HogwartsShapes.Card)
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (session.isLive) LiveDot()
                Text(
                    text = session.subjectName ?: session.title.orEmpty(),
                    style = HogwartsTheme.type.bodyMedium,
                    color = colors.foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            session.sectionName?.let { section ->
                Text(
                    text = section,
                    style = HogwartsTheme.type.caption,
                    color = colors.mutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = sessionLine(session),
                style = HogwartsTheme.type.caption,
                color = if (session.isLive) BrandColors.Green else colors.mutedForeground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            Modifier
                .size(56.dp)
                .clip(HogwartsShapes.Sm)
                .background(colors.muted),
            contentAlignment = Alignment.Center,
        ) {
            if (!session.subjectThumbnail.isNullOrBlank()) {
                AsyncImage(
                    model = session.subjectThumbnail,
                    contentDescription = session.subjectName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/** The green dot the web puts beside a class that is actually running. */
@Composable
private fun LiveDot() {
    Box(
        Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(BrandColors.Green),
    )
}

/**
 * The card's third line: the teacher, and the clock. A live class says how far
 * in it is, a scheduled one says when it starts, a finished one says it
 * finished — which is what the web's strip says in each state.
 */
@Composable
private fun sessionLine(session: LiveSession): String {
    val locale = currentLocale()
    val time = DateTimeFormatter.ofPattern("h:mm a", locale).withZone(ZoneId.systemDefault())
    val teacher = session.teacherName?.let { "$it · " }.orEmpty()
    // "Starts" is about the future, and a class keeps `status: scheduled`
    // long after its hour passed when nobody ever opened the room. Wording it
    // off the status alone told a student a class they missed on Tuesday was
    // about to begin, so the clock decides: only a session still ahead of now
    // is one that starts.
    val ahead = session.scheduledStart.isAfter(Instant.now())
    return when {
        session.isLive -> teacher + stringResource(R.string.live_in_progress)
        session.isScheduled && ahead -> teacher + stringResource(
            R.string.live_starts_at,
            time.format(session.scheduledStart),
        )
        else -> teacher + time.format(session.scheduledStart)
    }
}

/** "تجربة تفاعلية تجعل التعليم أقرب وأكثر تأثيرًا" — the site's own headline. */
@Composable
private fun liveHeadline(): AnnotatedString {
    val lead = stringResource(R.string.live_headline_lead)
    val rest = stringResource(R.string.live_headline_rest)
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(lead) }
        append(" ")
        append(rest)
    }
}
