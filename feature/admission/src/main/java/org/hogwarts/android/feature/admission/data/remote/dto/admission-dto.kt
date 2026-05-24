package org.hogwarts.android.feature.admission.data.remote.dto

import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.admission.domain.model.AcademicHistory
import org.hogwarts.android.feature.admission.domain.model.AdmissionApplication
import org.hogwarts.android.feature.admission.domain.model.AdmissionDocument
import org.hogwarts.android.feature.admission.domain.model.ApplicationStatus
import org.hogwarts.android.feature.admission.domain.model.ApplicationStep
import org.hogwarts.android.feature.admission.domain.model.ContactInfo
import org.hogwarts.android.feature.admission.domain.model.GuardianInfo
import org.hogwarts.android.feature.admission.domain.model.PersonalInfo

@Serializable
data class AdmissionApplicationDto(
    val id: String = "",
    val trackingNumber: String? = null,
    val status: String = "DRAFT",
    val currentStep: Int = 1,
    val givenNameEn: String = "",
    val familyNameEn: String = "",
    val givenNameAr: String = "",
    val familyNameAr: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val nationality: String = "",
    val nationalId: String = "",
    val address: String = "",
    val city: String = "",
    val countryCode: String = "+966",
    val phone: String = "",
    val email: String = "",
    val guardians: List<GuardianInfoDto> = emptyList(),
    val previousSchool: String = "",
    val lastGrade: String = "",
    val applyingGrade: String = "",
    val hasSpecialNeeds: Boolean = false,
    val specialNeedsDetails: String = "",
    val documents: List<DocumentDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): AdmissionApplication = AdmissionApplication(
        id = id,
        trackingNumber = trackingNumber,
        status = runCatching { ApplicationStatus.valueOf(status.uppercase()) }.getOrDefault(ApplicationStatus.DRAFT),
        currentStep = ApplicationStep.entries.find { it.number == currentStep } ?: ApplicationStep.PERSONAL_INFO,
        personalInfo = PersonalInfo(
            givenNameEn = givenNameEn,
            familyNameEn = familyNameEn,
            givenNameAr = givenNameAr,
            familyNameAr = familyNameAr,
            dateOfBirth = dateOfBirth,
            gender = gender,
            nationality = nationality,
            nationalId = nationalId
        ),
        contactInfo = ContactInfo(
            address = address,
            city = city,
            countryCode = countryCode,
            phone = phone,
            email = email
        ),
        guardians = guardians.map { it.toDomain() },
        academicHistory = AcademicHistory(
            previousSchool = previousSchool,
            lastGrade = lastGrade,
            applyingGrade = applyingGrade,
            hasSpecialNeeds = hasSpecialNeeds,
            specialNeedsDetails = specialNeedsDetails
        ),
        documents = documents.map { it.toDomain() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class GuardianInfoDto(
    val name: String = "",
    val relationship: String = "",
    val occupation: String = "",
    val phone: String = "",
    val email: String = ""
) {
    fun toDomain(): GuardianInfo = GuardianInfo(
        name = name,
        relationship = relationship,
        occupation = occupation,
        phone = phone,
        email = email
    )
}

@Serializable
data class DocumentDto(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val url: String = "",
    val uploadedAt: String? = null
) {
    fun toDomain(): AdmissionDocument = AdmissionDocument(
        id = id,
        name = name,
        type = type,
        url = url,
        uploadedAt = uploadedAt
    )
}

@Serializable
data class DocumentUploadResponseDto(
    val id: String,
    val name: String,
    val type: String,
    val url: String,
    val uploadedAt: String
)
