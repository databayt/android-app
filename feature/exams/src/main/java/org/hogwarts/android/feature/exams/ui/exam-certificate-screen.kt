package org.hogwarts.android.feature.exams.ui

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.FormError
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.exams.domain.model.ExamCertificate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCertificateScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExamCertificateViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exams_cert_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.exams_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.certificate == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    FormError(message = uiState.error ?: "Failed to load certificate")
                }
            }
            uiState.certificate != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    CertificateCard(
                        certificate = uiState.certificate!!,
                        modifier = Modifier.padding(AppleSpacing.Standard)
                    )

                    Spacer(modifier = Modifier.height(AppleSpacing.Standard))

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppleSpacing.Standard),
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Small)
                    ) {
                        // Share button
                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "I earned a certificate for ${uiState.certificate!!.examTitle}! " +
                                            "Score: ${uiState.certificate!!.score.toInt()}% | Grade: ${uiState.certificate!!.grade}\n" +
                                            "Verify: ${uiState.certificate!!.verificationCode}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Certificate"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = HogwartsIcons.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.exams_cert_share))
                        }

                        // Download PDF button
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(uiState.certificate!!.certificateUrl)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = HogwartsIcons.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.exams_cert_download))
                        }
                    }

                    Spacer(modifier = Modifier.height(AppleSpacing.Large))

                    // Verification info
                    Text(
                        text = "Verification Code: ${uiState.certificate!!.verificationCode}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = AppleSpacing.Large)
                    )
                }
            }
        }
    }
}

@Composable
private fun CertificateCard(
    certificate: ExamCertificate,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter
        .ofPattern("MMMM d, yyyy")
        .withZone(ZoneId.systemDefault())

    val qrBitmap = remember(certificate.verificationCode) {
        generateVerificationQrCode(certificate.verificationCode)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Inner border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(AppleSpacing.Large)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "CERTIFICATE OF ACHIEVEMENT",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(AppleSpacing.Large))

                // Student name
                Text(
                    text = "This is to certify that",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                Text(
                    text = certificate.studentName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppleSpacing.Standard))

                // Achievement details
                Text(
                    text = "has successfully completed the examination",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                Text(
                    text = certificate.examTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = certificate.subject,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppleSpacing.Large))

                // Score and grade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Score",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${certificate.score.toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Grade",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = certificate.grade,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppleSpacing.Large))

                // Date
                Text(
                    text = "Completed on ${dateFormatter.format(certificate.completedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(AppleSpacing.Large))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(AppleSpacing.Standard))

                // QR code for verification
                qrBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.exams_cert_qr_content_description),
                        modifier = Modifier.size(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                Text(
                    text = "Scan to verify",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = certificate.verificationCode,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Generate a simple QR-like bitmap for certificate verification.
 * In production, use a proper QR library such as ZXing.
 */
private fun generateVerificationQrCode(data: String, size: Int = 400): Bitmap? {
    return try {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val cellSize = size / 25
        val hash = data.hashCode()

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

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun CertificateCardPreview() {
    HogwartsTheme {
        CertificateCard(
            certificate = ExamCertificate(
                id = "cert-1",
                examId = "exam-1",
                studentName = "Ahmed Al-Hassan",
                examTitle = "Mathematics Final Examination",
                subject = "Mathematics",
                score = 92f,
                grade = "A+",
                completedAt = Instant.now(),
                certificateUrl = "https://hogwarts.example.com/certificates/cert-1.pdf",
                verificationCode = "HGW-2026-MATH-8A3F"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
