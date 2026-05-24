package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for invoice records (Enhanced Finance - EPIC-25).
 */
@Entity(
    tableName = "invoices",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["studentId", "schoolId"]),
        Index(value = ["status", "schoolId"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val invoiceNumber: String,
    val studentId: String,
    val studentName: String,
    val issueDate: String,
    val dueDate: String,
    val status: String,
    val subtotal: Double,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val notes: String? = null,
    val lastSyncedAt: Instant
)

/**
 * Room entity for invoice line items.
 */
@Entity(
    tableName = "invoice_line_items",
    indices = [
        Index(value = ["invoiceId"])
    ]
)
data class InvoiceLineItemEntity(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val description: String,
    val quantity: Int = 1,
    val unitPrice: Double,
    val amount: Double,
    val category: String? = null
)

/**
 * Room entity for payment transaction records.
 */
@Entity(
    tableName = "payment_transactions",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["invoiceId"]),
        Index(value = ["status", "schoolId"])
    ]
)
data class PaymentTransactionEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val invoiceId: String? = null,
    val feeId: String? = null,
    val amount: Double,
    val currency: String = "SAR",
    val paymentMethod: String,
    val status: String,
    val transactionRef: String? = null,
    val processedAt: Instant? = null,
    val description: String? = null,
    val lastSyncedAt: Instant
)
