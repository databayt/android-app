package org.hogwarts.android.feature.dashboard.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET

/**
 * `GET /api/mobile/dashboard/sections` — the two real sections every role
 * dashboard renders under the quick actions on the web: the resource-usage
 * table (`resource-usage-section.tsx`) and the invoice history
 * (`invoice-history-section.tsx`).
 *
 * The third web section, `chart-section.tsx`, is deliberately absent: its
 * `generateBarChartData()` is a deterministic placeholder carrying a
 * "TODO: Replace with real data" comment, so there is nothing to mirror.
 */
interface DashboardSectionsApi {
    @GET("api/mobile/dashboard/sections")
    suspend fun getSections(): Response<DashboardSectionsDto>
}

@Serializable
data class DashboardSectionsDto(
    @SerialName("resource_usage") val resourceUsage: List<ResourceUsageDto> = emptyList(),
    val invoices: List<InvoiceDto> = emptyList(),
)

/**
 * One row of the usage table. [key] is the stable identifier the phone
 * localizes through `dashboard.resourceNames` (the web's `toCamelCase(name)`
 * lookup); [name] is the server's own wording, used when the key is unknown.
 */
@Serializable
data class ResourceUsageDto(
    val key: String = "",
    val name: String = "",
    val used: Double = 0.0,
    val limit: Double = 0.0,
    val unit: String? = null,
    /** The server's own figure; else used/limit, as `DetailedUsageTable` computes it. */
    val percent: Double? = null,
)

@Serializable
data class InvoiceDto(
    val id: String = "",
    val date: String = "",
    val description: String? = null,
    val amount: Double = 0.0,
    val currency: String? = null,
    /** `paid` | `refunded` | `open` | `void` — `InvoiceItem["status"]`. */
    val status: String = "",
)
