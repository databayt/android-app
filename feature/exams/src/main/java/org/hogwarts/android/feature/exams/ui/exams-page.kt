package org.hogwarts.android.feature.exams.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.kit.AppTile
import org.hogwarts.android.core.designsystem.kit.AppTileItem
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.PageNav
import org.hogwarts.android.core.designsystem.kit.PageNavItem
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.feature.exams.navigation.ExamsUpcoming
import org.hogwarts.android.feature.exams.navigation.QuestionBank

/** The section's sub-pages — `getExamTabsForRole` in `exams/lib/permissions.ts`. */
enum class ExamsTab(val href: String) {
    Overview("/exams"),
    Manage("/exams/manage"),
    Qbank("/exams/qbank"),
    Practice("/exams/qbank"),
    Generate("/exams/generate"),
    Mark("/exams/mark"),
    Results("/exams/result"),
    Quiz("/exams/quiz"),
    Mock("/exams/mock"),
    Upcoming("/exams/upcoming"),
    Templates("/exams/templates"),
}

internal fun tabsForRole(role: UserRole?): List<ExamsTab> = when (role) {
    UserRole.DEVELOPER, UserRole.ADMIN, UserRole.TEACHER -> listOf(
        ExamsTab.Overview, ExamsTab.Manage, ExamsTab.Qbank, ExamsTab.Generate, ExamsTab.Mark,
        ExamsTab.Results, ExamsTab.Quiz, ExamsTab.Mock, ExamsTab.Upcoming, ExamsTab.Templates,
    )
    UserRole.STUDENT -> listOf(ExamsTab.Overview, ExamsTab.Practice, ExamsTab.Results, ExamsTab.Quiz, ExamsTab.Mock, ExamsTab.Upcoming)
    UserRole.GUARDIAN -> listOf(ExamsTab.Overview, ExamsTab.Results, ExamsTab.Upcoming)
    UserRole.ACCOUNTANT -> listOf(ExamsTab.Overview, ExamsTab.Results)
    UserRole.STAFF -> listOf(ExamsTab.Overview, ExamsTab.Upcoming)
    else -> listOf(ExamsTab.Overview)
}

/** Roles the question-bank route answers (`hasRole(auth, "TEACHER", "ADMIN", "DEVELOPER")`). */
internal fun canReadQuestionBank(role: UserRole?): Boolean =
    role == UserRole.TEACHER || role == UserRole.ADMIN || role == UserRole.DEVELOPER

/**
 * Where a web path of this section leads: a native route when one mirrors it
 * for [role], else null (the web page). Overview is the landing itself.
 */
internal fun nativeRouteFor(href: String, role: UserRole?): Any? = when (href) {
    ExamsTab.Upcoming.href -> ExamsUpcoming
    ExamsTab.Qbank.href -> if (canReadQuestionBank(role)) QuestionBank else null
    else -> null
}

/** Opens a section path: native when mirrored, else through the shell's web handoff. */
internal class ExamsLinks(
    private val role: UserRole?,
    private val onNavigate: (Any) -> Unit,
    private val onOpenHref: (String) -> Unit,
) {
    fun open(href: String) {
        nativeRouteFor(href, role)?.let(onNavigate) ?: onOpenHref(href)
    }
}

@Composable
internal fun ExamsTabs(role: UserRole?, selected: ExamsTab, links: ExamsLinks) {
    val tabs = tabsForRole(role)
    PageNav(
        items = tabs.map { PageNavItem(it.name, stringResource(it.labelRes())) },
        selectedKey = selected.name,
        onSelect = { item ->
            val tab = ExamsTab.valueOf(item.key)
            if (tab != selected) links.open(tab.href)
        },
    )
}

private fun ExamsTab.labelRes(): Int = when (this) {
    ExamsTab.Overview -> R.string.exams_nav_overview
    ExamsTab.Manage -> R.string.exams_nav_manage
    ExamsTab.Qbank -> R.string.exams_nav_qbank
    ExamsTab.Practice -> R.string.exams_nav_practice
    ExamsTab.Generate -> R.string.exams_nav_generate
    ExamsTab.Mark -> R.string.exams_nav_mark
    ExamsTab.Results -> R.string.exams_nav_record
    ExamsTab.Quiz -> R.string.exams_nav_quiz
    ExamsTab.Mock -> R.string.exams_nav_mock
    ExamsTab.Upcoming -> R.string.exams_nav_upcoming
    ExamsTab.Templates -> R.string.exams_nav_templates
}

/**
 * The page frame of `exams/layout.tsx`: 16dp gutters, the tabs, then the page
 * 24dp below (`space-y-6`). The shell draws the platform header above.
 */
@Composable
internal fun ExamsPage(
    tabs: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(HogwartsTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp),
    ) {
        if (tabs != null) {
            tabs()
            Spacer(Modifier.height(24.dp))
        }
        content()
    }
}

/** A placeholder while a block loads — `ui/skeleton.tsx`: a muted rounded block. */
@Composable
internal fun SkeletonBlock(modifier: Modifier = Modifier) {
    Box(modifier.clip(HogwartsShapes.Banner).background(HogwartsTheme.colors.muted))
}

/** A failed load with a retry — the section's error card. */
@Composable
internal fun LoadFailedNote(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val colors = HogwartsTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .background(colors.destructive.copy(alpha = 0.06f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = colors.destructive, modifier = Modifier.size(20.dp))
        Text(stringResource(R.string.exams_unable_to_load), style = HogwartsTheme.type.bodyMedium, color = colors.destructive, modifier = Modifier.weight(1f))
        PillButton(stringResource(R.string.exams_retry), onClick = onRetry, variant = PillVariant.Outline)
    }
}

/** The centred "no record" card: icon, title, one line under it. */
@Composable
internal fun NoticeCard(icon: ImageVector, title: String, description: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    val colors = HogwartsTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .background(colors.muted)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(title, style = HogwartsTheme.type.section, color = colors.foreground, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(description, style = HogwartsTheme.type.body, color = colors.mutedForeground, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

/** `text-muted-foreground py-6 text-center text-sm` — a list's empty line. */
@Composable
internal fun EmptyLine(text: String) {
    Text(
        text,
        style = HogwartsTheme.type.body,
        color = HogwartsTheme.colors.mutedForeground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
    )
}

/** An [AppTile] row of four where a tile may carry its own face (the calendar date). */
@Composable
internal fun TileRow(tiles: List<Pair<AppTileItem, (@Composable () -> Unit)?>>) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        tiles.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { (item, face) -> AppTile(item, Modifier.weight(1f), face = face) }
                repeat(4 - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

/** A result's figure: bold percentage over `obtained/total`, then its grade badge. */
@Composable
internal fun ResultTrailing(percentage: Double?, score: Double?, maxScore: Double?, grade: String?) {
    val colors = HogwartsTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                ltr(percentage?.let(::percentLabel) ?: "—"),
                style = HogwartsTheme.type.section.copy(fontWeight = FontWeight.Bold),
                color = colors.foreground,
            )
            Text(ltr("${mark(score)}/${mark(maxScore)}"), style = HogwartsTheme.type.caption, color = colors.mutedForeground)
        }
        if (!grade.isNullOrBlank()) {
            val pct = percentage ?: 0.0
            LabelBadge(
                label = grade,
                variant = when {
                    pct >= 80 -> BadgeVariant.Default
                    pct >= 50 -> BadgeVariant.Secondary
                    else -> BadgeVariant.Destructive
                },
            )
        }
    }
}
