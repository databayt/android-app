package org.hogwarts.android.feature.attendance.ui.kiosk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.utils.LocaleFormatter
import java.time.LocalTime
import javax.inject.Inject

data class ScanRecord(
    val studentId: String,
    val studentName: String,
    val time: String,
    val success: Boolean,
    val message: String
)

data class KioskUiState(
    val schoolName: String = "Hogwarts School",
    val presentCount: Int = 0,
    val lateCount: Int = 0,
    val totalScans: Int = 0,
    val lastScan: ScanRecord? = null,
    val recentScans: List<ScanRecord> = emptyList(),
    val isScanning: Boolean = true
)

@HiltViewModel
class KioskModeViewModel @Inject constructor(
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(KioskUiState())
    val uiState: StateFlow<KioskUiState> = _uiState.asStateFlow()

    private val adminPin = "123456" // In production, from encrypted storage

    fun onStudentScanned(studentId: String) {
        viewModelScope.launch {
            val scan = ScanRecord(
                studentId = studentId,
                studentName = "Student $studentId",
                time = localeFormatter.formatTime(LocalTime.now()),
                success = true,
                message = "Checked in successfully"
            )
            _uiState.update { state ->
                state.copy(
                    presentCount = state.presentCount + 1,
                    totalScans = state.totalScans + 1,
                    lastScan = scan,
                    recentScans = listOf(scan) + state.recentScans.take(19)
                )
            }
        }
    }

    fun verifyAdminPin(pin: String): Boolean = pin == adminPin
}
