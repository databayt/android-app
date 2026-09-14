package org.hogwarts.android.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.preferences.AppPreferences
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.auth.data.remote.AuthApi
import org.hogwarts.android.feature.dashboard.data.repository.DashboardRepository
import timber.log.Timber
import javax.inject.Inject

data class ShellUiState(
    val role: UserRole? = null,
    val userName: String? = null,
    /** Web-side module toggles; null means every module is visible. */
    val enabledModules: List<String>? = null,
    /** The school's subdomain, used for web handoff. */
    val schoolDomain: String? = null,
    val menuOpen: Boolean = false,
)

@HiltViewModel
class ShellViewModel @Inject constructor(
    private val tenantContext: TenantContext,
    private val authApi: AuthApi,
    private val preferences: AppPreferences,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ShellUiState(role = tenantContext.userRole, userName = tenantContext.userName)
    )
    val state: StateFlow<ShellUiState> = _state.asStateFlow()

    val themeMode: StateFlow<String> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    init {
        resolveSchoolDomain()
        viewModelScope.launch {
            // Same source the web sidebar reads: School.enabledModules, null = all on.
            dashboardRepository.latest.collect { dto ->
                if (dto != null) _state.update { it.copy(enabledModules = dto.school?.enabledModules) }
            }
        }
    }

    fun refreshSession() {
        _state.update { it.copy(role = tenantContext.userRole, userName = tenantContext.userName) }
    }

    fun setMenuOpen(open: Boolean) = _state.update { it.copy(menuOpen = open) }

    fun cycleTheme(isDarkNow: Boolean) {
        viewModelScope.launch { preferences.setThemeMode(if (isDarkNow) "light" else "dark") }
    }

    private fun resolveSchoolDomain() {
        val schoolId = tenantContext.schoolId ?: return
        viewModelScope.launch {
            try {
                val schools = authApi.getSchools().body().orEmpty()
                _state.update { state -> state.copy(schoolDomain = schools.firstOrNull { it.id == schoolId }?.domain) }
            } catch (e: Exception) {
                Timber.w(e, "School domain lookup failed")
            }
        }
    }
}
