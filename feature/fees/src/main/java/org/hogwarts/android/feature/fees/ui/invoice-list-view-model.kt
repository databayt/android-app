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
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.fees.domain.model.Invoice
import org.hogwarts.android.feature.fees.domain.model.InvoiceStatus
import org.hogwarts.android.feature.fees.domain.usecase.GetInvoicesUseCase
import javax.inject.Inject

data class InvoiceListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val invoices: List<Invoice> = emptyList(),
    val selectedStatus: InvoiceStatus? = null,
    val error: String? = null
)

@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val getInvoicesUseCase: GetInvoicesUseCase,
    private val tenantContext: TenantContext,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceListUiState())
    val uiState: StateFlow<InvoiceListUiState> = _uiState.asStateFlow()

    init {
        loadInvoices()
    }

    private fun loadInvoices() {
        val userId = tenantContext.userId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = _uiState.value.selectedStatus?.name
            when (val result = getInvoicesUseCase(studentId = userId, status = status)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        invoices = result.data,
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

    fun onStatusFilterChanged(status: InvoiceStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
        loadInvoices()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadInvoices()
    }
}
