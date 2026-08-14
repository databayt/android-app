package org.hogwarts.android.feature.lumos.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.lumos.ui.ChapterListScreen
import org.hogwarts.android.feature.lumos.ui.CourseCatalogScreen
import org.hogwarts.android.feature.lumos.ui.CourseCertificateScreen
import org.hogwarts.android.feature.lumos.ui.CourseDetailScreen
import org.hogwarts.android.feature.lumos.ui.CourseProgressScreen
import org.hogwarts.android.feature.lumos.ui.LessonQuizScreen
import org.hogwarts.android.feature.lumos.ui.LumosHomeScreen
import org.hogwarts.android.feature.lumos.ui.TextLessonScreen
import org.hogwarts.android.feature.lumos.ui.VideoLessonScreen
import org.hogwarts.android.feature.lumos.ui.admin.LumosAdminReviewScreen
import org.hogwarts.android.feature.lumos.ui.teacher.LumosTeacherVideosScreen

@Serializable
data object LumosHome

@Serializable
data class LumosCatalog(
    val initialGrade: Int? = null,
    val lockGrade: Boolean = false
)

@Serializable
data class LumosCourseDetail(
    val courseId: String
)

@Serializable
data class LumosChapters(
    val courseId: String
)

@Serializable
data class LumosVideoLesson(
    val courseId: String,
    val lessonId: String
)

@Serializable
data class LumosTextLesson(
    val courseId: String,
    val lessonId: String
)

@Serializable
data class LumosQuiz(
    val courseId: String,
    val lessonId: String
)

@Serializable
data class LumosProgress(
    val courseId: String
)

@Serializable
data class LumosCertificate(
    val courseId: String
)

@Serializable
data object LumosTeacherVideos

@Serializable
data object LumosAdminReview

fun NavController.navigateToLumosHome(navOptions: NavOptions? = null) = navigate(route = LumosHome, navOptions)
fun NavController.navigateToLumosCatalog(initialGrade: Int? = null, lockGrade: Boolean = false, navOptions: NavOptions? = null) =
    navigate(route = LumosCatalog(initialGrade, lockGrade), navOptions)
fun NavController.navigateToLumosCourseDetail(courseId: String, navOptions: NavOptions? = null) =
    navigate(route = LumosCourseDetail(courseId), navOptions)

fun NavGraphBuilder.lumosHomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToMyLearning: () -> Unit,
    onNavigateToCourse: (String) -> Unit,
    onNavigateToTeacherVideos: () -> Unit = {}
) {
    composable<LumosHome> {
        LumosHomeScreen(
            onNavigateToCourses = onNavigateToCourses,
            onNavigateToMyLearning = onNavigateToMyLearning,
            onNavigateToCourse = onNavigateToCourse,
            onNavigateToTeacherVideos = onNavigateToTeacherVideos
        )
    }
}

fun NavGraphBuilder.courseCatalogScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCourse: (String) -> Unit
) {
    composable<LumosCatalog> {
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
    composable<LumosCourseDetail> {
        CourseDetailScreen(
            onNavigateBack = onNavigateBack,
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
    composable<LumosChapters> {
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
    composable<LumosVideoLesson> {
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
    composable<LumosTextLesson> {
        TextLessonScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToNext = onNavigateToNext
        )
    }
}

fun NavGraphBuilder.lessonQuizScreen(
    onNavigateBack: () -> Unit,
    onQuizCompleted: () -> Unit = onNavigateBack
) {
    composable<LumosQuiz> {
        LessonQuizScreen(
            onNavigateBack = onNavigateBack,
            onQuizCompleted = onQuizCompleted
        )
    }
}

fun NavGraphBuilder.courseProgressScreen(
    onNavigateBack: () -> Unit
) {
    composable<LumosProgress> {
        CourseProgressScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

fun NavGraphBuilder.courseCertificateScreen(
    onNavigateBack: () -> Unit
) {
    composable<LumosCertificate> {
        CourseCertificateScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

fun NavGraphBuilder.lumosTeacherVideosScreen(
    onNavigateBack: () -> Unit
) {
    composable<LumosTeacherVideos> {
        LumosTeacherVideosScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

fun NavGraphBuilder.lumosAdminReviewScreen(
    onNavigateBack: () -> Unit
) {
    composable<LumosAdminReview> {
        LumosAdminReviewScreen(
            onNavigateBack = onNavigateBack
        )
    }
}
