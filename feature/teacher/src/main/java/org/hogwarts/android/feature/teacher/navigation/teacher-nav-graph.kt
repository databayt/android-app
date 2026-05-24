package org.hogwarts.android.feature.teacher.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.teacher.ui.BatchAttendanceScreen
import org.hogwarts.android.feature.teacher.ui.ClassDetailScreen
import org.hogwarts.android.feature.teacher.ui.GradeEntryScreen
import org.hogwarts.android.feature.teacher.ui.MyClassesScreen
import org.hogwarts.android.feature.teacher.ui.StudentRosterScreen
import org.hogwarts.android.feature.teacher.ui.TeacherScheduleScreen

@Serializable data object TeacherClasses
@Serializable data class TeacherClassDetail(val classId: String)
@Serializable data class TeacherClassAttendance(val classId: String)
@Serializable data class TeacherClassGrades(val classId: String)
@Serializable data class TeacherClassStudents(val classId: String)
@Serializable data object TeacherSchedule

fun NavGraphBuilder.teacherClassesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToClass: (String) -> Unit
) {
    composable<TeacherClasses> {
        MyClassesScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToClass = onNavigateToClass
        )
    }
}

fun NavGraphBuilder.teacherClassDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAttendance: (String) -> Unit,
    onNavigateToGrades: (String) -> Unit,
    onNavigateToStudentRoster: (String) -> Unit
) {
    composable<TeacherClassDetail> {
        ClassDetailScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToGrades = onNavigateToGrades,
            onNavigateToStudentRoster = onNavigateToStudentRoster
        )
    }
}

fun NavGraphBuilder.teacherBatchAttendanceScreen(
    onNavigateBack: () -> Unit
) {
    composable<TeacherClassAttendance> {
        BatchAttendanceScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.teacherGradeEntryScreen(
    onNavigateBack: () -> Unit
) {
    composable<TeacherClassGrades> {
        GradeEntryScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.teacherStudentRosterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStudent: (String) -> Unit
) {
    composable<TeacherClassStudents> {
        StudentRosterScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToStudent = onNavigateToStudent
        )
    }
}

fun NavGraphBuilder.teacherScheduleScreen(
    onNavigateBack: () -> Unit,
    onNavigateToClass: (String) -> Unit
) {
    composable<TeacherSchedule> {
        TeacherScheduleScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToClass = onNavigateToClass
        )
    }
}
