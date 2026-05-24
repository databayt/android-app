package org.hogwarts.android.feature.lessons.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.lessons.ui.CurriculumMapScreen
import org.hogwarts.android.feature.lessons.ui.LessonDetailScreen
import org.hogwarts.android.feature.lessons.ui.LessonPlanFormScreen
import org.hogwarts.android.feature.lessons.ui.LessonPlansScreen
import org.hogwarts.android.feature.lessons.ui.ResourceBrowserScreen

@Serializable data object LessonsList
@Serializable data class LessonDetail(val lessonId: String)
@Serializable data object LessonCurriculum
@Serializable data object LessonResources
@Serializable data object LessonPlanForm
@Serializable data class LessonPlanFormEdit(val lessonId: String)

fun NavGraphBuilder.lessonPlansScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLesson: (String) -> Unit,
    onNavigateToCurriculum: () -> Unit,
    onNavigateToResources: () -> Unit,
    onNavigateToForm: () -> Unit
) {
    composable<LessonsList> {
        LessonPlansScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToLesson = onNavigateToLesson,
            onNavigateToCurriculum = onNavigateToCurriculum,
            onNavigateToResources = onNavigateToResources,
            onNavigateToForm = onNavigateToForm
        )
    }
}

fun NavGraphBuilder.lessonDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: ((String) -> Unit)? = null
) {
    composable<LessonDetail> {
        LessonDetailScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToEdit = onNavigateToEdit
        )
    }
}

fun NavGraphBuilder.curriculumMapScreen(
    onNavigateBack: () -> Unit
) {
    composable<LessonCurriculum> {
        CurriculumMapScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.resourceBrowserScreen(
    onNavigateBack: () -> Unit
) {
    composable<LessonResources> {
        ResourceBrowserScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.lessonPlanFormScreen(
    onNavigateBack: () -> Unit
) {
    composable<LessonPlanForm> {
        LessonPlanFormScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.lessonPlanFormEditScreen(
    onNavigateBack: () -> Unit
) {
    composable<LessonPlanFormEdit> {
        LessonPlanFormScreen(onNavigateBack = onNavigateBack)
    }
}
