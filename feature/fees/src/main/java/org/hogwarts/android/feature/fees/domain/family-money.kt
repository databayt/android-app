package org.hogwarts.android.feature.fees.domain

import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.fees.data.remote.InvoiceDto
import org.hogwarts.android.feature.fees.data.remote.InvoiceListResponse
import org.hogwarts.android.feature.fees.data.remote.PaymentDto
import org.hogwarts.android.feature.fees.data.remote.TotalsDto

/** `InstallmentStatus` in `finance/family/types.ts`. */
enum class InstallmentStatus { PAID, PARTIAL, PENDING, OVERDUE, CANCELLED;
    companion object {
        fun fromWire(value: String): InstallmentStatus = entries.firstOrNull { it.name == value.uppercase() } ?: PENDING
    }
}

/**
 * The rails a school takes money through — `PaymentGateway`. Only [STRIPE]
 * and [TAP] have a hosted checkout (`fees/pay`); the wallet rails need a
 * transfer proof, and cash / bank transfer are recorded at the office.
 */
enum class Gateway(val wire: String) {
    STRIPE("stripe"), TAP("tap"), BANKAK("bankak"), CASHI("cashi"), CASH("cash"), BANK_TRANSFER("bank_transfer");

    val redirect: Boolean get() = this == STRIPE || this == TAP
    val wallet: Boolean get() = this == BANKAK || this == CASHI

    companion object {
        fun fromWire(value: String): Gateway? = entries.firstOrNull { it.wire == value }
    }
}

/** `payableGateways()` in `fees/fee-payment-methods.tsx`: redirect rails, then wallet rails. */
fun payableGateways(methods: List<Gateway>): List<Gateway> =
    methods.filter { it.redirect } + methods.filter { it.wallet }

/** One scheduled payment — an invoice row where the school issued one. */
@Serializable
data class Installment(
    val id: String,
    val number: Int,
    val count: Int,
    val invoiceNo: String?,
    val shareUrl: String?,
    /** `yyyy-MM-dd` of the stored midnight-UTC due date, or null for a lump fee. */
    val dueDate: String?,
    val amount: Double,
    val paidAmount: Double,
    val status: InstallmentStatus,
    val feeAssignmentId: String,
    val feeName: String,
    val studentId: String,
    val studentName: String,
    val academicYear: String,
) {
    val outstanding: Double get() = (amount - paidAmount).coerceAtLeast(0.0)
}

@Serializable
data class FamilyPayment(
    val id: String,
    val feeAssignmentId: String,
    val paymentNumber: String,
    val amount: Double,
    val paymentDate: String,
    val paymentMethod: String,
    val status: String,
    val feeName: String,
)

@Serializable
data class FamilyFee(
    val id: String,
    val feeName: String,
    val studentName: String,
    val academicYear: String,
    val total: Double,
    val paid: Double,
    val remaining: Double,
    val installments: List<Installment>,
)

@Serializable
data class FamilyTotals(
    val billed: Double = 0.0,
    val paid: Double = 0.0,
    val pendingVerification: Double = 0.0,
    val remaining: Double = 0.0,
    val overdue: Double = 0.0,
)

/** `FamilyMoney` — everything a family owes, has paid, and can pay with. */
@Serializable
data class FamilyMoney(
    val currency: String,
    val studentNames: List<String>,
    val methods: List<Gateway>,
    val fees: List<FamilyFee>,
    /** Every instalment across every fee, ordered by when it is owed. */
    val installments: List<Installment>,
    val payments: List<FamilyPayment>,
    val totals: FamilyTotals,
) {
    /** The unpaid part of [installments] — what this family still has to pay. */
    val due: List<Installment> get() = installments.filter {
        it.status == InstallmentStatus.OVERDUE || it.status == InstallmentStatus.PENDING || it.status == InstallmentStatus.PARTIAL
    }
    val nextDue: Installment? get() = due.firstOrNull()
    val settled: Boolean get() = totals.remaining <= 0.0
    val isOverdue: Boolean get() = totals.overdue > 0.0

    /** Whole-number percent paid of billed, capped at 100 — the banner's bar. */
    val progress: Int get() = if (totals.billed > 0) ((totals.paid / totals.billed) * 100).roundHalfUp().coerceAtMost(100) else 0

    fun fee(id: String): FamilyFee? = fees.firstOrNull { it.id == id }

    /**
     * The web's `PayFeeDialog` renders nothing when the fee is settled or the
     * school offers no payable rail — the Pay control follows the same rule.
     */
    fun canPay(feeAssignmentId: String): Boolean =
        (fee(feeAssignmentId)?.remaining ?: 0.0) > 0.0 && payableGateways(methods).isNotEmpty()

    /** "Paying online settles the whole remaining balance" — only where a fee's balance exceeds this instalment. */
    val showsFullBalanceNote: Boolean
        get() = due.any { i -> fee(i.feeAssignmentId)?.let { it.remaining > i.outstanding } ?: false }

    companion object {
        /**
         * Rebuild the family surface from the mobile routes. The instalment rows
         * and totals are the server's (`loadFamilyMoney`); fees are those rows
         * grouped by fee assignment, in first-appearance order.
         */
        fun from(invoices: InvoiceListResponse, payments: List<PaymentDto>): FamilyMoney? {
            val totals = invoices.totals ?: return null
            val installments = invoices.data.map { it.toInstallment() }
            val byFee = LinkedHashMap<String, MutableList<Installment>>()
            installments.forEach { byFee.getOrPut(it.feeAssignmentId) { mutableListOf() } += it }
            val fees = byFee.map { (id, rows) ->
                val ordered = rows.sortedBy { it.number }
                val total = ordered.sumOf { it.amount }
                val paid = ordered.sumOf { it.paidAmount }
                FamilyFee(
                    id = id,
                    feeName = ordered.first().feeName,
                    studentName = ordered.first().studentName,
                    academicYear = ordered.first().academicYear,
                    total = total,
                    paid = paid,
                    remaining = (total - paid).coerceAtLeast(0.0),
                    installments = ordered,
                )
            }
            return FamilyMoney(
                currency = invoices.currency ?: invoices.data.firstOrNull()?.currency ?: "USD",
                studentNames = installments.map { it.studentName }.filter { it.isNotBlank() }.distinct(),
                methods = invoices.methods.mapNotNull { Gateway.fromWire(it) },
                fees = fees,
                installments = installments,
                payments = payments.map { it.toPayment() },
                totals = totals.toTotals(),
            )
        }
    }
}

private fun Double.roundHalfUp(): Int = kotlin.math.floor(this + 0.5).toInt()

internal fun InvoiceDto.toInstallment() = Installment(
    id = id,
    number = installmentNumber,
    count = installmentCount,
    invoiceNo = invoiceNo,
    shareUrl = shareUrl,
    dueDate = dueDate?.take(10),
    amount = amount,
    paidAmount = paidAmount,
    status = InstallmentStatus.fromWire(status),
    feeAssignmentId = feeAssignmentId,
    feeName = feeName,
    studentId = studentId,
    studentName = studentName,
    academicYear = academicYear,
)

internal fun PaymentDto.toPayment() = FamilyPayment(
    id = id,
    feeAssignmentId = feeAssignmentId,
    paymentNumber = paymentNumber,
    amount = amount,
    paymentDate = paymentDate,
    paymentMethod = paymentMethod,
    status = status,
    feeName = feeName,
)

private fun TotalsDto.toTotals() = FamilyTotals(billed, paid, pendingVerification, remaining, overdue)
