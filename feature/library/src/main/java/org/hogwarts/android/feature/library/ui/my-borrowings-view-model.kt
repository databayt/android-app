package org.hogwarts.android.feature.library.ui

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
import org.hogwarts.android.feature.library.domain.model.Borrowing
import org.hogwarts.android.feature.library.domain.model.BorrowingStatus
import org.hogwarts.android.feature.library.domain.usecase.GetMyBorrowingsUseCase
import org.hogwarts.android.feature.library.domain.usecase.RenewBorrowingUseCase
import javax.inject.Inject

data class MyBorrowingsUiState(
    val activeBorrowings: List<Borrowing> = emptyList(),
    val historyBorrowings: List<Borrowing> = emptyList(),
    val isLoading: Boolean = false,
    val renewingId: String? = null,
    val error: String? = null,
    val renewSuccess: Boolean = false
)

@HiltViewModel
class MyBorrowingsViewModel @Inject constructor(
    private val getMyBorrowingsUseCase: GetMyBorrowingsUseCase,
    private val renewBorrowingUseCase: RenewBorrowingUseCase,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBorrowingsUiState())
    val uiState: StateFlow<MyBorrowingsUiState> = _uiState.asStateFlow()

    init {
        loadBorrowings()
    }

    private fun loadBorrowings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getMyBorrowingsUseCase()) {
                is Result.Success -> {
                    val all = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeBorrowings = all.filter { b -> b.isActive || b.isOverdue },
                            historyBorrowings = all.filter { b -> b.status == BorrowingStatus.RETURNED }
                        )
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun renewBorrowing(borrowingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(renewingId = borrowingId, error = null, renewSuccess = false) }
            when (val result = renewBorrowingUseCase(borrowingId)) {
                is Result.Success -> {
                    val renewed = result.data
                    _uiState.update { state ->
                        state.copy(
                            renewingId = null,
                            renewSuccess = true,
                            activeBorrowings = state.activeBorrowings.map { b ->
                                if (b.id == borrowingId) renewed else b
                            }
                        )
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(renewingId = null, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun refresh() = loadBorrowings()

    fun clearRenewSuccess() {
        _uiState.update { it.copy(renewSuccess = false) }
    }
}
