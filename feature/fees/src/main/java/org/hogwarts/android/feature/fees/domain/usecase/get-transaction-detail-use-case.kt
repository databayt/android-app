package org.hogwarts.android.feature.fees.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.fees.data.repository.FeesRepository
import org.hogwarts.android.feature.fees.domain.model.PaymentTransaction
import javax.inject.Inject

class GetTransactionDetailUseCase @Inject constructor(
    private val repository: FeesRepository
) {
    suspend operator fun invoke(transactionId: String): Result<PaymentTransaction> =
        repository.getTransactionDetail(transactionId = transactionId)
}
