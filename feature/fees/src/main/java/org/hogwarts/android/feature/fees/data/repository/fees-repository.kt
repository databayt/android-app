package org.hogwarts.android.feature.fees.data.repository

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.fees.domain.model.FeeRecord
import org.hogwarts.android.feature.fees.domain.model.FeeSummary
import org.hogwarts.android.feature.fees.domain.model.Invoice
import org.hogwarts.android.feature.fees.domain.model.PaymentMethod
import org.hogwarts.android.feature.fees.domain.model.PaymentTransaction
import java.math.BigDecimal

interface FeesRepository {
    fun getFees(studentId: String? = null, status: String? = null): Flow<Resource<List<FeeRecord>>>
    suspend fun getFeeSummary(studentId: String): FeeSummary
    suspend fun getInvoices(studentId: String? = null, status: String? = null): Result<List<Invoice>>
    suspend fun getInvoiceDetail(invoiceId: String): Result<Invoice>
    suspend fun processPayment(invoiceId: String?, feeId: String?, amount: BigDecimal, method: PaymentMethod): Result<PaymentTransaction>
    suspend fun getTransactions(startDate: String? = null, endDate: String? = null, status: String? = null): Result<List<PaymentTransaction>>
    suspend fun getTransactionDetail(transactionId: String): Result<PaymentTransaction>
}
