package org.hogwarts.android.feature.dashboard.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.dashboard.data.remote.DashboardApi
import org.hogwarts.android.feature.dashboard.data.remote.DashboardDto
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface DashboardResult {
    data class Fresh(val data: DashboardDto) : DashboardResult
    /** Served from the last good response because the network failed. */
    data class Cached(val data: DashboardDto) : DashboardResult
    data class Failed(val message: String?) : DashboardResult
}

interface DashboardRepository {
    suspend fun cached(): DashboardDto?
    suspend fun refresh(): DashboardResult
}

/**
 * Network first, last good response as the offline fallback — the web
 * service worker's "saved page" behaviour for the dashboard. The cache is a
 * per-user file so a different sign-in never sees someone else's home.
 */
@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val api: DashboardApi,
    private val json: Json,
    private val tenantContext: TenantContext,
    @ApplicationContext private val context: Context,
) : DashboardRepository {

    private fun cacheFile(): File? =
        tenantContext.userId?.let { File(context.filesDir, "dashboard-$it.json") }

    override suspend fun cached(): DashboardDto? = withContext(Dispatchers.IO) {
        runCatching { cacheFile()?.takeIf { it.exists() }?.readText()?.let { json.decodeFromString<DashboardDto>(it) } }
            .onFailure { Timber.w(it, "Dashboard cache unreadable") }
            .getOrNull()
    }

    override suspend fun refresh(): DashboardResult {
        return try {
            val response = api.getDashboard()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                withContext(Dispatchers.IO) {
                    runCatching { cacheFile()?.writeText(json.encodeToString(DashboardDto.serializer(), body)) }
                }
                DashboardResult.Fresh(body)
            } else {
                cached()?.let { DashboardResult.Cached(it) } ?: DashboardResult.Failed("HTTP ${response.code()}")
            }
        } catch (e: IOException) {
            cached()?.let { DashboardResult.Cached(it) } ?: DashboardResult.Failed(e.message)
        }
    }
}
