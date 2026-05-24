package org.hogwarts.android.feature.admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.admin.domain.model.StaffMember
import org.hogwarts.android.feature.admin.domain.model.StaffRole
import org.hogwarts.android.feature.admin.domain.usecase.GetStaffUseCase
import javax.inject.Inject

data class StaffDirectoryUiState(
    val allStaff: List<StaffMember> = emptyList(),
    val filteredStaff: List<StaffMember> = emptyList(),
    val selectedRole: StaffRole? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StaffDirectoryViewModel @Inject constructor(
    private val getStaffUseCase: GetStaffUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffDirectoryUiState())
    val uiState: StateFlow<StaffDirectoryUiState> = _uiState.asStateFlow()

    init {
        loadStaff()
    }

    private fun loadStaff() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getStaffUseCase()) {
                is Result.Success -> {
                    val staff = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allStaff = staff,
                            filteredStaff = applyFilters(staff, it.selectedRole, it.searchQuery)
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun selectRole(role: StaffRole?) {
        _uiState.update { state ->
            state.copy(
                selectedRole = role,
                filteredStaff = applyFilters(state.allStaff, role, state.searchQuery)
            )
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredStaff = applyFilters(state.allStaff, state.selectedRole, query)
            )
        }
    }

    fun refresh() = loadStaff()

    private fun applyFilters(
        staff: List<StaffMember>,
        role: StaffRole?,
        search: String
    ): List<StaffMember> {
        return staff
            .filter { role == null || it.role == role }
            .filter { member ->
                search.isBlank() ||
                    member.displayName.contains(search, ignoreCase = true) ||
                    member.email.contains(search, ignoreCase = true) ||
                    member.department?.contains(search, ignoreCase = true) == true
            }
    }
}
