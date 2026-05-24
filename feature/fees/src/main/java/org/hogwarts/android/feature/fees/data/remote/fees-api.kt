package org.hogwarts.android.feature.fees.data.remote

import org.hogwarts.android.feature.fees.data.remote.dto.FeeListResponse
import org.hogwarts.android.feature.fees.data.remote.dto.FeeSummaryDto
import org.hogwarts.android.feature.fees.data.remote.dto.InvoiceDto
import org.hogwarts.android.feature.fees.data.remote.dto.InvoiceListResponse
import org.hogwarts.android.feature.fees.data.remote.dto.PaymentTransactionDto
import org.hogwarts.android.feature.fees.data.remote.dto.ProcessPaymentRequestDto
import org.hogwarts.android.feature.fees.data.remote.dto.TransactionListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FeesApi {

    @GET("api/mobile/fees")
    suspend fun getFees(
        @Query("studentId") studentId: String? = null,
        @Query("status") status: String? = null
    ): Response<FeeListResponse>

    @GET("api/mobile/fees/summary/{studentId}")
    suspend fun getFeeSummary(
        @Path("studentId") studentId: String
    ): Response<FeeSummaryDto>

    // NOTE: Web endpoint being added — will 404 until available
    @GET("api/mobile/invoices")
    suspend fun getInvoices(
        @Query("studentId") studentId: String? = null,
        @Query("status") status: String? = null
    ): Response<InvoiceListResponse>

    // NOTE: Web endpoint being added — will 404 until available
    @GET("api/mobile/invoices/{invoiceId}")
    suspend fun getInvoiceDetail(
        @Path("invoiceId") invoiceId: String
    ): Response<InvoiceDto>

    // NOTE: Web endpoint being added — will 404 until available
    @POST("api/mobile/payments/process")
    suspend fun processPayment(
        @Body request: ProcessPaymentRequestDto
    ): Response<PaymentTransactionDto>

    // NOTE: Web endpoint being added — will 404 until available
    @GET("api/mobile/payments/transactions")
    suspend fun getTransactions(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("status") status: String? = null
    ): Response<TransactionListResponse>

    // NOTE: Web endpoint being added — will 404 until available
    @GET("api/mobile/payments/transactions/{transactionId}")
    suspend fun getTransactionDetail(
        @Path("transactionId") transactionId: String
    ): Response<PaymentTransactionDto>
}
