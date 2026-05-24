package org.hogwarts.android.feature.idcard.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.feature.idcard.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

/**
 * Generates a Code128 barcode bitmap from the given content string.
 */
fun generateBarcodeBitmap(content: String, width: Int = 600, height: Int = 150): Bitmap? {
    return try {
        val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

/**
 * Generates a QR code bitmap from the given content string.
 */
fun generateQrCodeBitmap(content: String, size: Int = 300): Bitmap? {
    return try {
        val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

/**
 * Composable that renders a Code128 barcode.
 */
@Composable
fun BarcodeImage(
    content: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(content) { generateBarcodeBitmap(content) }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Barcode",
            modifier = modifier
                .fillMaxWidth()
                .height(60.dp),
            contentScale = ContentScale.FillBounds
        )
    }
}

/**
 * Composable that renders a QR code.
 */
@Composable
fun QrCodeImage(
    content: String,
    modifier: Modifier = Modifier,
    size: Int = 300
) {
    val bitmap = remember(content) { generateQrCodeBitmap(content, size) }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}
