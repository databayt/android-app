package org.hogwarts.android.feature.admission.data.repository

import org.hogwarts.android.feature.admission.domain.model.AdmissionApplication
import org.hogwarts.android.feature.admission.domain.model.AdmissionDocument
import java.io.File

/**
 * Repository interface for admission data.
 */
interface AdmissionRepository {
    suspend fun getApplications(): List<AdmissionApplication>
    suspend fun getApplication(applicationId: String): AdmissionApplication
    suspend fun createApplication(application: AdmissionApplication): AdmissionApplication
    suspend fun updateApplication(application: AdmissionApplication): AdmissionApplication
    suspend fun submitApplication(applicationId: String): AdmissionApplication
    suspend fun uploadDocument(applicationId: String, file: File): AdmissionDocument
}
