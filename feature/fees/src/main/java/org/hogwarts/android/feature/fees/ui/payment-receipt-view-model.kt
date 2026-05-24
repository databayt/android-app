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
import org.hogwarts.android.feature.fees.domain.model.PaymentTransaction
import org.hogwarts.android.feature.fees.domain.usecase.GetTransactionDetailUseCase
import javax.inject.Inject

data class PaymentReceiptUiState(
    val isLoading: Boolean = true,
    val transaction: PaymentTransaction? = null,
    val error: String? = null
)

@HiltViewModel
class PaymentReceiptViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTransactionDetailUseCase: GetTransactionDetailUseCase
) : ViewModel() {

    private val transactionId: String = checkNotNull(savedStateHandle["transactionId"])

    private val _uiState = MutableStateFlow(PaymentReceiptUiState())
    val uiState: StateFlow<PaymentReceiptUiState> = _uiState.asStateFlow()

    init {
        loadTransaction()
    }

    private fun loadTransaction() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getTransactionDetailUseCase(transactionId = transactionId)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, transaction = result.data, error = null)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun downloadPdf() {
        // TODO: Implement PDF download via WorkManager or DownloadManager
        // This would trigger a background download and save the receipt PDF
    }
}
