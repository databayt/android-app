package org.hogwarts.android.feature.subjects.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.subjects.ui.SubjectDetailScreen
import org.hogwarts.android.feature.subjects.ui.SubjectsListScreen

@Serializable data object Subjects
@Serializable data class SubjectDetail(val subjectId: String)

/**
 * Subjects list screen - school catalog grouped by department.
 */
fun NavGraphBuilder.subjectsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubject: (String) -> Unit,
    onOpenHref: (String) -> Unit = {},
    /** The web narrows a student's list and hides its filters; see the screen. */
    role: UserRole? = null,
) {
    composable<Subjects> {
        SubjectsListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSubject = onNavigateToSubject,
            onOpenHref = onOpenHref,
            role = role,
        )
    }
}

/**
 * Subject detail screen - description, syllabus, teachers, resources.
 */
fun NavGraphBuilder.subjectDetailScreen(
    onNavigateBack: () -> Unit,
    onOpenHref: (String) -> Unit = {},
) {
    composable<SubjectDetail> {
        SubjectDetailScreen(
            onNavigateBack = onNavigateBack,
            onOpenHref = onOpenHref,
        )
    }
}
