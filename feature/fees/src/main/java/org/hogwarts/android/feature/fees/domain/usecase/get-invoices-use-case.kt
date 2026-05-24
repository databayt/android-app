package org.hogwarts.android.feature.fees.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.fees.data.repository.FeesRepository
import org.hogwarts.android.feature.fees.domain.model.Invoice
import javax.inject.Inject

class GetInvoicesUseCase @Inject constructor(
    private val repository: FeesRepository
) {
    suspend operator fun invoke(
        studentId: String? = null,
        status: String? = null
    ): Result<List<Invoice>> =
        repository.getInvoices(studentId = studentId, status = status)
}
