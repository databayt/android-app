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
import org.hogwarts.android.feature.fees.domain.model.PaymentMethod
import org.hogwarts.android.feature.fees.domain.usecase.GetInvoiceDetailUseCase
import org.hogwarts.android.feature.fees.domain.usecase.ProcessPaymentUseCase
import java.math.BigDecimal
import javax.inject.Inject

enum class ProcessingState {
    IDLE, SELECTING, PROCESSING, SUCCESS, FAILURE
}

data class PaymentProcessingUiState(
    val invoiceId: String = "",
    val selectedMethod: PaymentMethod = PaymentMethod.CARD,
    val amount: BigDecimal = BigDecimal.ZERO,
    val currency: String = "SAR",
    val processingState: ProcessingState = ProcessingState.IDLE,
    val transactionId: String? = null,
    val error: String? = null
)

@HiltViewModel
class PaymentProcessingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInvoiceDetailUseCase: GetInvoiceDetailUseCase,
    private val processPaymentUseCase: ProcessPaymentUseCase
) : ViewModel() {

    private val invoiceId: String = checkNotNull(savedStateHandle["invoiceId"])

    private val _uiState = MutableStateFlow(PaymentProcessingUiState(invoiceId = invoiceId))
    val uiState: StateFlow<PaymentProcessingUiState> = _uiState.asStateFlow()

    init {
        loadInvoiceAmount()
    }

    private fun loadInvoiceAmount() {
        viewModelScope.launch {
            when (val result = getInvoiceDetailUseCase(invoiceId = invoiceId)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        amount = result.data.balanceDue,
                        processingState = ProcessingState.SELECTING
                    )
                }
                is Result.Error -> _uiState.update {
                    it.copy(
                        error = result.exception.message,
                        processingState = ProcessingState.FAILURE
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun onPaymentMethodSelected(method: PaymentMethod) {
        _uiState.update { it.copy(selectedMethod = method) }
    }

    fun confirmPayment() {
        _uiState.update { it.copy(processingState = ProcessingState.PROCESSING) }
        viewModelScope.launch {
            val state = _uiState.value
            when (val result = processPaymentUseCase(
                invoiceId = invoiceId,
                amount = state.amount,
                method = state.selectedMethod
            )) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        processingState = ProcessingState.SUCCESS,
                        transactionId = result.data.id
                    )
                }
                is Result.Error -> _uiState.update {
                    it.copy(
                        processingState = ProcessingState.FAILURE,
                        error = result.exception.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetToSelection() {
        _uiState.update {
            it.copy(
                processingState = ProcessingState.SELECTING,
                error = null,
                transactionId = null
            )
        }
    }
}
