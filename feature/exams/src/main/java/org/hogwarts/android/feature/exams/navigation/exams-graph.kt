package org.hogwarts.android.feature.exams.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.exams.ui.ExamsRoleViewModel
import org.hogwarts.android.feature.exams.ui.ExamsScreen
import org.hogwarts.android.feature.exams.ui.detail.ExamDetailScreen
import org.hogwarts.android.feature.exams.ui.qbank.QuestionBankScreen
import org.hogwarts.android.feature.exams.ui.take.OnlineExamScreen
import org.hogwarts.android.feature.exams.ui.upcoming.UpcomingScreen

/** `/exams` — the role's landing. */
@Serializable data object Exams

/** `/exams/upcoming`. */
@Serializable data object ExamsUpcoming

/** `/exams/:id`. */
@Serializable data class ExamDetail(val examId: String)

/** `/exams/:id/take`, on `GET /api/mobile/exams/:id/online`. */
@Serializable data class OnlineExam(val examId: String)

/** `/exams/qbank` — staff only; students practise on the web. */
@Serializable data object QuestionBank

/**
 * The exams section. [onNavigate] receives this module's routes (tabs,
 * tiles, rows that have a native screen); [onOpenHref] receives every other
 * locale-less web path of the section (`/exams/new`, `/exams/mark`…) for the
 * shell to open; [onBack] leaves a finished exam.
 */
fun NavGraphBuilder.examsGraph(
    onNavigate: (Any) -> Unit,
    onOpenHref: (String) -> Unit,
    onBack: () -> Unit,
) {
    composable<Exams> {
        ExamsScreen(onNavigate = onNavigate, onOpenHref = onOpenHref)
    }
    composable<ExamsUpcoming> {
        val role = hiltViewModel<ExamsRoleViewModel>().role
        UpcomingScreen(onNavigate = onNavigate, onOpenHref = onOpenHref, role = role)
    }
    composable<ExamDetail> {
        ExamDetailScreen(onNavigate = onNavigate, onOpenHref = onOpenHref)
    }
    composable<OnlineExam> {
        OnlineExamScreen(onDone = onBack)
    }
    composable<QuestionBank> {
        val role = hiltViewModel<ExamsRoleViewModel>().role
        QuestionBankScreen(onNavigate = onNavigate, onOpenHref = onOpenHref, role = role)
    }
}
