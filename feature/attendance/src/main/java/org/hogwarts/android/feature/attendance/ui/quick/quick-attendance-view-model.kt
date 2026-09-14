package org.hogwarts.android.feature.attendance.ui.quick

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepository
import org.hogwarts.android.feature.attendance.di.AttendanceClock
import org.hogwarts.android.feature.attendance.domain.model.MarkStatus
import org.hogwarts.android.feature.attendance.domain.model.QuickMark
import org.hogwarts.android.feature.attendance.domain.model.SaveOutcome
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Teacher-first, absent-oriented marking — mirrors `attendance/quick/content.tsx`.
 * Everyone starts present; a tap cycles present → absent → late; save sends
 * the exceptions for the whole section.
 */
@HiltViewModel
class QuickAttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    @AttendanceClock private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickAttendanceUiState(today = LocalDate.now(clock)))
    val uiState: StateFlow<QuickAttendanceUiState> = _uiState.asStateFlow()

    private var rosterJob: Job? = null

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(sections = null, loadFailed = false) }
        viewModelScope.launch {
            try {
                val today = LocalDate.now(clock)
                val context = repository.quickContext(today, LocalTime.now(clock))
                val first = context.sections.firstOrNull()
                _uiState.update {
                    it.copy(
                        today = context.today,
                        isSchoolDay = context.isSchoolDay,
                        sections = context.sections,
                        selectedSectionId = first?.id,
                        roster = first?.let { s -> context.rosters[s.id]?.students },
                    )
                }
                if (first != null && context.rosters[first.id] == null) loadRoster(first.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(sections = emptyList(), loadFailed = true) }
            }
        }
    }

    fun selectSection(sectionId: String) {
        if (sectionId == _uiState.value.selectedSectionId) return
        _uiState.update { it.copy(selectedSectionId = sectionId, saved = null, saveError = null, search = "", roster = null) }
        loadRoster(sectionId)
    }

    private fun loadRoster(sectionId: String) {
        rosterJob?.cancel()
        rosterJob = viewModelScope.launch {
            val students = try {
                repository.roster(sectionId, _uiState.value.today).students
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            }
            _uiState.update { if (it.selectedSectionId == sectionId) it.copy(roster = students) else it }
        }
    }

    fun cycle(studentId: String) {
        _uiState.update { state ->
            state.copy(
                roster = state.roster?.map { if (it.studentId == studentId) it.copy(status = it.status.next()) else it },
                saveError = null,
            )
        }
    }

    fun search(text: String) {
        _uiState.update { it.copy(search = text) }
    }

    fun markAnother() {
        _uiState.update { it.copy(saved = null) }
    }

    fun save() {
        val state = _uiState.value
        val sectionId = state.selectedSectionId ?: return
        val roster = state.roster?.takeIf { it.isNotEmpty() } ?: return
        if (state.saving) return

        val absent = roster.filter { it.status == MarkStatus.Absent }
        val late = roster.filter { it.status == MarkStatus.Late }
        val mark = QuickMark(
            sectionId = sectionId,
            date = state.today,
            absentStudentIds = absent.map { it.studentId },
            lateStudentIds = late.map { it.studentId },
        )
        _uiState.update { it.copy(saving = true, saveError = null) }

        viewModelScope.launch {
            val outcome = try {
                repository.submitQuick(mark)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SaveOutcome.Rejected(null)
            }
            val local = SavedPanel(
                present = roster.size - absent.size - late.size,
                absent = absent.size,
                late = late.size,
                guardiansNotified = null,
                absentNames = absent.map { it.name },
                queued = false,
            )
            _uiState.update { current ->
                when (outcome) {
                    is SaveOutcome.Saved -> current.copy(
                        saving = false,
                        saved = SavedPanel(
                            present = outcome.summary.present,
                            absent = outcome.summary.absent,
                            late = outcome.summary.late,
                            guardiansNotified = outcome.summary.guardiansNotified,
                            absentNames = local.absentNames,
                            queued = false,
                        ),
                        sections = current.sections?.map {
                            if (it.id == sectionId) it.copy(markedCount = outcome.summary.total) else it
                        },
                    )
                    SaveOutcome.AlreadyNewer -> current.copy(saving = false, saved = local)
                    SaveOutcome.Queued -> current.copy(saving = false, saved = local.copy(queued = true))
                    is SaveOutcome.Rejected -> current.copy(saving = false, saveError = SaveError(outcome.code))
                }
            }
        }
    }
}
