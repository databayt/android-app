package org.hogwarts.android.feature.fees.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.fees.domain.model.PaymentStatus
import org.hogwarts.android.feature.fees.domain.model.PaymentTransaction
import org.hogwarts.android.feature.fees.domain.usecase.GetTransactionsUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TransactionHistoryUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val transactions: List<PaymentTransaction> = emptyList(),
    val selectedStatus: PaymentStatus? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val error: String? = null
)

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val tenantContext: TenantContext
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    private val isoFormatter = DateTimeFormatter.ISO_INSTANT

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val state = _uiState.value
            when (val result = getTransactionsUseCase(
                status = state.selectedStatus?.name,
                startDate = state.startDate?.let { isoFormatter.format(it) },
                endDate = state.endDate?.let { isoFormatter.format(it) }
            )) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        transactions = result.data,
                        error = null
                    )
                }
                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.exception.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun onStatusFilterChanged(status: PaymentStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
        loadTransactions()
    }

    fun onDateRangeChanged(start: Instant?, end: Instant?) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        loadTransactions()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadTransactions()
    }
}
