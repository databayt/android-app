package org.hogwarts.android.feature.dashboard.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import org.hogwarts.android.core.designsystem.kit.BrandPill
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.dashboard.R
import org.hogwarts.android.feature.dashboard.data.remote.NextActionDto

private const val HOLD_MS = 10_000L

/**
 * The green next-action card — mirrors hogwarts `dashboard/next-action-client.tsx`.
 *
 * Up to four ranked actions, one at a time, advancing every 10s while the
 * screen is resumed. The headline is a fixed two-line box so the card never
 * changes height on a timer; the phrase in `**…**` is set bold. "Open" follows
 * the action; "Acknowledge" dismisses it locally.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NextActionBanner(
    actions: List<NextActionDto>,
    onOpen: (href: String) -> Unit,
    onAcknowledge: (NextActionDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (actions.isEmpty()) return
    var index by remember(actions) { mutableIntStateOf(0) }
    val action = actions[index % actions.size]
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    LaunchedEffect(actions.size) {
        if (actions.size < 2) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(HOLD_MS)
                index = (index + 1) % actions.size
            }
        }
    }

    val type = HogwartsTheme.type

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Banner)
            .background(BrandColors.Green)
            .padding(horizontal = 32.dp, vertical = 48.dp),
    ) {
        AnimatedContent(
            targetState = action,
            transitionSpec = {
                val ms = if (reduceMotion) 0 else 340
                fadeIn(tween(ms)) togetherWith fadeOut(tween(ms))
            },
            label = "next-action",
        ) { current ->
            Text(
                text = headline(nextActionTemplate(current.kind), current.mark),
                style = type.bannerHeadline,
                color = BrandColors.Ink,
                // Always exactly two lines tall, measured with the real font, so
                // the card never changes height as the actions rotate.
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        FlowRow(
            modifier = Modifier.padding(top = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BrandPill(stringResource(R.string.dash_next_open), onClick = { onOpen(action.href) })
            BrandPill(stringResource(R.string.dash_next_acknowledge), onClick = { onAcknowledge(action) }, ghost = true)
        }
    }
}

@Composable
private fun nextActionTemplate(kind: String): String? = when (kind) {
    "assignmentOverdue" -> stringResource(R.string.dash_next_assignment_overdue)
    "assignmentDue" -> stringResource(R.string.dash_next_assignment_due)
    "nextClass" -> stringResource(R.string.dash_next_next_class)
    "pendingGrading" -> stringResource(R.string.dash_next_pending_grading)
    "attendanceDue" -> stringResource(R.string.dash_next_attendance_due)
    "childOverdue" -> stringResource(R.string.dash_next_child_overdue)
    "childPending" -> stringResource(R.string.dash_next_child_pending)
    "pendingPayments" -> stringResource(R.string.dash_next_pending_payments)
    "overdueInvoices" -> stringResource(R.string.dash_next_overdue_invoices)
    "urgentTask" -> stringResource(R.string.dash_next_urgent_task)
    "pendingRequests" -> stringResource(R.string.dash_next_pending_requests)
    "activeIssues" -> stringResource(R.string.dash_next_active_issues)
    "pendingApprovals" -> stringResource(R.string.dash_next_pending_approvals)
    else -> null
}

/**
 * Port of the web's `toPieces`: the dictionary sentence wraps the bold part in
 * `**…**` and marks the data with `{mark}`. Without a template the mark alone
 * is the headline, bold.
 */
internal fun headline(template: String?, mark: String): AnnotatedString = buildAnnotatedString {
    val bold = SpanStyle(fontWeight = FontWeight.Bold)
    if (template == null) {
        pushStyle(bold); append(mark); pop()
        return@buildAnnotatedString
    }
    if (template.contains("**")) {
        template.split("**").forEachIndexed { i, segment ->
            val text = segment.replace("{mark}", mark)
            if (i % 2 == 1) { pushStyle(bold); append(text); pop() } else append(text)
        }
        return@buildAnnotatedString
    }
    val parts = template.split("{mark}", limit = 2)
    if (parts.size < 2) {
        append(template)
    } else {
        pushStyle(bold); append(parts[0] + mark); pop()
        append(parts[1])
    }
}
