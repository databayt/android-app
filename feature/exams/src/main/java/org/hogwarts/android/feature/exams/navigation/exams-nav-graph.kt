package org.hogwarts.android.feature.exams.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.exams.ui.ExamCertificateScreen
import org.hogwarts.android.feature.exams.ui.ExamDetailScreen
import org.hogwarts.android.feature.exams.ui.ExamResultsScreen
import org.hogwarts.android.feature.exams.ui.ExamsScreen
import org.hogwarts.android.feature.exams.ui.OnlineExamScreen
import org.hogwarts.android.feature.exams.ui.QuestionBankScreen
import org.hogwarts.android.feature.exams.ui.QuizScreen

@Serializable data object Exams
@Serializable data class ExamDetail(val examId: String)
@Serializable data object ExamQuiz
@Serializable data class OnlineExam(val examId: String)
@Serializable data object QuestionBank
@Serializable data class ExamResults(val examId: String)
@Serializable data class ExamCertificate(val examId: String)

fun NavGraphBuilder.examsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExam: (String) -> Unit
) {
    composable<Exams> {
        ExamsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToExam = onNavigateToExam
        )
    }
}

fun NavGraphBuilder.examDetailScreen(
    onNavigateBack: () -> Unit
) {
    composable<ExamDetail> {
        ExamDetailScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

fun NavGraphBuilder.quizScreen(
    onNavigateBack: () -> Unit
) {
    composable<ExamQuiz> {
        QuizScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

fun NavGraphBuilder.onlineExamScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: (String) -> Unit
) {
    composable<OnlineExam> {
        OnlineExamScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToResults = onNavigateToResults
        )
    }
}

fun NavGraphBuilder.questionBankScreen(
    onNavigateBack: () -> Unit
) {
    composable<QuestionBank> {
        QuestionBankScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

fun NavGraphBuilder.examResultsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCertificate: (String) -> Unit
) {
    composable<ExamResults> {
        ExamResultsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToCertificate = onNavigateToCertificate
        )
    }
}

fun NavGraphBuilder.examCertificateScreen(
    onNavigateBack: () -> Unit
) {
    composable<ExamCertificate> {
        ExamCertificateScreen(
            onNavigateBack = onNavigateBack
        )
    }
}
