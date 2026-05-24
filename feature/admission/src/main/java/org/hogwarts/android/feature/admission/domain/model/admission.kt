package org.hogwarts.android.feature.admission.domain.model

/**
 * Status of an admission application.
 */
enum class ApplicationStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    INTERVIEW_SCHEDULED,
    ACCEPTED,
    REJECTED,
    WAITLISTED
}

/**
 * Steps in the admission application form.
 */
enum class ApplicationStep(val number: Int, val title: String) {
    PERSONAL_INFO(1, "Personal Information"),
    CONTACT_INFO(2, "Contact Information"),
    GUARDIAN_INFO(3, "Guardian Information"),
    ACADEMIC_HISTORY(4, "Academic History"),
    DOCUMENTS(5, "Documents"),
    REVIEW(6, "Review & Submit")
}

/**
 * Full admission application domain model.
 */
data class AdmissionApplication(
    val id: String = "",
    val trackingNumber: String? = null,
    val status: ApplicationStatus = ApplicationStatus.DRAFT,
    val currentStep: ApplicationStep = ApplicationStep.PERSONAL_INFO,
    val personalInfo: PersonalInfo = PersonalInfo(),
    val contactInfo: ContactInfo = ContactInfo(),
    val guardians: List<GuardianInfo> = emptyList(),
    val academicHistory: AcademicHistory = AcademicHistory(),
    val documents: List<AdmissionDocument> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class PersonalInfo(
    val givenNameEn: String = "",
    val familyNameEn: String = "",
    val givenNameAr: String = "",
    val familyNameAr: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val nationality: String = "",
    val nationalId: String = ""
)

data class ContactInfo(
    val address: String = "",
    val city: String = "",
    val countryCode: String = "+966",
    val phone: String = "",
    val email: String = ""
)

data class GuardianInfo(
    val name: String = "",
    val relationship: String = "",
    val occupation: String = "",
    val phone: String = "",
    val email: String = ""
)

data class AcademicHistory(
    val previousSchool: String = "",
    val lastGrade: String = "",
    val applyingGrade: String = "",
    val hasSpecialNeeds: Boolean = false,
    val specialNeedsDetails: String = ""
)

data class AdmissionDocument(
    val id: String = "",
    val name: String,
    val type: String,
    val url: String = "",
    val uploadedAt: String? = null
)
