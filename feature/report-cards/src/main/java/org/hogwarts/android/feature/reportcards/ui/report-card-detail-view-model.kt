package org.hogwarts.android.feature.reportcards.ui

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.reportcards.domain.model.ReportCard
import org.hogwarts.android.feature.reportcards.domain.usecase.DownloadReportCardPdfUseCase
import org.hogwarts.android.feature.reportcards.domain.usecase.GetReportCardDetailUseCase
import java.io.File
import javax.inject.Inject

/**
 * UI state for the report card detail screen.
 */
data class ReportCardDetailUiState(
    val isLoading: Boolean = true,
    val reportCard: ReportCard? = null,
    val error: String? = null,
    val isDownloadingPdf: Boolean = false,
    val pdfMessage: String? = null
)

/**
 * ViewModel for the report card detail screen.
 *
 * Handles loading report card details, PDF download, and sharing.
 */
@HiltViewModel
class ReportCardDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getReportCardDetailUseCase: GetReportCardDetailUseCase,
    private val downloadReportCardPdfUseCase: DownloadReportCardPdfUseCase,
    private val application: Application
) : ViewModel() {

    private val reportCardId: String = savedStateHandle["reportCardId"] ?: ""

    private val _uiState = MutableStateFlow(ReportCardDetailUiState())
    val uiState: StateFlow<ReportCardDetailUiState> = _uiState.asStateFlow()

    init {
        loadReportCard()
    }

    /**
     * Load the report card detail from backend.
     */
    private fun loadReportCard() {
        if (reportCardId.isBlank()) {
            _uiState.value = ReportCardDetailUiState(
                isLoading = false,
                error = "Invalid report card ID"
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getReportCardDetailUseCase(reportCardId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reportCard = result.data,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to load report card"
                        )
                    }
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }

    /**
     * Download the report card as PDF and save to Downloads.
     */
    fun downloadPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingPdf = true, pdfMessage = null) }

            when (val result = downloadReportCardPdfUseCase(reportCardId)) {
                is Result.Success -> {
                    try {
                        val reportCard = _uiState.value.reportCard
                        val fileName = buildPdfFileName(reportCard)

                        withContext(Dispatchers.IO) {
                            savePdfToDownloads(
                                bytes = result.data.bytes(),
                                fileName = fileName
                            )
                        }

                        _uiState.update {
                            it.copy(
                                isDownloadingPdf = false,
                                pdfMessage = "PDF saved to Downloads"
                            )
                        }
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                isDownloadingPdf = false,
                                pdfMessage = "Failed to save PDF: ${e.message}"
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isDownloadingPdf = false,
                            pdfMessage = "Download failed: ${result.exception.message}"
                        )
                    }
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }

    /**
     * Share the report card info.
     *
     * Launches an Android share sheet with report card summary text.
     */
    fun shareReportCard() {
        val reportCard = _uiState.value.reportCard ?: return

        val shareText = buildString {
            appendLine("Report Card - ${reportCard.studentName}")
            appendLine("Term: ${reportCard.termName} (${reportCard.academicYear})")
            if (reportCard.gpa != null) {
                appendLine("GPA: ${"%.2f".format(reportCard.gpa)}")
            }
            if (reportCard.rankDisplay != null) {
                appendLine("Rank: ${reportCard.rankDisplay}")
            }
            if (reportCard.subjects.isNotEmpty()) {
                appendLine()
                appendLine("Subjects:")
                reportCard.subjects.forEach { subject ->
                    appendLine("  ${subject.subjectName}: ${subject.grade} (${"%.0f".format(subject.percentage)}%)")
                }
            }
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Report Card - ${reportCard.studentName}")
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Report Card").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        application.startActivity(chooser)
    }

    /**
     * Clear the PDF message after it has been shown.
     */
    fun clearPdfMessage() {
        _uiState.update { it.copy(pdfMessage = null) }
    }

    /**
     * Retry loading the report card.
     */
    fun retry() {
        _uiState.value = ReportCardDetailUiState()
        loadReportCard()
    }

    /**
     * Build a descriptive PDF file name.
     */
    private fun buildPdfFileName(reportCard: ReportCard?): String {
        val studentName = reportCard?.studentName?.replace(" ", "_") ?: "report"
        val term = reportCard?.termName?.replace(" ", "_") ?: "term"
        val year = reportCard?.academicYear ?: "year"
        return "ReportCard_${studentName}_${term}_${year}.pdf"
    }

    /**
     * Save PDF bytes to the Downloads directory.
     *
     * Uses MediaStore on API 29+ and direct file access on older versions.
     */
    private fun savePdfToDownloads(bytes: ByteArray, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for API 29+
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = application.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw Exception("Failed to create MediaStore entry")
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(bytes)
            } ?: throw Exception("Failed to open output stream")
        } else {
            // Direct file access for older APIs
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val file = File(downloadsDir, fileName)
            file.writeBytes(bytes)
        }
    }
}
