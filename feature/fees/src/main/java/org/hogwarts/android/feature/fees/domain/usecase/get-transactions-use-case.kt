package org.hogwarts.android.feature.fees.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.fees.data.repository.FeesRepository
import org.hogwarts.android.feature.fees.domain.model.PaymentTransaction
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: FeesRepository
) {
    suspend operator fun invoke(
        startDate: String? = null,
        endDate: String? = null,
        status: String? = null
    ): Result<List<PaymentTransaction>> =
        repository.getTransactions(
            startDate = startDate,
            endDate = endDate,
            status = status
        )
}
