package org.hogwarts.android.feature.live.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.icon.LucideIcons
import org.hogwarts.android.core.designsystem.kit.BrandBanner
import org.hogwarts.android.core.designsystem.kit.BrandPill
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.live.R
import org.hogwarts.android.feature.live.domain.model.LandingPolicy
import org.hogwarts.android.feature.live.domain.model.LandingViewer
import org.hogwarts.android.feature.live.domain.model.LiveLanding
import org.hogwarts.android.feature.live.ui.components.GuideCard
import org.hogwarts.android.feature.live.ui.components.LandingSessionCard
import org.hogwarts.android.feature.live.ui.components.LandingSessionRow
import org.hogwarts.android.feature.live.ui.components.RowSize
import org.hogwarts.android.feature.live.ui.components.guideCardsFor

/**
 * `/live` — the web's landing (`live/landing/content.tsx`), section for section:
 *
 * 1. the hero, whose headline and buttons depend on whether the school
 *    teaches online and on what this reader may do;
 * 2. the now strip — a lead class and two under it — only while online;
 * 3. the catch-up shelf of classes this reader missed;
 * 4. the two recordings ranked for them;
 * 5. what this role can do here.
 *
 * Every section is gated the way the web gates it, on the same rows and the
 * same viewer rules, because both read `loadLiveLanding`.
 *
 * Not mirrored: the admin readiness band and the offline admin's setup steps.
 * Both are configuration surfaces, and an admin reaches them through the
 * Settings card, which hands off to the web.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveHomeScreen(
    onOpenHref: (String) -> Unit,
    viewModel: LiveHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HogwartsTheme.colors
    val landing = state.landing

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        ) {
            if (landing == null) {
                item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(vertical = 80.dp), Alignment.Center) {
                        if (state.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text(
                                text = state.error.orEmpty(),
                                style = HogwartsTheme.type.caption,
                                color = colors.destructive,
                            )
                        }
                    }
                }
                return@LazyColumn
            }

            item(key = "hero") {
                LiveHero(
                    viewer = landing.viewer,
                    policy = landing.policy,
                    onOpenHref = onOpenHref,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
                )
            }

            if (landing.policy.isOnline) {
                nowStrip(landing, onOpenHref)
            }

            if (landing.catchUp.isNotEmpty()) {
                item(key = "catch-up") {
                    SectionWithIcon(icon = { LucideIcons.History }, label = stringResource(R.string.live_catch_up_title)) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(landing.catchUp, key = { "catch-${it.id}" }) { session ->
                                LandingSessionCard(
                                    session = session,
                                    viewer = landing.viewer,
                                    onOpenHref = onOpenHref,
                                    modifier = Modifier.width(224.dp),
                                )
                            }
                        }
                    }
                }
            }

            if (landing.recordings.isNotEmpty()) {
                item(key = "recordings") {
                    SectionWithIcon(icon = { LucideIcons.Video }, label = stringResource(R.string.live_action_recordings)) {
                        // Two across at every width, as the web draws the pair.
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            landing.recordings.take(2).forEach { session ->
                                LandingSessionCard(
                                    session = session,
                                    viewer = landing.viewer,
                                    onOpenHref = onOpenHref,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (landing.recordings.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }

            val cards = guideCardsFor(landing.viewer)
            if (cards.isNotEmpty()) {
                item(key = "guide-title") {
                    Text(
                        text = stringResource(R.string.live_guide_title),
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.foreground,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                    )
                }
                items(cards, key = { "guide-${it.key}" }) { card ->
                    GuideCard(
                        card = card,
                        onOpenHref = onOpenHref,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    )
                }
            }
        }
    }
}

/**
 * The strip: the running class as the lead, then two more — anything else live
 * before anything merely scheduled. Three rows at most; the rest is one tap
 * away on the sessions list. Empty, it is a dashed box that says so.
 */
private fun LazyListScope.nowStrip(
    landing: LiveLanding,
    onOpenHref: (String) -> Unit,
) {
    val featured = landing.live.firstOrNull()
    val rest = landing.live.drop(1) + landing.upcoming
    val rows = (if (featured != null) listOf(featured) + rest else rest).take(3)

    if (rows.isEmpty()) {
        item(key = "now-empty") { NowEmpty() }
        return
    }

    rows.forEachIndexed { index, session ->
        item(key = "now-${session.id}") {
            LandingSessionRow(
                session = session,
                viewer = landing.viewer,
                size = if (index == 0) RowSize.LEAD else RowSize.BRIEF,
                onOpenHref = onOpenHref,
                // The row is pulled 8dp into the gutter and padded 4dp, and the
                // art column pads 12dp — so the art sits 24dp from the edge.
                modifier = Modifier.padding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = if (index == rows.lastIndex) 0.dp else 32.dp,
                ),
            )
        }
    }
    item(key = "now-rule") { SectionRule(top = 32.dp) }
}

