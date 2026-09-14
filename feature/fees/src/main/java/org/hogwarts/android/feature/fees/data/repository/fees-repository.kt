package org.hogwarts.android.feature.fees.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.fees.data.remote.FeesApi
import org.hogwarts.android.feature.fees.data.remote.InvoiceListResponse
import org.hogwarts.android.feature.fees.data.remote.PayRequest
import org.hogwarts.android.feature.fees.data.remote.PaymentDto
import org.hogwarts.android.feature.fees.data.remote.StaffDashboardDto
import org.hogwarts.android.feature.fees.domain.FamilyMoney
import org.hogwarts.android.feature.fees.domain.Gateway
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface FamilyResult {
    /** [money] is null when the server resolves no student for this account. */
    data class Fresh(val money: FamilyMoney?) : FamilyResult
    /** The last good response, served because the network failed. */
    data class Cached(val money: FamilyMoney) : FamilyResult
    data class Failed(val message: String?) : FamilyResult
}

sealed interface CheckoutResult {
    data class Ready(val url: String) : CheckoutResult
    /** `fees/pay` refusals: UNAUTHORIZED, FEE_FULLY_PAID, PAYMENT_GATEWAY_UNAVAILABLE, PAYMENT_FAILED… */
    data class Refused(val code: Int) : CheckoutResult
    data class Failed(val message: String?) : CheckoutResult
}

interface FeesRepository {
    suspend fun cachedFamily(lang: String): FamilyMoney?
    suspend fun family(lang: String): FamilyResult
    /** The accountant figures from the dashboard route, or null when unavailable. */
    suspend fun staff(): StaffDashboardDto?
    suspend fun checkout(feeAssignmentId: String, gateway: Gateway, lang: String): CheckoutResult
}

/**
 * Network first, the last good family response as the offline fallback — the
 * web worker's saved-page behaviour. The cache is a per-user, per-language
 * file, so another sign-in never sees this family's money.
 */
@Singleton
class FeesRepositoryImpl @Inject constructor(
    private val api: FeesApi,
    private val json: Json,
    private val tenantContext: TenantContext,
    @ApplicationContext private val context: Context,
) : FeesRepository {

    private fun cacheFile(lang: String): File? =
        tenantContext.userId?.let { File(context.filesDir, "family-money-$it-$lang.json") }

    override suspend fun cachedFamily(lang: String): FamilyMoney? = withContext(Dispatchers.IO) {
        runCatching { cacheFile(lang)?.takeIf { it.exists() }?.readText()?.let { json.decodeFromString<FamilyMoney>(it) } }
            .onFailure { Timber.w(it, "Family money cache unreadable") }
            .getOrNull()
    }

    override suspend fun family(lang: String): FamilyResult = try {
        coroutineScope {
            val invoices = async { allInvoices(lang) }
            val payments = async { allPayments() }
            val money = FamilyMoney.from(invoices.await(), payments.await())
            withContext(Dispatchers.IO) {
                runCatching {
                    val file = cacheFile(lang)
                    if (money != null) file?.writeText(json.encodeToString(FamilyMoney.serializer(), money)) else file?.delete()
                }
            }
            FamilyResult.Fresh(money)
        }
    } catch (e: IOException) {
        cachedFamily(lang)?.let { FamilyResult.Cached(it) } ?: FamilyResult.Failed(e.message)
    } catch (e: HttpFailure) {
        cachedFamily(lang)?.let { FamilyResult.Cached(it) } ?: FamilyResult.Failed("HTTP ${e.code}")
    }

    private suspend fun allInvoices(lang: String): InvoiceListResponse {
        val first = api.invoices(lang = lang, page = 1).bodyOrThrow()
        var rows = first.data
        var page = 1
        while (rows.size < first.total && page * first.perPage < first.total) {
            page++
            rows = rows + api.invoices(lang = lang, page = page).bodyOrThrow().data
        }
        return first.copy(data = rows)
    }

    private suspend fun allPayments(): List<PaymentDto> {
        val first = api.payments(page = 1).bodyOrThrow()
        var rows = first.data
        var page = 1
        while (rows.size < first.total && page * first.perPage < first.total) {
            page++
            rows = rows + api.payments(page = page).bodyOrThrow().data
        }
        return rows
    }

    override suspend fun staff(): StaffDashboardDto? = try {
        api.dashboard().let { if (it.isSuccessful) it.body() else null }
    } catch (e: IOException) {
        Timber.w(e, "Finance figures unavailable")
        null
    }

    override suspend fun checkout(feeAssignmentId: String, gateway: Gateway, lang: String): CheckoutResult = try {
        val response = api.pay(PayRequest(feeAssignmentId = feeAssignmentId, gateway = gateway.wire, lang = lang))
        val body = response.body()
        if (response.isSuccessful && body != null && body.checkoutUrl.startsWith("https://")) {
            CheckoutResult.Ready(body.checkoutUrl)
        } else {
            CheckoutResult.Refused(response.code())
        }
    } catch (e: IOException) {
        CheckoutResult.Failed(e.message)
    }
}

private class HttpFailure(val code: Int) : RuntimeException("HTTP $code")

private fun <T> retrofit2.Response<T>.bodyOrThrow(): T {
    val body = body()
    if (isSuccessful && body != null) return body
    throw HttpFailure(code())
}
