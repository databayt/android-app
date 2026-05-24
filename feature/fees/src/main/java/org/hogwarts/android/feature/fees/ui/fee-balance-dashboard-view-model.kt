package org.hogwarts.android.feature.fees.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.fees.domain.model.FeeStatus
import org.hogwarts.android.feature.fees.domain.usecase.GetFeesUseCase
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

data class FeeBalanceDashboardUiState(
    val isLoading: Boolean = true,
    val outstandingBalance: BigDecimal = BigDecimal.ZERO,
    val overdueAmount: BigDecimal = BigDecimal.ZERO,
    val nextDueDate: LocalDate? = null,
    val nextDueAmount: BigDecimal = BigDecimal.ZERO,
    val currency: String = "SAR",
    val error: String? = null
)

@HiltViewModel
class FeeBalanceDashboardViewModel @Inject constructor(
    private val getFeesUseCase: GetFeesUseCase,
    private val tenantContext: TenantContext,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeeBalanceDashboardUiState())
    val uiState: StateFlow<FeeBalanceDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val userId = tenantContext.userId ?: return

        viewModelScope.launch {
            getFeesUseCase(studentId = userId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val fees = resource.data ?: emptyList()
                        val outstanding = fees
                            .filter { it.status != FeeStatus.PAID }
                            .sumOf { it.balance }
                        val overdue = fees
                            .filter { it.status == FeeStatus.OVERDUE }
                            .sumOf { it.balance }
                        val nextDueFee = fees
                            .filter { it.status != FeeStatus.PAID && it.dueDate >= LocalDate.now() }
                            .minByOrNull { it.dueDate }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                outstandingBalance = outstanding,
                                overdueAmount = overdue,
                                nextDueDate = nextDueFee?.dueDate,
                                nextDueAmount = nextDueFee?.balance ?: BigDecimal.ZERO,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        val fees = resource.data ?: emptyList()
                        val outstanding = fees
                            .filter { it.status != FeeStatus.PAID }
                            .sumOf { it.balance }
                        val overdue = fees
                            .filter { it.status == FeeStatus.OVERDUE }
                            .sumOf { it.balance }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                outstandingBalance = outstanding,
                                overdueAmount = overdue,
                                error = resource.error?.message
                            )
                        }
                    }
                }
            }
        }
    }
}
