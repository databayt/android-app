package org.hogwarts.android.feature.stream.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.stream.ui.ChapterListScreen
import org.hogwarts.android.feature.stream.ui.CourseCatalogScreen
import org.hogwarts.android.feature.stream.ui.CourseCertificateScreen
import org.hogwarts.android.feature.stream.ui.CourseDetailScreen
import org.hogwarts.android.feature.stream.ui.CourseProgressScreen
import org.hogwarts.android.feature.stream.ui.LessonQuizScreen
import org.hogwarts.android.feature.stream.ui.StreamHomeScreen
import org.hogwarts.android.feature.stream.ui.TextLessonScreen
import org.hogwarts.android.feature.stream.ui.VideoLessonScreen

@Serializable data object StreamHome

/**
 * Catalog route. When [lockGrade] is true the catalog opens with [initialGrade]
 * pre-applied and hides the grade picker — used for the student-direct entry
 * from the home video tile. Defaults preserve today's "browse" behavior.
 */
@Serializable
data class StreamCatalog(
    val initialGrade: Int? = null,
    val lockGrade: Boolean = false
)

@Serializable data class StreamCourseDetail(val courseId: String)
@Serializable data class StreamChapters(val courseId: String)
@Serializable data class StreamVideoLesson(val courseId: String, val lessonId: String)
@Serializable data class StreamTextLesson(val courseId: String, val lessonId: String)
@Serializable data class StreamQuiz(val courseId: String, val lessonId: String)
@Serializable data class StreamProgress(val courseId: String)
@Serializable data class StreamCertificate(val courseId: String)

fun NavGraphBuilder.streamHomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToMyLearning: () -> Unit,
    onNavigateToCourse: (String) -> Unit
) {
    composable<StreamHome> {
        StreamHomeScreen(
            onNavigateToCourses = onNavigateToCourses,
            onNavigateToMyLearning = onNavigateToMyLearning,
            onNavigateToCourse = onNavigateToCourse
        )
    }
}

fun NavGraphBuilder.courseCatalogScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCourse: (String) -> Unit
) {
    composable<StreamCatalog> {
        CourseCatalogScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToCourse = onNavigateToCourse
        )
    }
}

fun NavGraphBuilder.courseDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChapters: (String) -> Unit,
    onNavigateToVideoLesson: (String, String) -> Unit,
    onNavigateToTextLesson: (String, String) -> Unit,
    onNavigateToQuiz: (String, String) -> Unit,
    onNavigateToProgress: (String) -> Unit,
    onNavigateToCertificate: (String) -> Unit
) {
    composable<StreamCourseDetail> {
        CourseDetailScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToChapters = onNavigateToChapters,
            onNavigateToVideoLesson = onNavigateToVideoLesson,
            onNavigateToTextLesson = onNavigateToTextLesson,
            onNavigateToQuiz = onNavigateToQuiz,
            onNavigateToProgress = onNavigateToProgress,
            onNavigateToCertificate = onNavigateToCertificate
        )
    }
}

fun NavGraphBuilder.chapterListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVideoLesson: (String, String) -> Unit,
    onNavigateToTextLesson: (String, String) -> Unit,
    onNavigateToQuiz: (String, String) -> Unit
) {
    composable<StreamChapters> {
        ChapterListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToVideoLesson = onNavigateToVideoLesson,
            onNavigateToTextLesson = onNavigateToTextLesson,
            onNavigateToQuiz = onNavigateToQuiz
        )
    }
}

fun NavGraphBuilder.videoLessonScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNext: (String, String) -> Unit
) {
    composable<StreamVideoLesson> {
        VideoLessonScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToNext = onNavigateToNext
        )
    }
}

fun NavGraphBuilder.textLessonScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNext: (String, String) -> Unit
) {
    composable<StreamTextLesson> {
        TextLessonScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToNext = onNavigateToNext
        )
    }
}

fun NavGraphBuilder.lessonQuizScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNext: (String, String) -> Unit
) {
    composable<StreamQuiz> {
        LessonQuizScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToNext = onNavigateToNext
        )
    }
}

fun NavGraphBuilder.courseProgressScreen(
    onNavigateBack: () -> Unit
) {
    composable<StreamProgress> {
        CourseProgressScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.courseCertificateScreen(
    onNavigateBack: () -> Unit
) {
    composable<StreamCertificate> {
        CourseCertificateScreen(onNavigateBack = onNavigateBack)
    }
}
