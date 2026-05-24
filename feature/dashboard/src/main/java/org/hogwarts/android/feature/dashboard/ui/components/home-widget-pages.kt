package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.SmartStackStatPage
import org.hogwarts.android.core.designsystem.theme.AppleBlue
import org.hogwarts.android.core.designsystem.theme.AppleIndigo
import org.hogwarts.android.core.designsystem.theme.AppleOrange
import org.hogwarts.android.core.designsystem.theme.AppleRed
import org.hogwarts.android.feature.dashboard.R
import org.hogwarts.android.feature.dashboard.ui.DashboardUiState
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Three pages slotted into the home Smart Stack widget. Each page paints its own
 * full-bleed colored card so the SmartStackWidget's scale/padding swipe animation
 * has a single visual surface to transform.
 *
 * The card's corner radius matches the outer Liquid Glass surface (28dp) so when the
 * pager is settled the page reads as edge-to-edge with the glass.
 */

private val PAGE_CARD_CORNER = 28.dp
private val PAGE_INNER_PADDING = 16.dp

/**
 * Mirrors the iOS Calendar app icon and the [R.drawable.ic_tile_schedule] drawable:
 * white card, full red day-of-week and big black day number stacked at the leading
 * edge, plus a small events line at the bottom that reflects [DashboardUiState.todayClasses].
 *
 * Day-of-week and number both come from the system clock so the card stays in sync
 * with the live calendar — `state` only feeds the events line.
 */
@Composable
fun BoxScope.TodayWidgetPage(state: DashboardUiState) {
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek
        .getDisplayName(TextStyle.FULL, Locale.getDefault())

    PageCard(
        gradient = listOf(Color.White, Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                ),
                color = AppleRed,
                maxLines = 1
            )

            Text(
                text = today.dayOfMonth.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-3).sp,
                    lineHeight = 72.sp
                ),
                color = Color.Black
            )

            // Spacer absorbs slack so the events line drops to the bottom of the card,
            // matching the iOS Calendar layout where event text sits below the number
            // with empty space between.
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (state.todayClasses > 0) {
                    stringResource(R.string.home_widget_events_today, state.todayClasses)
                } else {
                    stringResource(R.string.home_widget_no_events)
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.Black.copy(alpha = 0.55f),
                maxLines = 2
            )
        }
    }
}

@Composable
fun BoxScope.GlanceWidgetPage(state: DashboardUiState) {
    PageCard(
        gradient = listOf(
            AppleOrange.copy(alpha = 0.55f),
            AppleOrange.copy(alpha = 0.85f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.home_widget_glance_title),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                ),
                color = Color.White.copy(alpha = 0.9f)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniStat(
                        icon = { StatDot(color = Color.White) },
                        value = state.todayClasses.toString(),
                        label = stringResource(R.string.home_widget_classes)
                    )
                    MiniStat(
                        icon = { StatDot(color = Color.White) },
                        value = state.upcomingExams.toString(),
                        label = stringResource(R.string.home_widget_exams)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniStat(
                        icon = { StatDot(color = Color.White) },
                        value = state.pendingAttendance.toString(),
                        label = stringResource(R.string.home_widget_pending)
                    )
                    MiniStat(
                        icon = { StatDot(color = Color.White) },
                        value = state.pendingAssignments.toString(),
                        label = stringResource(R.string.home_widget_tasks)
                    )
                }
            }
        }
    }
}

@Composable
fun BoxScope.UpNextWidgetPage(state: DashboardUiState) {
    PageCard(
        gradient = listOf(
            AppleBlue.copy(alpha = 0.55f),
            AppleBlue.copy(alpha = 0.85f)
        )
    ) {
        SmartStackStatPage(
            title = stringResource(R.string.home_widget_activity_title),
            value = state.unreadNotifications.toString(),
            caption = if (state.unreadNotifications > 0) {
                stringResource(R.string.home_widget_unread_alerts)
            } else {
                stringResource(R.string.home_widget_no_alerts)
            },
            leadingIcon = {
                Icon(
                    imageVector = HogwartsIcons.Notifications,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

/**
 * Shared chrome for a Smart Stack page: a full-bleed gradient card (matching the outer
 * glass corner radius so settled = edge-to-edge), with the standard inner text padding.
 *
 * Page-specific content goes in [content] and is rendered on top of the gradient.
 */
@Composable
private fun BoxScope.PageCard(
    gradient: List<Color>,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(PAGE_CARD_CORNER))
            .background(Brush.verticalGradient(gradient))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(PAGE_INNER_PADDING),
        content = content
    )
}

@Composable
private fun MiniStat(
    icon: @Composable () -> Unit,
    value: String,
    label: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon()
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            ),
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1
        )
    }
}

@Composable
private fun StatDot(color: Color) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.85f))
    )
}
