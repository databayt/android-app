package org.hogwarts.android.feature.admission.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import org.hogwarts.android.core.designsystem.atom.HogwartsTextField
import org.hogwarts.android.feature.admission.R
import org.hogwarts.android.feature.admission.domain.model.ApplicationStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationFormScreen(
    onNavigateBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: ApplicationFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.currentStep.title) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep == ApplicationStep.PERSONAL_INFO) onNavigateBack()
                        else viewModel.previousStep()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.admission_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { uiState.currentStep.number / 6f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.admission_form_step_of, uiState.currentStep.number),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (uiState.currentStep) {
                    ApplicationStep.PERSONAL_INFO -> PersonalInfoStep(viewModel)
                    ApplicationStep.CONTACT_INFO -> ContactInfoStep(viewModel)
                    ApplicationStep.GUARDIAN_INFO -> GuardianInfoStep(viewModel)
                    ApplicationStep.ACADEMIC_HISTORY -> AcademicHistoryStep(viewModel)
                    ApplicationStep.DOCUMENTS -> DocumentsStep(viewModel)
                    ApplicationStep.REVIEW -> ReviewStep(uiState, viewModel)
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.currentStep != ApplicationStep.PERSONAL_INFO) {
                    TextButton(onClick = { viewModel.previousStep() }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.admission_form_previous))
                    }
                }
                if (uiState.currentStep == ApplicationStep.REVIEW) {
                    HogwartsButton(
                        text = stringResource(R.string.admission_form_submit),
                        onClick = { viewModel.submit(onSubmitSuccess) },
                        isLoading = uiState.isSubmitting,
                        enabled = uiState.termsAccepted,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    HogwartsButton(
                        text = stringResource(R.string.admission_form_next),
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalInfoStep(viewModel: ApplicationFormViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val pi = uiState.application.personalInfo

    HogwartsTextField(value = pi.givenNameEn, onValueChange = { viewModel.updatePersonalInfo(pi.copy(givenNameEn = it)) }, label = stringResource(R.string.admission_form_given_name_en), placeholder = stringResource(R.string.admission_form_given_name_en_placeholder))
    HogwartsTextField(value = pi.familyNameEn, onValueChange = { viewModel.updatePersonalInfo(pi.copy(familyNameEn = it)) }, label = stringResource(R.string.admission_form_family_name_en), placeholder = stringResource(R.string.admission_form_family_name_en_placeholder))
    HogwartsTextField(value = pi.givenNameAr, onValueChange = { viewModel.updatePersonalInfo(pi.copy(givenNameAr = it)) }, label = stringResource(R.string.admission_form_given_name_ar), placeholder = stringResource(R.string.admission_form_given_name_ar_placeholder))
    HogwartsTextField(value = pi.familyNameAr, onValueChange = { viewModel.updatePersonalInfo(pi.copy(familyNameAr = it)) }, label = stringResource(R.string.admission_form_family_name_ar), placeholder = stringResource(R.string.admission_form_family_name_ar_placeholder))
    HogwartsTextField(value = pi.dateOfBirth, onValueChange = { viewModel.updatePersonalInfo(pi.copy(dateOfBirth = it)) }, label = stringResource(R.string.admission_form_dob), placeholder = stringResource(R.string.admission_form_dob_placeholder))
    HogwartsTextField(value = pi.gender, onValueChange = { viewModel.updatePersonalInfo(pi.copy(gender = it)) }, label = stringResource(R.string.admission_form_gender), placeholder = stringResource(R.string.admission_form_gender_placeholder))
    HogwartsTextField(value = pi.nationality, onValueChange = { viewModel.updatePersonalInfo(pi.copy(nationality = it)) }, label = stringResource(R.string.admission_form_nationality), placeholder = stringResource(R.string.admission_form_nationality_placeholder))
    HogwartsTextField(value = pi.nationalId, onValueChange = { viewModel.updatePersonalInfo(pi.copy(nationalId = it)) }, label = stringResource(R.string.admission_form_national_id), placeholder = stringResource(R.string.admission_form_national_id_placeholder))
}

@Composable
private fun ContactInfoStep(viewModel: ApplicationFormViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val ci = uiState.application.contactInfo

    HogwartsTextField(value = ci.address, onValueChange = { viewModel.updateContactInfo(ci.copy(address = it)) }, label = stringResource(R.string.admission_form_address), placeholder = stringResource(R.string.admission_form_address_placeholder))
    HogwartsTextField(value = ci.city, onValueChange = { viewModel.updateContactInfo(ci.copy(city = it)) }, label = stringResource(R.string.admission_form_city), placeholder = stringResource(R.string.admission_form_city_placeholder))
    HogwartsTextField(value = ci.phone, onValueChange = { viewModel.updateContactInfo(ci.copy(phone = it)) }, label = stringResource(R.string.admission_form_phone), placeholder = stringResource(R.string.admission_form_phone_placeholder))
    HogwartsTextField(value = ci.email, onValueChange = { viewModel.updateContactInfo(ci.copy(email = it)) }, label = stringResource(R.string.admission_form_email), placeholder = stringResource(R.string.admission_form_email_placeholder))
}

@Composable
private fun GuardianInfoStep(viewModel: ApplicationFormViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    uiState.application.guardians.forEachIndexed { index, guardian ->
        Text(text = stringResource(R.string.admission_form_guardian_number, index + 1), style = MaterialTheme.typography.titleSmall)
        HogwartsTextField(value = guardian.name, onValueChange = { viewModel.updateGuardian(index, guardian.copy(name = it)) }, label = stringResource(R.string.admission_form_guardian_name), placeholder = stringResource(R.string.admission_form_guardian_name_placeholder))
        HogwartsTextField(value = guardian.relationship, onValueChange = { viewModel.updateGuardian(index, guardian.copy(relationship = it)) }, label = stringResource(R.string.admission_form_guardian_relationship), placeholder = stringResource(R.string.admission_form_guardian_relationship_placeholder))
        HogwartsTextField(value = guardian.occupation, onValueChange = { viewModel.updateGuardian(index, guardian.copy(occupation = it)) }, label = stringResource(R.string.admission_form_guardian_occupation), placeholder = stringResource(R.string.admission_form_guardian_occupation_placeholder))
        HogwartsTextField(value = guardian.phone, onValueChange = { viewModel.updateGuardian(index, guardian.copy(phone = it)) }, label = stringResource(R.string.admission_form_guardian_phone), placeholder = stringResource(R.string.admission_form_guardian_phone_placeholder))
        HogwartsTextField(value = guardian.email, onValueChange = { viewModel.updateGuardian(index, guardian.copy(email = it)) }, label = stringResource(R.string.admission_form_guardian_email), placeholder = stringResource(R.string.admission_form_guardian_email_placeholder))
        Spacer(modifier = Modifier.height(8.dp))
    }

    TextButton(onClick = { viewModel.addGuardian() }) {
        Text(stringResource(R.string.admission_form_add_guardian))
    }
}

@Composable
private fun AcademicHistoryStep(viewModel: ApplicationFormViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val ah = uiState.application.academicHistory

    HogwartsTextField(value = ah.previousSchool, onValueChange = { viewModel.updateAcademicHistory(ah.copy(previousSchool = it)) }, label = stringResource(R.string.admission_form_previous_school), placeholder = stringResource(R.string.admission_form_previous_school_placeholder))
    HogwartsTextField(value = ah.lastGrade, onValueChange = { viewModel.updateAcademicHistory(ah.copy(lastGrade = it)) }, label = stringResource(R.string.admission_form_last_grade), placeholder = stringResource(R.string.admission_form_last_grade_placeholder))
    HogwartsTextField(value = ah.applyingGrade, onValueChange = { viewModel.updateAcademicHistory(ah.copy(applyingGrade = it)) }, label = stringResource(R.string.admission_form_applying_grade), placeholder = stringResource(R.string.admission_form_applying_grade_placeholder))
    if (ah.hasSpecialNeeds) {
        HogwartsTextField(value = ah.specialNeedsDetails, onValueChange = { viewModel.updateAcademicHistory(ah.copy(specialNeedsDetails = it)) }, label = stringResource(R.string.admission_form_special_needs), placeholder = stringResource(R.string.admission_form_special_needs_placeholder))
    }
}

@Composable
private fun DocumentsStep(viewModel: ApplicationFormViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Text(text = stringResource(R.string.admission_form_upload_title), style = MaterialTheme.typography.bodyMedium)
    Text(text = stringResource(R.string.admission_form_upload_formats), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(8.dp))

    uiState.application.documents.forEach { doc ->
        Text(text = "${doc.name} (${doc.type})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }

    HogwartsButton(
        text = stringResource(R.string.admission_form_upload_button),
        onClick = { viewModel.requestDocumentUpload() },
        isLoading = uiState.isUploading,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ReviewStep(uiState: ApplicationFormUiState, viewModel: ApplicationFormViewModel) {
    val app = uiState.application

    Text(text = stringResource(R.string.admission_form_review_personal), style = MaterialTheme.typography.titleSmall)
    Text(text = "${app.personalInfo.givenNameEn} ${app.personalInfo.familyNameEn}", style = MaterialTheme.typography.bodyMedium)
    Text(text = stringResource(R.string.admission_form_review_dob, app.personalInfo.dateOfBirth), style = MaterialTheme.typography.bodySmall)

    Spacer(modifier = Modifier.height(8.dp))
    Text(text = stringResource(R.string.admission_form_review_contact), style = MaterialTheme.typography.titleSmall)
    Text(text = app.contactInfo.email, style = MaterialTheme.typography.bodyMedium)
    Text(text = app.contactInfo.phone, style = MaterialTheme.typography.bodySmall)

    Spacer(modifier = Modifier.height(8.dp))
    Text(text = stringResource(R.string.admission_form_review_guardians, app.guardians.size), style = MaterialTheme.typography.titleSmall)
    app.guardians.forEach {
        Text(text = "${it.name} (${it.relationship})", style = MaterialTheme.typography.bodySmall)
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(text = stringResource(R.string.admission_form_review_applying, app.academicHistory.applyingGrade), style = MaterialTheme.typography.titleSmall)
    Text(text = stringResource(R.string.admission_form_review_documents, app.documents.size), style = MaterialTheme.typography.bodySmall)

    Spacer(modifier = Modifier.height(16.dp))
    Row {
        androidx.compose.material3.Checkbox(
            checked = uiState.termsAccepted,
            onCheckedChange = { viewModel.setTermsAccepted(it) }
        )
        Text(
            text = stringResource(R.string.admission_form_review_confirm),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
