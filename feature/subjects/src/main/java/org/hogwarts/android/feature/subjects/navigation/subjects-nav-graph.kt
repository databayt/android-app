package org.hogwarts.android.feature.subjects.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.subjects.ui.MySubjectsScreen
import org.hogwarts.android.feature.subjects.ui.SubjectDetailScreen
import org.hogwarts.android.feature.subjects.ui.SubjectsListScreen

@Serializable data object Subjects
@Serializable data class SubjectDetail(val subjectId: String)
@Serializable data object MySubjects

/**
 * Subjects list screen - school catalog grouped by department.
 */
fun NavGraphBuilder.subjectsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubject: (String) -> Unit
) {
    composable<Subjects> {
        SubjectsListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSubject = onNavigateToSubject
        )
    }
}

/**
 * Subject detail screen - description, syllabus, teachers, resources.
 */
fun NavGraphBuilder.subjectDetailScreen(
    onNavigateBack: () -> Unit
) {
    composable<SubjectDetail> {
        SubjectDetailScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

/**
 * My subjects screen - enrolled/assigned subjects with grade and attendance.
 */
fun NavGraphBuilder.mySubjectsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubject: (String) -> Unit
) {
    composable<MySubjects> {
        MySubjectsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSubject = onNavigateToSubject
        )
    }
}
