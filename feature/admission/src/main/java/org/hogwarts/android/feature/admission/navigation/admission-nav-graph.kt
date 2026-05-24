package org.hogwarts.android.feature.admission.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.admission.ui.ApplicationFormScreen
import org.hogwarts.android.feature.admission.ui.ApplicationStatusScreen
import org.hogwarts.android.feature.admission.ui.ApplicationsScreen

@Serializable data object AdmissionList
@Serializable data object AdmissionForm
@Serializable data class AdmissionFormEdit(val applicationId: String)
@Serializable data class AdmissionStatus(val applicationId: String)

fun NavGraphBuilder.admissionListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToApplication: (String) -> Unit,
    onNavigateToNewApplication: () -> Unit
) {
    composable<AdmissionList> {
        ApplicationsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToApplication = onNavigateToApplication,
            onNavigateToNewApplication = onNavigateToNewApplication
        )
    }
}

fun NavGraphBuilder.admissionFormScreen(
    onNavigateBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    composable<AdmissionForm> {
        ApplicationFormScreen(
            onNavigateBack = onNavigateBack,
            onSubmitSuccess = onSubmitSuccess
        )
    }
}

fun NavGraphBuilder.admissionFormEditScreen(
    onNavigateBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    composable<AdmissionFormEdit> {
        ApplicationFormScreen(
            onNavigateBack = onNavigateBack,
            onSubmitSuccess = onSubmitSuccess
        )
    }
}

fun NavGraphBuilder.admissionStatusScreen(
    onNavigateBack: () -> Unit
) {
    composable<AdmissionStatus> {
        ApplicationStatusScreen(onNavigateBack = onNavigateBack)
    }
}
