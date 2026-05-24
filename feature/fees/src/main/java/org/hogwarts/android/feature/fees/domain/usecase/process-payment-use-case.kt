package org.hogwarts.android.feature.fees.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.fees.data.repository.FeesRepository
import org.hogwarts.android.feature.fees.domain.model.PaymentMethod
import org.hogwarts.android.feature.fees.domain.model.PaymentTransaction
import java.math.BigDecimal
import javax.inject.Inject

class ProcessPaymentUseCase @Inject constructor(
    private val repository: FeesRepository
) {
    suspend operator fun invoke(
        invoiceId: String? = null,
        feeId: String? = null,
        amount: BigDecimal,
        method: PaymentMethod
    ): Result<PaymentTransaction> {
        require(invoiceId != null || feeId != null) {
            "Either invoiceId or feeId must be provided"
        }
        require(amount > BigDecimal.ZERO) {
            "Payment amount must be greater than zero"
        }
        return repository.processPayment(
            invoiceId = invoiceId,
            feeId = feeId,
            amount = amount,
            method = method
        )
    }
}
