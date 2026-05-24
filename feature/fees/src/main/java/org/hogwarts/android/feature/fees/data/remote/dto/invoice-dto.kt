package org.hogwarts.android.feature.fees.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.fees.domain.model.Invoice
import org.hogwarts.android.feature.fees.domain.model.InvoiceLineItem
import org.hogwarts.android.feature.fees.domain.model.InvoiceStatus
import org.hogwarts.android.feature.fees.domain.model.PaymentMethod
import org.hogwarts.android.feature.fees.domain.model.PaymentStatus
import org.hogwarts.android.feature.fees.domain.model.PaymentTransaction
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Serializable
data class InvoiceDto(
    val id: String,
    @SerialName("invoice_number") val invoiceNumber: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("issue_date") val issueDate: String,
    @SerialName("due_date") val dueDate: String,
    val status: String,
    val subtotal: Double,
    @SerialName("tax_amount") val taxAmount: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("paid_amount") val paidAmount: Double = 0.0,
    @SerialName("line_items") val lineItems: List<InvoiceLineItemDto> = emptyList(),
    val notes: String? = null
)

@Serializable
data class InvoiceLineItemDto(
    val id: String,
    val description: String,
    val quantity: Int = 1,
    @SerialName("unit_price") val unitPrice: Double,
    val amount: Double,
    val category: String? = null
)

@Serializable
data class InvoiceListResponse(
    val data: List<InvoiceDto>
)

@Serializable
data class PaymentTransactionDto(
    val id: String,
    @SerialName("invoice_id") val invoiceId: String? = null,
    @SerialName("fee_id") val feeId: String? = null,
    val amount: Double,
    val currency: String = "SAR",
    @SerialName("payment_method") val paymentMethod: String,
    val status: String,
    @SerialName("transaction_ref") val transactionRef: String? = null,
    @SerialName("processed_at") val processedAt: String? = null,
    val description: String? = null
)

@Serializable
data class TransactionListResponse(
    val data: List<PaymentTransactionDto>
)

@Serializable
data class ProcessPaymentRequestDto(
    @SerialName("invoice_id") val invoiceId: String? = null,
    @SerialName("fee_id") val feeId: String? = null,
    val amount: Double,
    val currency: String = "SAR",
    @SerialName("payment_method") val paymentMethod: String
)

// ── Extension: InvoiceDto → Invoice ─────────────────────────────────────────

fun InvoiceDto.toDomain(): Invoice = Invoice(
    id = id,
    invoiceNumber = invoiceNumber,
    studentId = studentId,
    studentName = studentName,
    issueDate = LocalDate.parse(issueDate),
    dueDate = LocalDate.parse(dueDate),
    status = InvoiceStatus.fromString(status),
    subtotal = BigDecimal.valueOf(subtotal),
    taxAmount = BigDecimal.valueOf(taxAmount),
    discountAmount = BigDecimal.valueOf(discountAmount),
    totalAmount = BigDecimal.valueOf(totalAmount),
    paidAmount = BigDecimal.valueOf(paidAmount),
    lineItems = lineItems.map { it.toDomain() },
    notes = notes
)

// ── Extension: InvoiceLineItemDto → InvoiceLineItem ─────────────────────────

fun InvoiceLineItemDto.toDomain(): InvoiceLineItem = InvoiceLineItem(
    id = id,
    description = description,
    quantity = quantity,
    unitPrice = BigDecimal.valueOf(unitPrice),
    amount = BigDecimal.valueOf(amount),
    category = category
)

// ── Extension: PaymentTransactionDto → PaymentTransaction ───────────────────

fun PaymentTransactionDto.toDomain(): PaymentTransaction = PaymentTransaction(
    id = id,
    invoiceId = invoiceId,
    feeId = feeId,
    amount = BigDecimal.valueOf(amount),
    currency = currency,
    paymentMethod = PaymentMethod.valueOf(paymentMethod.uppercase()),
    status = PaymentStatus.valueOf(status.uppercase()),
    transactionRef = transactionRef,
    processedAt = processedAt?.let { Instant.parse(it) },
    description = description
)
