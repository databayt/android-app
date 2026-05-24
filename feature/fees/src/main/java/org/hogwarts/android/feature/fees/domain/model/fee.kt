package org.hogwarts.android.feature.fees.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class FeeRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val description: String,
    val amount: BigDecimal,
    val paidAmount: BigDecimal = BigDecimal.ZERO,
    val dueDate: LocalDate,
    val status: FeeStatus,
    val category: String? = null,
    val term: String? = null
) {
    val balance: BigDecimal get() = amount - paidAmount
    val isPaid: Boolean get() = status == FeeStatus.PAID
}

enum class FeeStatus {
    PENDING,
    PAID,
    OVERDUE,
    PARTIAL;

    companion object {
        fun fromString(value: String): FeeStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: PENDING
    }
}

data class FeeSummary(
    val totalAmount: BigDecimal,
    val paidAmount: BigDecimal,
    val pendingAmount: BigDecimal,
    val overdueAmount: BigDecimal
)
