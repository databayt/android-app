package org.hogwarts.android.feature.fees.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.data.util.networkBoundResource
import org.hogwarts.android.core.database.dao.FeeDao
import org.hogwarts.android.core.database.entity.FeeEntity
import org.hogwarts.android.feature.fees.data.remote.FeesApi
import org.hogwarts.android.feature.fees.data.remote.dto.FeeRecordDto
import org.hogwarts.android.feature.fees.data.remote.dto.ProcessPaymentRequestDto
import org.hogwarts.android.feature.fees.data.remote.dto.toDomain
import org.hogwarts.android.feature.fees.domain.model.FeeRecord
import org.hogwarts.android.feature.fees.domain.model.FeeStatus
import org.hogwarts.android.feature.fees.domain.model.FeeSummary
import org.hogwarts.android.feature.fees.domain.model.Invoice
import org.hogwarts.android.feature.fees.domain.model.PaymentMethod
import org.hogwarts.android.feature.fees.domain.model.PaymentTransaction
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeesRepositoryImpl @Inject constructor(
    private val api: FeesApi,
    private val dao: FeeDao,
    private val tenantContext: TenantContext
) : FeesRepository {

    override fun getFees(studentId: String?, status: String?): Flow<Resource<List<FeeRecord>>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                when {
                    studentId != null -> dao.observeByStudent(schoolId, studentId)
                    status != null -> dao.observeByStatus(schoolId, status)
                    else -> dao.observeAll(schoolId)
                }.map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                val response = api.getFees(studentId, status)
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                dao.insertAll(dtos.map { it.toEntity(schoolId) })
            }
        )
    }

    override suspend fun getFeeSummary(studentId: String): FeeSummary {
        val response = api.getFeeSummary(studentId)
        val dto = response.body() ?: throw Exception("No fee summary")
        return FeeSummary(
            totalAmount = BigDecimal.valueOf(dto.totalAmount),
            paidAmount = BigDecimal.valueOf(dto.paidAmount),
            pendingAmount = BigDecimal.valueOf(dto.pendingAmount),
            overdueAmount = BigDecimal.valueOf(dto.overdueAmount)
        )
    }

    override suspend fun getInvoices(
        studentId: String?,
        status: String?
    ): Result<List<Invoice>> {
        return try {
            val response = api.getInvoices(
                studentId = studentId,
                status = status
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.Success(body.data.map { it.toDomain() })
            } else {
                Result.Error(Exception("Failed to fetch invoices: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getInvoiceDetail(invoiceId: String): Result<Invoice> {
        return try {
            val response = api.getInvoiceDetail(
                invoiceId = invoiceId
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.Success(body.toDomain())
            } else {
                Result.Error(Exception("Failed to fetch invoice detail: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun processPayment(
        invoiceId: String?,
        feeId: String?,
        amount: BigDecimal,
        method: PaymentMethod
    ): Result<PaymentTransaction> {
        return try {
            val request = ProcessPaymentRequestDto(
                invoiceId = invoiceId,
                feeId = feeId,
                amount = amount.toDouble(),
                paymentMethod = method.name
            )
            val response = api.processPayment(request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.Success(body.toDomain())
            } else {
                Result.Error(Exception("Payment processing failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTransactions(
        startDate: String?,
        endDate: String?,
        status: String?
    ): Result<List<PaymentTransaction>> {
        return try {
            val response = api.getTransactions(
                startDate = startDate,
                endDate = endDate,
                status = status
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.Success(body.data.map { it.toDomain() })
            } else {
                Result.Error(Exception("Failed to fetch transactions: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTransactionDetail(transactionId: String): Result<PaymentTransaction> {
        return try {
            val response = api.getTransactionDetail(
                transactionId = transactionId
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.Success(body.toDomain())
            } else {
                Result.Error(Exception("Failed to fetch transaction detail: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

private fun FeeRecordDto.toEntity(schoolId: String) = FeeEntity(
    id = id,
    schoolId = schoolId,
    studentId = studentId,
    studentName = studentName,
    description = description,
    amount = amount,
    paidAmount = paidAmount,
    dueDate = LocalDate.parse(dueDate),
    status = status,
    category = category,
    term = term,
    lastSyncAt = Instant.now()
)

private fun FeeEntity.toDomain() = FeeRecord(
    id = id,
    studentId = studentId,
    studentName = studentName,
    description = description,
    amount = BigDecimal.valueOf(amount),
    paidAmount = BigDecimal.valueOf(paidAmount),
    dueDate = dueDate,
    status = FeeStatus.fromString(status),
    category = category,
    term = term
)
