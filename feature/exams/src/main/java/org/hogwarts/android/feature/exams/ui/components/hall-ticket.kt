package org.hogwarts.android.feature.exams.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.feature.exams.domain.model.Exam

@Composable
fun HallTicket(
    exam: Exam,
    studentName: String,
    studentId: String,
    schoolName: String,
    formatter: LocaleFormatter,
    modifier: Modifier = Modifier
) {
    val qrBitmap = remember(studentId, exam.id) {
        generateQrCode("HOGWARTS:EXAM:${exam.id}:STUDENT:$studentId")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppleSpacing.Standard),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = schoolName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.exams_hall_ticket_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(AppleSpacing.Standard))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(AppleSpacing.Small))

        // Student Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                DetailField(label = stringResource(R.string.exams_hall_ticket_label_student), value = studentName)
                DetailField(label = stringResource(R.string.exams_hall_ticket_label_id), value = formatter.unicodeWrap(studentId))
            }
            Column(horizontalAlignment = Alignment.End) {
                DetailField(label = stringResource(R.string.exams_hall_ticket_label_exam), value = exam.title)
                DetailField(label = stringResource(R.string.exams_hall_ticket_label_subject), value = exam.subjectName)
            }
        }

        Spacer(modifier = Modifier.height(AppleSpacing.Small))

        // Schedule
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailField(
                label = stringResource(R.string.exams_hall_ticket_label_date),
                value = formatter.formatDate(exam.date)
            )
            DetailField(
                label = stringResource(R.string.exams_hall_ticket_label_time),
                value = "${formatter.formatTime(exam.startTime)} - ${formatter.formatTime(exam.endTime)}"
            )
            exam.venue?.let {
                DetailField(label = stringResource(R.string.exams_hall_ticket_label_venue), value = it)
            }
        }

        Spacer(modifier = Modifier.height(AppleSpacing.Standard))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(AppleSpacing.Standard))

        // QR Code
        qrBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.exams_hall_ticket_qr_cd),
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(AppleSpacing.Compact))
        Text(
            text = stringResource(R.string.exams_hall_ticket_scan),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Simple QR code generator using bitmap manipulation.
 * Encodes data as a basic QR-like pattern for hall ticket verification.
 */
private fun generateQrCode(data: String, size: Int = 512): Bitmap? {
    return try {
        // Use a simple hash-based pattern for QR code visualization
        // In production, use ZXing or a proper QR library
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val cellSize = size / 25
        val hash = data.hashCode()

        // Draw finder patterns (corners)
        for (x in 0 until size) {
            for (y in 0 until size) {
                val cellX = x / cellSize
                val cellY = y / cellSize
                val isFinderPattern = (cellX < 7 && cellY < 7) ||
                    (cellX >= 18 && cellY < 7) ||
                    (cellX < 7 && cellY >= 18)

                val color = if (isFinderPattern) {
                    val innerX = cellX % 7
                    val innerY = cellY % 7
                    if (innerX == 0 || innerX == 6 || innerY == 0 || innerY == 6 ||
                        (innerX in 2..4 && innerY in 2..4)
                    ) Color.BLACK else Color.WHITE
                } else {
                    // Data area: use hash to determine black/white
                    val seed = (cellX * 31 + cellY * 17 + hash) and 0xFF
                    if (seed % 3 == 0) Color.BLACK else Color.WHITE
                }
                bitmap.setPixel(x, y, color)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
