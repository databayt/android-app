package org.hogwarts.android.feature.fees.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * The family money routes (hogwarts `api/mobile/fees/{invoices,payments,pay}`)
 * — they read the same `loadFamilyMoney` resolution as the web `/finance` page
 * — plus the accountant figures from `api/mobile/dashboard`, the only staff
 * finance numbers the mobile API serves.
 */
interface FeesApi {
    /** STUDENT / GUARDIAN only; anyone else is a 403. Ordered by due date. */
    @GET("api/mobile/fees/invoices")
    suspend fun invoices(
        @Query("lang") lang: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100,
    ): Response<InvoiceListResponse>

    /** SUCCESS payments plus PENDING_VERIFICATION proofs, newest first. */
    @GET("api/mobile/fees/payments")
    suspend fun payments(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100,
    ): Response<PaymentListResponse>

    /** Hosted checkout for a fee's whole remaining balance (Stripe / Tap only). */
    @POST("api/mobile/fees/pay")
    suspend fun pay(@Body body: PayRequest): Response<PayResponse>

    @GET("api/mobile/dashboard")
    suspend fun dashboard(): Response<StaffDashboardDto>
}

@Serializable
data class InvoiceListResponse(
    val data: List<InvoiceDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
    /** Null together with [totals] when the caller is not a resolvable family. */
    val currency: String? = null,
    val totals: TotalsDto? = null,
    val methods: List<String> = emptyList(),
)

@Serializable
data class InvoiceDto(
    /** An invoice id, or the fee assignment id for a lump fee with no invoice. */
    val id: String,
    @SerialName("invoice_no") val invoiceNo: String? = null,
    @SerialName("fee_assignment_id") val feeAssignmentId: String,
    @SerialName("fee_name") val feeName: String = "",
    @SerialName("student_id") val studentId: String = "",
    @SerialName("student_name") val studentName: String = "",
    @SerialName("academic_year") val academicYear: String = "",
    @SerialName("installment_number") val installmentNumber: Int = 1,
    @SerialName("installment_count") val installmentCount: Int = 1,
    /** ISO instant at midnight UTC, or null for a lump fee. */
    @SerialName("due_date") val dueDate: String? = null,
    val amount: Double = 0.0,
    @SerialName("paid_amount") val paidAmount: Double = 0.0,
    val remaining: Double = 0.0,
    val currency: String? = null,
    /** PAID | PARTIAL | PENDING | OVERDUE | CANCELLED */
    val status: String = "PENDING",
    /** The hosted invoice page, only when the school published it. */
    @SerialName("share_url") val shareUrl: String? = null,
)

@Serializable
data class TotalsDto(
    val billed: Double = 0.0,
    val paid: Double = 0.0,
    @SerialName("pending_verification") val pendingVerification: Double = 0.0,
    val remaining: Double = 0.0,
    val overdue: Double = 0.0,
)

@Serializable
data class PaymentListResponse(
    val data: List<PaymentDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
)

@Serializable
data class PaymentDto(
    val id: String,
    @SerialName("fee_assignment_id") val feeAssignmentId: String = "",
    @SerialName("payment_number") val paymentNumber: String = "",
    @SerialName("receipt_number") val receiptNumber: String = "",
    val amount: Double = 0.0,
    val currency: String? = null,
    @SerialName("payment_date") val paymentDate: String = "",
    @SerialName("payment_method") val paymentMethod: String = "",
    /** SUCCESS | PENDING_VERIFICATION */
    val status: String = "SUCCESS",
    @SerialName("fee_name") val feeName: String = "",
    @SerialName("student_id") val studentId: String = "",
    @SerialName("student_name") val studentName: String = "",
    @SerialName("academic_year") val academicYear: String = "",
)

@Serializable
data class PayRequest(
    @SerialName("fee_assignment_id") val feeAssignmentId: String,
    val gateway: String? = null,
    val lang: String,
)

@Serializable
data class PayResponse(
    @SerialName("checkout_url") val checkoutUrl: String,
    val gateway: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
)

/** The money fields `api/mobile/dashboard` returns for an ACCOUNTANT. */
@Serializable
data class StaffDashboardDto(
    val role: String = "",
    @SerialName("pending_invoices") val pendingInvoices: Int? = null,
    @SerialName("pending_amount") val pendingAmount: Double? = null,
    @SerialName("overdue_invoices") val overdueInvoices: Int? = null,
    @SerialName("overdue_amount") val overdueAmount: Double? = null,
    @SerialName("collected_today") val collectedToday: Double? = null,
)
