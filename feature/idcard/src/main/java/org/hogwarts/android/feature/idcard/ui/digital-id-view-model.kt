package org.hogwarts.android.feature.idcard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.idcard.domain.model.IdCard
import org.hogwarts.android.feature.idcard.domain.usecase.GetIdCardUseCase
import javax.inject.Inject

data class DigitalIdUiState(
    val idCard: IdCard? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DigitalIdViewModel @Inject constructor(
    private val getIdCardUseCase: GetIdCardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DigitalIdUiState())
    val uiState: StateFlow<DigitalIdUiState> = _uiState.asStateFlow()

    init {
        loadIdCard()
    }

    private fun loadIdCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getIdCardUseCase()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, idCard = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun refresh() = loadIdCard()
}