/** `border-b` under a section, then the next section's `mb-16`. */
@Composable
private fun SectionRule(top: Dp) {
    HorizontalDivider(
        color = HogwartsTheme.colors.border,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = top, bottom = 64.dp),
    )
}

/**
 * The shelf and the recordings are headed by an icon alone — the cards under
 * them already say what they are. The words stay for TalkBack.
 */
@Composable
private fun SectionWithIcon(
    icon: () -> ImageVector,
    label: String,
    content: @Composable () -> Unit,
) {
    Column {
        Icon(
            imageVector = icon(),
            contentDescription = label,
            tint = HogwartsTheme.colors.foreground,
            modifier = Modifier.padding(horizontal = 16.dp).size(24.dp),
        )
        Box(Modifier.padding(top = 16.dp)) { content() }
        SectionRule(top = 64.dp)
    }
}

@Composable
private fun NowEmpty() {
    val colors = HogwartsTheme.colors
    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 64.dp)
            .fillMaxWidth()
            .dashedBorder(colors.border)
            .padding(vertical = 64.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = LucideIcons.CalendarClock,
            contentDescription = null,
            tint = colors.mutedForeground,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = stringResource(R.string.live_now_empty_title),
            style = HogwartsTheme.type.bodyMedium,
            color = colors.foreground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.live_now_empty_description),
            style = HogwartsTheme.type.body,
            color = colors.mutedForeground,
            textAlign = TextAlign.Center,
        )
    }
}

/** A 36dp-radius dashed outline — `rounded-[36px] border border-dashed`. */
private fun Modifier.dashedBorder(color: Color): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(36.dp.toPx()),
        style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
        ),
    )
}

/**
 * `status-hero.tsx`. Online, the headline alone over two pills; offline, the
 * offline line with a sentence under it and — for an admin only — the one
 * button that turns it on.
 */
@Composable
private fun LiveHero(
    viewer: LandingViewer,
    policy: LandingPolicy,
    onOpenHref: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val headline: AnnotatedString = if (policy.isOnline) {
        markedHeadline(
            template = stringResource(R.string.live_hero_title, MARK_SLOT),
            mark = stringResource(R.string.live_hero_title_mark),
        )
    } else {
        AnnotatedString(stringResource(R.string.live_hero_offline))
    }
    val body = if (policy.isOnline) {
        null
    } else if (viewer.canConfigure) {
        stringResource(R.string.live_hero_offline_admin)
    } else {
        stringResource(R.string.live_hero_offline_other)
    }

    // Resolved outside the slot: the pills' labels are composable reads.
    val primary: Pair<String, String>? = when {
        !policy.isOnline && viewer.canConfigure ->
            stringResource(R.string.live_action_turn_on) to "/live/settings"
        !policy.isOnline -> null
        else -> stringResource(R.string.live_action_sessions) to "/live/dashboard"
    }
    // First match wins: whoever can create a class is here to create one,
    // whoever cannot is most likely here for a lesson they missed.
    val secondary: Pair<String, String>? = when {
        !policy.isOnline -> null
        viewer.canSchedule -> stringResource(R.string.live_action_schedule) to "/live/schedule"
        viewer.canViewRecordings ->
            stringResource(R.string.live_action_recordings) to "/live/dashboard?status=ended"
        viewer.canConfigure -> stringResource(R.string.live_action_settings) to "/live/settings"
        else -> null
    }

    BrandBanner(
        headline = headline,
        body = body,
        modifier = modifier,
        actions = if (primary == null && secondary == null) null else {
            {
                primary?.let { (label, href) -> BrandPill(label = label, onClick = { onOpenHref(href) }) }
                secondary?.let { (label, href) ->
                    BrandPill(label = label, onClick = { onOpenHref(href) }, ghost = true)
                }
            }
        },
    )
}

/** The dictionary's `{mark}`, carried through a string resource's `%1$s`. */
private const val MARK_SLOT = "\u0000"

/** The headline with its phrase in bold — `MarkedHeadline` in status-hero.tsx. */
private fun markedHeadline(template: String, mark: String): AnnotatedString {
    val parts = template.split(MARK_SLOT)
    if (parts.size != 2) return AnnotatedString(template.replace(MARK_SLOT, mark))
    return buildAnnotatedString {
        append(parts[0])
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(mark) }
        append(parts[1])
    }
}
