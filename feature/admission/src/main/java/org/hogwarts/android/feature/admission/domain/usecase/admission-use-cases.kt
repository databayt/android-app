package org.hogwarts.android.feature.admission.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.admission.data.repository.AdmissionRepository
import org.hogwarts.android.feature.admission.domain.model.AdmissionApplication
import org.hogwarts.android.feature.admission.domain.model.AdmissionDocument
import java.io.File
import javax.inject.Inject

class GetApplicationsUseCase @Inject constructor(
    private val repository: AdmissionRepository
) {
    suspend operator fun invoke(): Result<List<AdmissionApplication>> {
        return try {
            Result.Success(repository.getApplications())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetApplicationUseCase @Inject constructor(
    private val repository: AdmissionRepository
) {
    suspend operator fun invoke(applicationId: String): Result<AdmissionApplication> {
        return try {
            Result.Success(repository.getApplication(applicationId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class SaveApplicationUseCase @Inject constructor(
    private val repository: AdmissionRepository
) {
    suspend operator fun invoke(application: AdmissionApplication): Result<AdmissionApplication> {
        return try {
            val saved = if (application.id.isBlank()) {
                repository.createApplication(application)
            } else {
                repository.updateApplication(application)
            }
            Result.Success(saved)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class SubmitApplicationUseCase @Inject constructor(
    private val repository: AdmissionRepository
) {
    suspend operator fun invoke(applicationId: String): Result<AdmissionApplication> {
        return try {
            Result.Success(repository.submitApplication(applicationId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class UploadDocumentUseCase @Inject constructor(
    private val repository: AdmissionRepository
) {
    suspend operator fun invoke(applicationId: String, file: File): Result<AdmissionDocument> {
        return try {
            Result.Success(repository.uploadDocument(applicationId, file))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
