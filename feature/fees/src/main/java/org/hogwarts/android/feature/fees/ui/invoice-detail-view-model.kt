package org.hogwarts.android.feature.fees.ui

import androidx.lifecycle.SavedStateHandle
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
import org.hogwarts.android.feature.fees.domain.model.Invoice
import org.hogwarts.android.feature.fees.domain.usecase.GetInvoiceDetailUseCase
import javax.inject.Inject

data class InvoiceDetailUiState(
    val isLoading: Boolean = true,
    val invoice: Invoice? = null,
    val error: String? = null
)

@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInvoiceDetailUseCase: GetInvoiceDetailUseCase,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val invoiceId: String = checkNotNull(savedStateHandle["invoiceId"])

    private val _uiState = MutableStateFlow(InvoiceDetailUiState())
    val uiState: StateFlow<InvoiceDetailUiState> = _uiState.asStateFlow()

    init {
        loadInvoiceDetail()
    }

    private fun loadInvoiceDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getInvoiceDetailUseCase(invoiceId = invoiceId)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, invoice = result.data, error = null)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }
}
