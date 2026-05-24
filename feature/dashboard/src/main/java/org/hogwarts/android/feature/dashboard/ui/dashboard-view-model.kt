package org.hogwarts.android.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.preferences.AppPreferences
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.dashboard.data.repository.DashboardRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Dashboard screen.
 *
 * Fetches role-based dashboard stats from the backend API.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val tenantContext: TenantContext,
    private val repository: DashboardRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
        observeWallpaper()
    }

    private fun observeWallpaper() {
        viewModelScope.launch {
            appPreferences.wallpaper.collect { id ->
                _uiState.update { it.copy(wallpaperId = id) }
            }
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val dto = repository.getDashboard()
                val role = try { UserRole.valueOf(dto.role) } catch (_: Exception) {
                    tenantContext.userRole ?: UserRole.STUDENT
                }

                _uiState.update {
                    it.copy(
                        userName = dto.userName.ifEmpty {
                            tenantContext.userName ?: "User"
                        },
                        userRole = role,
                        schoolName = dto.schoolName.ifEmpty { "School" },
                        isLoading = false,
                        isOffline = false,
                        todayClasses = dto.todayClasses ?: dto.totalClasses ?: 0,
                        attendancePercentage = dto.attendancePercentage ?: 0f,
                        unreadNotifications = dto.unreadNotifications,
                        upcomingExams = dto.upcomingExams ?: 0,
                        childrenCount = dto.childrenCount ?: 0,
                        totalStudents = dto.totalStudents ?: 0,
                    )
                }
            } catch (e: Exception) {
                Timber.d(e, "Dashboard API unavailable, using local context")
                // Fallback to tenant context only — no fake numbers
                val role = tenantContext.userRole ?: UserRole.STUDENT
                val userName = tenantContext.userName ?: "User"

                _uiState.update {
                    it.copy(
                        userName = userName,
                        userRole = role,
                        isLoading = false,
                        isOffline = true,
                    )
                }
            }
        }
    }

    fun refresh() {
        loadDashboard()
    }
}
