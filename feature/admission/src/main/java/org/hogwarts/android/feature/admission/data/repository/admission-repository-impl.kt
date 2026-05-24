package org.hogwarts.android.feature.admission.data.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.admission.data.remote.AdmissionApi
import org.hogwarts.android.feature.admission.data.remote.dto.AdmissionApplicationDto
import org.hogwarts.android.feature.admission.data.remote.dto.GuardianInfoDto
import org.hogwarts.android.feature.admission.domain.model.AdmissionApplication
import org.hogwarts.android.feature.admission.domain.model.AdmissionDocument
import java.io.File
import javax.inject.Inject

class AdmissionRepositoryImpl @Inject constructor(
    private val api: AdmissionApi,
    private val tenantContext: TenantContext
) : AdmissionRepository {

    override suspend fun getApplications(): List<AdmissionApplication> {
        val schoolId = tenantContext.requireSchoolId()
        return api.getApplications(schoolId).map { it.toDomain() }
    }

    override suspend fun getApplication(applicationId: String): AdmissionApplication {
        val schoolId = tenantContext.requireSchoolId()
        return api.getApplication(applicationId, schoolId).toDomain()
    }

    override suspend fun createApplication(application: AdmissionApplication): AdmissionApplication {
        val schoolId = tenantContext.requireSchoolId()
        return api.createApplication(schoolId, application.toDto()).toDomain()
    }

    override suspend fun updateApplication(application: AdmissionApplication): AdmissionApplication {
        val schoolId = tenantContext.requireSchoolId()
        return api.updateApplication(application.id, schoolId, application.toDto()).toDomain()
    }

    override suspend fun submitApplication(applicationId: String): AdmissionApplication {
        val schoolId = tenantContext.requireSchoolId()
        return api.submitApplication(applicationId, schoolId).toDomain()
    }

    override suspend fun uploadDocument(applicationId: String, file: File): AdmissionDocument {
        val schoolId = tenantContext.requireSchoolId()
        val mediaType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
        val requestBody = file.asRequestBody(mediaType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val response = api.uploadDocument(applicationId, schoolId, part)
        return AdmissionDocument(
            id = response.id,
            name = response.name,
            type = response.type,
            url = response.url,
            uploadedAt = response.uploadedAt
        )
    }

    private fun AdmissionApplication.toDto() = AdmissionApplicationDto(
        id = id,
        status = status.name,
        currentStep = currentStep.number,
        givenNameEn = personalInfo.givenNameEn,
        familyNameEn = personalInfo.familyNameEn,
        givenNameAr = personalInfo.givenNameAr,
        familyNameAr = personalInfo.familyNameAr,
        dateOfBirth = personalInfo.dateOfBirth,
        gender = personalInfo.gender,
        nationality = personalInfo.nationality,
        nationalId = personalInfo.nationalId,
        address = contactInfo.address,
        city = contactInfo.city,
        countryCode = contactInfo.countryCode,
        phone = contactInfo.phone,
        email = contactInfo.email,
        guardians = guardians.map { GuardianInfoDto(it.name, it.relationship, it.occupation, it.phone, it.email) },
        previousSchool = academicHistory.previousSchool,
        lastGrade = academicHistory.lastGrade,
        applyingGrade = academicHistory.applyingGrade,
        hasSpecialNeeds = academicHistory.hasSpecialNeeds,
        specialNeedsDetails = academicHistory.specialNeedsDetails
    )
}
