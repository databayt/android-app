package org.hogwarts.android.feature.fees.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.fees.data.repository.FeesRepository
import org.hogwarts.android.feature.fees.domain.model.Invoice
import javax.inject.Inject

class GetInvoiceDetailUseCase @Inject constructor(
    private val repository: FeesRepository
) {
    suspend operator fun invoke(invoiceId: String): Result<Invoice> =
        repository.getInvoiceDetail(invoiceId = invoiceId)
}
