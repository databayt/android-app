package org.hogwarts.android.feature.students.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.students.ui.StudentDetailScreen
import org.hogwarts.android.feature.students.ui.StudentFormScreen
import org.hogwarts.android.feature.students.ui.StudentsScreen

@Serializable data object StudentsList
@Serializable data class StudentDetail(val studentId: String)
@Serializable data object StudentCreate
@Serializable data class StudentEdit(val studentId: String)

fun NavGraphBuilder.studentsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStudent: (String) -> Unit,
    onNavigateToCreateStudent: () -> Unit = {}
) {
    composable<StudentsList> {
        StudentsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToStudent = onNavigateToStudent
        )
    }
}

fun NavGraphBuilder.studentDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    composable<StudentDetail> { backStackEntry ->
        val route = backStackEntry.toRoute<StudentDetail>()
        StudentDetailScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToEdit = onNavigateToEdit
        )
    }
}

fun NavGraphBuilder.studentFormScreen(
    onNavigateBack: () -> Unit
) {
    // Create route (no studentId)
    composable<StudentCreate> {
        StudentFormScreen(onNavigateBack = onNavigateBack)
    }
    // Edit route (with studentId)
    composable<StudentEdit> { backStackEntry ->
        val route = backStackEntry.toRoute<StudentEdit>()
        StudentFormScreen(onNavigateBack = onNavigateBack)
    }
}
