package org.hogwarts.android.feature.reportcards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.reportcards.domain.model.ReportCard
import org.hogwarts.android.feature.reportcards.domain.model.ReportCardStatus
import org.hogwarts.android.feature.reportcards.domain.usecase.GetReportCardsUseCase
import javax.inject.Inject

/**
 * A single data point on the GPA trend line chart.
 *
 * @param label Display label (e.g. "Term 1 2024-2025")
 * @param gpa The GPA value for that term
 */
data class GpaTrendPoint(
    val label: String,
    val gpa: Float
)

/**
 * Per-subject performance bar data.
 *
 * @param subjectName Display name of the subject
 * @param percentage Average percentage across all terms
 * @param latestGrade The most recent grade achieved
 */
data class SubjectPerformance(
    val subjectName: String,
    val percentage: Float,
    val latestGrade: String
)

/**
 * UI state for the progress charts screen.
 */
data class ProgressChartsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val gpaTrend: List<GpaTrendPoint> = emptyList(),
    val subjectPerformances: List<SubjectPerformance> = emptyList(),
    val overallGpa: Float? = null,
    val gpaChange: Float? = null
)

/**
 * ViewModel for the progress charts screen.
 *
 * Loads all published report cards and computes:
 * - GPA trend across terms (line chart data)
 * - Per-subject average performance (bar chart data)
 * - Overall GPA and change from previous term
 */
@HiltViewModel
class ProgressChartsViewModel @Inject constructor(
    private val getReportCardsUseCase: GetReportCardsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressChartsUiState())
    val uiState: StateFlow<ProgressChartsUiState> = _uiState.asStateFlow()

    init {
        loadProgressData()
    }

    /**
     * Load report cards and compute chart data.
     */
    fun loadProgressData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getReportCardsUseCase()) {
                is Result.Success -> {
                    val published = result.data
                        .filter { it.status == ReportCardStatus.PUBLISHED }
                        .sortedBy { "${it.academicYear}-${it.termName}" }

                    val gpaTrend = buildGpaTrend(published)
                    val subjectPerformances = buildSubjectPerformances(published)
                    val overallGpa = gpaTrend.lastOrNull()?.gpa
                    val gpaChange = if (gpaTrend.size >= 2) {
                        gpaTrend.last().gpa - gpaTrend[gpaTrend.size - 2].gpa
                    } else {
                        null
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            gpaTrend = gpaTrend,
                            subjectPerformances = subjectPerformances,
                            overallGpa = overallGpa,
                            gpaChange = gpaChange,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to load progress data"
                        )
                    }
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    /**
     * Retry loading after an error.
     */
    fun retry() {
        loadProgressData()
    }

    /**
     * Build GPA trend data points from sorted report cards.
     */
    private fun buildGpaTrend(reportCards: List<ReportCard>): List<GpaTrendPoint> {
        return reportCards
            .filter { it.gpa != null }
            .map { card ->
                GpaTrendPoint(
                    label = "${card.termName}\n${card.academicYear}",
                    gpa = card.gpa!!
                )
            }
    }

    /**
     * Build per-subject average performance from all report cards.
     *
     * Groups all subject reports by name, computes average percentage,
     * and picks the latest grade.
     */
    private fun buildSubjectPerformances(
        reportCards: List<ReportCard>
    ): List<SubjectPerformance> {
        val allSubjects = reportCards.flatMap { it.subjects }
        if (allSubjects.isEmpty()) return emptyList()

        return allSubjects
            .groupBy { it.subjectName }
            .map { (name, reports) ->
                SubjectPerformance(
                    subjectName = name,
                    percentage = reports.map { it.percentage }.average().toFloat(),
                    latestGrade = reports.last().grade
                )
            }
            .sortedByDescending { it.percentage }
    }
}
