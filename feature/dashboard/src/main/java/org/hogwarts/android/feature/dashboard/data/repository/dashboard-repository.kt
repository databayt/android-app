package org.hogwarts.android.feature.dashboard.data.repository

import org.hogwarts.android.feature.dashboard.data.remote.DashboardApi
import org.hogwarts.android.feature.dashboard.data.remote.DashboardDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val api: DashboardApi
) {
    suspend fun getDashboard(): DashboardDto {
        val response = api.getDashboard()
        return response.body() ?: throw Exception("Failed to load dashboard: ${response.code()}")
    }
}
