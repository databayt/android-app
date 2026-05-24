package org.hogwarts.android.feature.fees.domain.model

import java.time.Instant

data class PaymentRequest(
    val feeId: String,
    val amount: Double,
    val currency: String = "SAR",
    val paymentMethod: PaymentMethod = PaymentMethod.CARD
)

enum class PaymentMethod {
    CARD, BANK_TRANSFER, WALLET
}

data class PaymentResult(
    val id: String,
    val feeId: String,
    val amount: Double,
    val status: PaymentStatus,
    val transactionId: String? = null,
    val processedAt: Instant? = null,
    val error: String? = null
)

enum class PaymentStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
}
