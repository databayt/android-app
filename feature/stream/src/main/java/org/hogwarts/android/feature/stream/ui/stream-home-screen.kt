package org.hogwarts.android.feature.stream.ui

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
import org.hogwarts.android.feature.stream.R
import org.hogwarts.android.feature.stream.ui.components.StreamAiFluencySection
import org.hogwarts.android.feature.stream.ui.components.StreamContinueWatchingSection
import org.hogwarts.android.feature.stream.ui.components.StreamCurriculumSection
import org.hogwarts.android.feature.stream.ui.components.StreamFeatureDefaults
import org.hogwarts.android.feature.stream.ui.components.StreamFeaturesSection
import org.hogwarts.android.feature.stream.ui.components.StreamHeroSection
import org.hogwarts.android.feature.stream.ui.components.StreamHotReleasesSection
import org.hogwarts.android.feature.stream.ui.components.StreamHowToBeginSection
import org.hogwarts.android.feature.stream.ui.components.StreamReasonsSection
import org.hogwarts.android.feature.stream.ui.components.StreamSkillsSection
import org.hogwarts.android.feature.stream.ui.components.StreamTeachingHeroSection
import org.hogwarts.android.feature.stream.ui.components.defaultBeginSteps
import org.hogwarts.android.feature.stream.ui.components.defaultCurriculumFeatures
import org.hogwarts.android.feature.stream.ui.components.defaultStreamReasons
import org.hogwarts.android.feature.stream.ui.components.defaultStreamSkills

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamHomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToMyLearning: () -> Unit,
    onNavigateToCourse: (String) -> Unit,
    viewModel: StreamHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.stream_home_top_bar_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
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
                // 1. Hero
                item {
                    StreamHeroSection(
                        title = stringResource(R.string.stream_home_hero_title),
                        tagline = stringResource(R.string.stream_home_hero_tagline),
                        exploreLabel = stringResource(R.string.stream_home_explore_courses),
                        learningLabel = stringResource(R.string.stream_home_my_learning),
                        showLearningCta = uiState.isAuthenticated,
                        onExplore = onNavigateToCourses,
                        onMyLearning = onNavigateToMyLearning
                    )
                }

                // 2. Feature grid
                item {
                    StreamFeaturesSection(
                        features = StreamFeatureDefaults.defaults(
                            curatedTitle = stringResource(R.string.stream_home_feature_curated_title),
                            curatedDesc = stringResource(R.string.stream_home_feature_curated_desc),
                            interactiveTitle = stringResource(R.string.stream_home_feature_interactive_title),
                            interactiveDesc = stringResource(R.string.stream_home_feature_interactive_desc),
                            progressTitle = stringResource(R.string.stream_home_feature_progress_title),
                            progressDesc = stringResource(R.string.stream_home_feature_progress_desc),
                            communityTitle = stringResource(R.string.stream_home_feature_community_title),
                            communityDesc = stringResource(R.string.stream_home_feature_community_desc)
                        )
                    )
                }

                // 3. Continue watching
                if (uiState.continueWatching.isNotEmpty()) {
                    item {
                        StreamContinueWatchingSection(
                            title = stringResource(R.string.stream_home_continue_watching),
                            items = uiState.continueWatching,
                            onItemClick = { course -> onNavigateToCourse(course.id) }
                        )
                    }
                }

                // 4. AI Fluency
                item {
                    StreamAiFluencySection(
                        badge = stringResource(R.string.stream_home_ai_badge),
                        title = stringResource(R.string.stream_home_ai_title),
                        description = stringResource(R.string.stream_home_ai_description),
                        cta = stringResource(R.string.stream_home_ai_cta),
                        onCtaClick = onNavigateToCourses
                    )
                }

                // 5. Skills
                item {
                    StreamSkillsSection(
                        title = stringResource(R.string.stream_home_skills_title),
                        description = stringResource(R.string.stream_home_skills_description),
                        skills = defaultStreamSkills(
                            generativeAi = stringResource(R.string.stream_home_skill_generative_ai),
                            itCertifications = stringResource(R.string.stream_home_skill_it_certifications),
                            dataScience = stringResource(R.string.stream_home_skill_data_science)
                        ),
                        onSkillClick = { onNavigateToCourses() }
                    )
                }

                // 6. Hot releases
                item {
                    StreamHotReleasesSection(
                        title = stringResource(R.string.stream_home_hot_releases),
                        courses = uiState.hotReleases,
                        onViewAll = onNavigateToCourses,
                        onCourseClick = { course -> onNavigateToCourse(course.id) }
                    )
                }

                // 7. Curriculum
                item {
                    StreamCurriculumSection(
                        title = stringResource(R.string.stream_home_curriculum_title),
                        description = stringResource(R.string.stream_home_curriculum_description),
                        features = defaultCurriculumFeatures(
                            worldClassTitle = stringResource(R.string.stream_home_curriculum_world_class_title),
                            worldClassDesc = stringResource(R.string.stream_home_curriculum_world_class_desc),
                            guidedTitle = stringResource(R.string.stream_home_curriculum_guided_title),
                            guidedDesc = stringResource(R.string.stream_home_curriculum_guided_desc),
                            certificatesTitle = stringResource(R.string.stream_home_curriculum_certificates_title),
                            certificatesDesc = stringResource(R.string.stream_home_curriculum_certificates_desc),
                            lmsTitle = stringResource(R.string.stream_home_curriculum_lms_title),
                            lmsDesc = stringResource(R.string.stream_home_curriculum_lms_desc)
                        )
                    )
                }

                // 8. Teaching hero
                item {
                    StreamTeachingHeroSection(
                        title = stringResource(R.string.stream_home_teaching_title),
                        description = stringResource(R.string.stream_home_teaching_description),
                        primaryLabel = stringResource(R.string.stream_home_teaching_get_started),
                        secondaryLabel = stringResource(R.string.stream_home_teaching_learn_more),
                        onPrimary = onNavigateToCourses,
                        onSecondary = onNavigateToCourses
                    )
                }

                // 9. Reasons
                item {
                    StreamReasonsSection(
                        heading = stringResource(R.string.stream_home_reasons_heading),
                        reasons = defaultStreamReasons(
                            teachTitle = stringResource(R.string.stream_home_reason_teach_title),
                            teachDesc = stringResource(R.string.stream_home_reason_teach_desc),
                            inspireTitle = stringResource(R.string.stream_home_reason_inspire_title),
                            inspireDesc = stringResource(R.string.stream_home_reason_inspire_desc),
                            rewardTitle = stringResource(R.string.stream_home_reason_reward_title),
                            rewardDesc = stringResource(R.string.stream_home_reason_reward_desc)
                        )
                    )
                }

                // 10. How to begin
                item {
                    StreamHowToBeginSection(
                        heading = stringResource(R.string.stream_home_begin_heading),
                        steps = defaultBeginSteps(
                            planTab = stringResource(R.string.stream_home_begin_plan_tab),
                            planDesc = stringResource(R.string.stream_home_begin_plan_desc),
                            planTip = stringResource(R.string.stream_home_begin_plan_tip),
                            recordTab = stringResource(R.string.stream_home_begin_record_tab),
                            recordDesc = stringResource(R.string.stream_home_begin_record_desc),
                            recordTip = stringResource(R.string.stream_home_begin_record_tip),
                            launchTab = stringResource(R.string.stream_home_begin_launch_tab),
                            launchDesc = stringResource(R.string.stream_home_begin_launch_desc),
                            launchTip = stringResource(R.string.stream_home_begin_launch_tip),
                            helpTitle = stringResource(R.string.stream_home_begin_help_title),
                            planHelp = stringResource(R.string.stream_home_begin_plan_help),
                            recordHelp = stringResource(R.string.stream_home_begin_record_help),
                            launchHelp = stringResource(R.string.stream_home_begin_launch_help)
                        )
                    )
                }
            }
        }
    }
}
