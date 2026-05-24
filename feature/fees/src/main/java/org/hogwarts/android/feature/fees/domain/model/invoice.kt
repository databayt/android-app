package org.hogwarts.android.feature.fees.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class Invoice(
    val id: String,
    val invoiceNumber: String,
    val studentId: String,
    val studentName: String,
    val issueDate: LocalDate,
    val dueDate: LocalDate,
    val status: InvoiceStatus,
    val subtotal: BigDecimal,
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal,
    val paidAmount: BigDecimal = BigDecimal.ZERO,
    val lineItems: List<InvoiceLineItem> = emptyList(),
    val notes: String? = null
) {
    val balanceDue: BigDecimal get() = totalAmount - paidAmount
}

data class InvoiceLineItem(
    val id: String,
    val description: String,
    val quantity: Int = 1,
    val unitPrice: BigDecimal,
    val amount: BigDecimal,
    val category: String? = null
)

enum class InvoiceStatus {
    DRAFT,
    SENT,
    PAID,
    PARTIALLY_PAID,
    OVERDUE,
    CANCELLED;

    companion object {
        fun fromString(value: String): InvoiceStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: DRAFT
    }
}

data class PaymentTransaction(
    val id: String,
    val invoiceId: String? = null,
    val feeId: String? = null,
    val amount: BigDecimal,
    val currency: String = "SAR",
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    val transactionRef: String? = null,
    val processedAt: Instant? = null,
    val description: String? = null
)
