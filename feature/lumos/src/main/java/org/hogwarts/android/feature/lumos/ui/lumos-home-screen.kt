package org.hogwarts.android.feature.lumos.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.lumos.R
import org.hogwarts.android.feature.lumos.ui.components.LumosAssets
import org.hogwarts.android.feature.lumos.ui.components.LumosBeginStep
import org.hogwarts.android.feature.lumos.ui.components.LumosContinueWatchingSection
import org.hogwarts.android.feature.lumos.ui.components.LumosCurriculumFeature
import org.hogwarts.android.feature.lumos.ui.components.LumosCurriculumSection
import org.hogwarts.android.feature.lumos.ui.components.LumosFeatureItem
import org.hogwarts.android.feature.lumos.ui.components.LumosFeaturesSection
import org.hogwarts.android.feature.lumos.ui.components.LumosHeroSection
import org.hogwarts.android.feature.lumos.ui.components.LumosHotReleasesSection
import org.hogwarts.android.feature.lumos.ui.components.LumosHowToBeginSection
import org.hogwarts.android.feature.lumos.ui.components.LumosTeachingHeroSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumosHomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToMyLearning: () -> Unit,
    onNavigateToCourse: (String) -> Unit,
    onNavigateToTeacherVideos: () -> Unit = {},
    viewModel: LumosHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.lumos_home_top_bar_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // 1. Hero with Education Animation
                item {
                    LumosHeroSection(
                        title = stringResource(R.string.lumos_home_hero_title),
                        tagline = stringResource(R.string.lumos_home_hero_tagline),
                        exploreLabel = stringResource(R.string.lumos_home_explore_courses),
                        dashboardLabel = stringResource(R.string.lumos_home_dashboard),
                        showDashboardCta = uiState.isAuthenticated,
                        onExplore = onNavigateToCourses,
                        onDashboard = onNavigateToMyLearning
                    )
                }

                // 2. Core Features (4 pillars)
                item {
                    LumosFeaturesSection(
                        features = listOf(
                            LumosFeatureItem(
                                title = stringResource(R.string.lumos_home_feature_curated_title),
                                description = stringResource(R.string.lumos_home_feature_curated_desc),
                                iconUrl = LumosAssets.CURATED_COURSES_ICON_URL
                            ),
                            LumosFeatureItem(
                                title = stringResource(R.string.lumos_home_feature_interactive_title),
                                description = stringResource(R.string.lumos_home_feature_interactive_desc),
                                iconUrl = LumosAssets.INTERACTIVE_LEARNING_ICON_URL
                            ),
                            LumosFeatureItem(
                                title = stringResource(R.string.lumos_home_feature_progress_title),
                                description = stringResource(R.string.lumos_home_feature_progress_desc),
                                iconUrl = LumosAssets.PROGRESS_TRACKING_ICON_URL
                            ),
                            LumosFeatureItem(
                                title = stringResource(R.string.lumos_home_feature_community_title),
                                description = stringResource(R.string.lumos_home_feature_community_desc),
                                iconUrl = LumosAssets.COMMUNITY_ICON_URL
                            )
                        )
                    )
                }

                // 3. Continue Watching (if any)
                if (uiState.continueWatching.isNotEmpty()) {
                    item {
                        LumosContinueWatchingSection(
                            title = stringResource(R.string.lumos_home_continue_watching),
                            items = uiState.continueWatching,
                            onItemClick = { course -> onNavigateToCourse(course.id) }
                        )
                    }
                }

                // 4. Featured Catalog Releases
                if (uiState.featuredCourses.isNotEmpty()) {
                    item {
                        LumosHotReleasesSection(
                            title = stringResource(R.string.lumos_home_featured_releases),
                            courses = uiState.featuredCourses,
                            onCourseClick = { course -> onNavigateToCourse(course.id) }
                        )
                    }
                }

                // 5. Expand Curriculum Section
                item {
                    LumosCurriculumSection(
                        title = stringResource(R.string.lumos_home_curriculum_title),
                        description = stringResource(R.string.lumos_home_curriculum_description),
                        features = listOf(
                            LumosCurriculumFeature(
                                title = stringResource(R.string.lumos_home_curriculum_world_class_title),
                                description = stringResource(R.string.lumos_home_curriculum_world_class_desc),
                                emoji = "📚"
                            ),
                            LumosCurriculumFeature(
                                title = stringResource(R.string.lumos_home_curriculum_guided_title),
                                description = stringResource(R.string.lumos_home_curriculum_guided_desc),
                                emoji = "🎯"
                            ),
                            LumosCurriculumFeature(
                                title = stringResource(R.string.lumos_home_curriculum_certificates_title),
                                description = stringResource(R.string.lumos_home_curriculum_certificates_desc),
                                emoji = "🏆"
                            ),
                            LumosCurriculumFeature(
                                title = stringResource(R.string.lumos_home_curriculum_lms_title),
                                description = stringResource(R.string.lumos_home_curriculum_lms_desc),
                                emoji = "📱"
                            )
                        )
                    )
                }

                // 6. Come Teach With Us
                item {
                    LumosTeachingHeroSection(
                        title = stringResource(R.string.lumos_home_teaching_title),
                        description = stringResource(R.string.lumos_home_teaching_description),
                        getStartedLabel = stringResource(R.string.lumos_home_teaching_get_started),
                        onGetStarted = onNavigateToTeacherVideos
                    )
                }

                // 7. How to Begin
                item {
                    LumosHowToBeginSection(
                        heading = stringResource(R.string.lumos_home_begin_heading),
                        steps = listOf(
                            LumosBeginStep(
                                title = stringResource(R.string.lumos_home_begin_plan_tab),
                                description = stringResource(R.string.lumos_home_begin_plan_desc),
                                tip = stringResource(R.string.lumos_home_begin_plan_tip)
                            ),
                            LumosBeginStep(
                                title = stringResource(R.string.lumos_home_begin_record_tab),
                                description = stringResource(R.string.lumos_home_begin_record_desc),
                                tip = stringResource(R.string.lumos_home_begin_record_tip)
                            ),
                            LumosBeginStep(
                                title = stringResource(R.string.lumos_home_begin_launch_tab),
                                description = stringResource(R.string.lumos_home_begin_launch_desc),
                                tip = stringResource(R.string.lumos_home_begin_launch_tip)
                            )
                        )
                    )
                }
            }
        }
    }
}
