package org.hogwarts.android.feature.messaging.ui.whatsapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.messaging.data.repository.WhatsAppRepository
import javax.inject.Inject

data class WhatsAppUiState(
    val isLoading: Boolean = false,
    val status: String = "disconnected",
    val phone: String? = null,
    val qrCode: String? = null,
    val error: String? = null,
    val isPollingQR: Boolean = false,
) {
    val isConnected: Boolean get() = status == "connected"
}

@HiltViewModel
class WhatsAppViewModel @Inject constructor(
    private val repository: WhatsAppRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WhatsAppUiState())
    val uiState: StateFlow<WhatsAppUiState> = _uiState.asStateFlow()

    init {
        loadStatus()
    }

    fun loadStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val status = repository.getStatus()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        status = status.status,
                        phone = status.phone,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun connect() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val data = repository.connect()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        qrCode = data.qrCode,
                        status = data.status,
                        error = null,
                    )
                }
                if (data.qrCode != null) startPolling()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.disconnect()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        status = "disconnected",
                        phone = null,
                        qrCode = null,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun startPolling() {
        _uiState.update { it.copy(isPollingQR = true) }
        viewModelScope.launch {
            while (_uiState.value.isPollingQR && !_uiState.value.isConnected) {
                delay(5000)
                try {
                    val status = repository.getStatus()
                    _uiState.update {
                        it.copy(status = status.status, phone = status.phone)
                    }
                    if (status.isConnected) {
                        _uiState.update { it.copy(isPollingQR = false, qrCode = null) }
                    }
                } catch (_: Exception) { /* Keep polling */ }
            }
        }
    }
}
