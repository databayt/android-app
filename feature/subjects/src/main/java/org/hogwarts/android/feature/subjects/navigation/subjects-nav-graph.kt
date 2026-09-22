package org.hogwarts.android.feature.subjects.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.subjects.textbook.ui.TextbookReaderScreen
import org.hogwarts.android.feature.subjects.ui.SubjectDetailScreen
import org.hogwarts.android.feature.subjects.ui.SubjectsListScreen

@Serializable data object Subjects
@Serializable data class SubjectDetail(val subjectId: String)
/** `/subjects/{slug}/textbook` — the book, read in the app. */
@Serializable data class SubjectTextbook(val slug: String)

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

/**
 * The textbook reader — the web's Books-style reader at
 * `/subjects/{slug}/textbook`, native. Full screen: no shell header.
 */
fun NavGraphBuilder.subjectTextbookScreen(
    onClose: () -> Unit,
    onOpenPdf: (String) -> Unit,
) {
    composable<SubjectTextbook> {
        TextbookReaderScreen(onClose = onClose, onOpenPdf = onOpenPdf)
    }
}
