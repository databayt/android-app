package org.hogwarts.android.feature.idcard.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.idcard.R
import org.hogwarts.android.feature.idcard.domain.model.IdCard
import org.hogwarts.android.feature.idcard.domain.usecase.GetIdCardUseCase
import org.hogwarts.android.feature.idcard.ui.components.generateBarcodeBitmap
import org.hogwarts.android.feature.idcard.ui.components.generateQrCodeBitmap
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class IdCardPdfUiState(
    val idCard: IdCard? = null,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val pdfUri: Uri? = null,
    val error: String? = null
)

@HiltViewModel
class IdCardPdfViewModel @Inject constructor(
    private val getIdCardUseCase: GetIdCardUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(IdCardPdfUiState())
    val uiState: StateFlow<IdCardPdfUiState> = _uiState.asStateFlow()

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

    fun generatePdf() {
        val idCard = _uiState.value.idCard ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            try {
                val uri = withContext(Dispatchers.IO) {
                    createIdCardPdf(idCard)
                }
                _uiState.update { it.copy(isGenerating = false, pdfUri = uri) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, error = e.message) }
            }
        }
    }

    fun sharePdf() {
        val uri = _uiState.value.pdfUri ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
            Intent.createChooser(shareIntent, context.getString(R.string.idcard_share_pdf_chooser)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    private fun createIdCardPdf(idCard: IdCard): Uri {
        val document = PdfDocument()

        // Credit card size: 3.375" x 2.125" at 72 DPI = ~243 x 153 pts
        val pageWidth = 486
        val pageHeight = 306

        // Front page
        val frontPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val frontPage = document.startPage(frontPageInfo)
        drawFrontPage(frontPage.canvas, idCard, pageWidth, pageHeight)
        document.finishPage(frontPage)

        // Back page
        val backPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val backPage = document.startPage(backPageInfo)
        drawBackPage(backPage.canvas, idCard, pageWidth, pageHeight)
        document.finishPage(backPage)

        // Save to cache dir
        val file = File(context.cacheDir, "id-card-${idCard.idNumber}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun drawFrontPage(canvas: Canvas, idCard: IdCard, width: Int, height: Int) {
        val bgPaint = Paint().apply { color = android.graphics.Color.rgb(240, 245, 255) }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Border
        val borderPaint = Paint().apply {
            color = android.graphics.Color.rgb(59, 130, 246)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(4f, 4f, width - 4f, height - 4f, borderPaint)

        // School name
        val schoolPaint = Paint().apply {
            color = android.graphics.Color.rgb(30, 64, 175)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(idCard.schoolName, width / 2f, 36f, schoolPaint)

        // Name
        val namePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(idCard.name, width / 2f, 90f, namePaint)

        // Role
        val rolePaint = Paint().apply {
            color = android.graphics.Color.rgb(100, 100, 100)
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(idCard.role, width / 2f, 112f, rolePaint)

        // ID Number
        val idPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            context.getString(R.string.idcard_id_prefix, idCard.idNumber),
            width / 2f, 145f, idPaint
        )

        // Valid until
        val validPaint = Paint().apply {
            color = android.graphics.Color.rgb(100, 100, 100)
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            context.getString(R.string.idcard_valid_until_prefix, idCard.validUntil),
            width / 2f, 165f, validPaint
        )

        // Barcode
        val barcodeBitmap = generateBarcodeBitmap(idCard.barcode, 400, 80)
        barcodeBitmap?.let {
            canvas.drawBitmap(it, (width - 400f) / 2f, 180f, null)
        }

        // QR
        val qrBitmap = generateQrCodeBitmap(idCard.qrContent, 80)
        qrBitmap?.let {
            canvas.drawBitmap(it, (width - 80f) / 2f, height - 96f, null)
        }
    }

    private fun drawBackPage(canvas: Canvas, idCard: IdCard, width: Int, height: Int) {
        val bgPaint = Paint().apply { color = android.graphics.Color.rgb(240, 245, 255) }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val borderPaint = Paint().apply {
            color = android.graphics.Color.rgb(59, 130, 246)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(4f, 4f, width - 4f, height - 4f, borderPaint)

        val titlePaint = Paint().apply {
            color = android.graphics.Color.rgb(30, 64, 175)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(idCard.schoolName, width / 2f, 36f, titlePaint)

        val bodyPaint = Paint().apply {
            color = android.graphics.Color.rgb(60, 60, 60)
            textSize = 11f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(
            context.getString(R.string.idcard_pdf_property_line1, idCard.schoolName),
            width / 2f, 70f, bodyPaint
        )
        canvas.drawText(
            context.getString(R.string.idcard_pdf_property_line2),
            width / 2f, 88f, bodyPaint
        )
        canvas.drawText(
            context.getString(R.string.idcard_pdf_unauthorized),
            width / 2f, 106f, bodyPaint
        )

        // QR on back too
        val qrBitmap = generateQrCodeBitmap(idCard.qrContent, 120)
        qrBitmap?.let {
            canvas.drawBitmap(it, (width - 120f) / 2f, 130f, null)
        }

        val footerPaint = Paint().apply {
            color = android.graphics.Color.rgb(130, 130, 130)
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            context.getString(R.string.idcard_pdf_powered_by),
            width / 2f, height - 16f, footerPaint
        )
    }
}
