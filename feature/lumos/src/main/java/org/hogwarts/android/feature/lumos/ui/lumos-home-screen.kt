package org.hogwarts.android.feature.lumos.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.kit.BrandBanner
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.lumos.R
import org.hogwarts.android.feature.lumos.ui.components.CourseShelf
import org.hogwarts.android.feature.lumos.ui.components.FeatureCard
import org.hogwarts.android.feature.lumos.ui.components.TeachingCard

import androidx.compose.foundation.layout.Column
/**
 * `/lumos` — the LMS front door, as the site builds it: the brand hero, the
 * four cards that say what this is, the shelves, and the invitation to teach.
 *
 * `content.tsx`'s order, minus the three marketing sections below the teaching
 * hero (curriculum, reasons, how-to-begin). Those are long-form copy for a
 * page a visitor lands on; this screen is reached from a menu by someone
 * already inside the school.
 *
 * The top bar is gone, as on every screen rebuilt on the web's layout: the
 * platform header is the chrome.
 */
@Composable
fun LumosHomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToMyLearning: () -> Unit,
    onNavigateToCourse: (String) -> Unit,
    onNavigateToTeacherVideos: () -> Unit = {},
    viewModel: LumosHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = HogwartsTheme.colors

    LazyColumn(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item(key = "hero") {
            BrandBanner(
                headline = lumosHeadline(),
                modifier = Modifier.padding(horizontal = 16.dp),
                actions = {
                    PillButton(
                        label = stringResource(R.string.lumos_nav_courses),
                        onClick = onNavigateToCourses,
                        variant = PillVariant.BrandWhite,
                    )
                    PillButton(
                        label = stringResource(R.string.lumos_nav_dashboard),
                        onClick = onNavigateToMyLearning,
                        variant = PillVariant.BrandGhost,
                    )
                },
            )
        }

        item(key = "features") {
            val features = listOf(
                Triple("📚", R.string.lumos_feature_curated_title, R.string.lumos_feature_curated_desc),
                Triple("🎮", R.string.lumos_feature_interactive_title, R.string.lumos_feature_interactive_desc),
                Triple("📊", R.string.lumos_feature_progress_title, R.string.lumos_feature_progress_desc),
                Triple("👥", R.string.lumos_feature_community_title, R.string.lumos_feature_community_desc),
            )
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                features.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { (emoji, title, description) ->
                            FeatureCard(
                                emoji = emoji,
                                title = stringResource(title),
                                description = stringResource(description),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isLoading && uiState.featuredCourses.isEmpty()) {
            item(key = "loading") {
                Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        if (uiState.continueWatching.isNotEmpty()) {
            item(key = "continue") {
                CourseShelf(
                    title = stringResource(R.string.lumos_continue_watching),
                    courses = uiState.continueWatching,
                    onOpenCourse = onNavigateToCourse,
                )
            }
        }

        if (uiState.featuredCourses.isNotEmpty()) {
            item(key = "hot") {
                CourseShelf(
                    title = stringResource(R.string.lumos_hot_releases),
                    courses = uiState.featuredCourses,
                    onOpenCourse = onNavigateToCourse,
                )
            }
        }

        item(key = "teach") {
            TeachingCard(
                title = stringResource(R.string.lumos_teaching_title),
                description = stringResource(R.string.lumos_teaching_desc),
                modifier = Modifier.padding(horizontal = 16.dp),
                actions = {
                    PillButton(
                        label = stringResource(R.string.lumos_teaching_cta),
                        onClick = onNavigateToTeacherVideos,
                    )
                    PillButton(
                        label = stringResource(R.string.lumos_teaching_learn_more),
                        onClick = onNavigateToCourses,
                        variant = PillVariant.Muted,
                    )
                },
            )
        }

        uiState.error?.let { message ->
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

/** "لوموس أضئ نورًا." — the name carries the weight, the promise follows. */
@Composable
private fun lumosHeadline(): AnnotatedString {
    val lead = stringResource(R.string.lumos_headline_lead)
    val rest = stringResource(R.string.lumos_headline_rest)
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(lead) }
        append(" ")
        append(rest)
    }
}
