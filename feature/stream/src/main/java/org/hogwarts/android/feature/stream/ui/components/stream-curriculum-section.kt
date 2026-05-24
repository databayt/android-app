package org.hogwarts.android.feature.stream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class CurriculumFeature(
    val iconUrl: String,
    val title: String,
    val description: String
)

@Composable
fun StreamCurriculumSection(
    title: String,
    description: String,
    features: List<CurriculumFeature>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF6A9BCC))
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(8.dp))
            val rows = features.chunked(2)
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    row.forEach { item ->
                        CurriculumItem(
                            feature = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CurriculumItem(feature: CurriculumFeature, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Contentful-hosted SVG icons are rendered black; tint them white for the blue card
        AsyncImage(
            model = feature.iconUrl,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = feature.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = feature.description,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

@Composable
fun defaultCurriculumFeatures(
    worldClassTitle: String,
    worldClassDesc: String,
    guidedTitle: String,
    guidedDesc: String,
    certificatesTitle: String,
    certificatesDesc: String,
    lmsTitle: String,
    lmsDesc: String
): List<CurriculumFeature> = listOf(
    CurriculumFeature(StreamAssets.curriculumWorldClassIcon, worldClassTitle, worldClassDesc),
    CurriculumFeature(StreamAssets.curriculumGuidedIcon, guidedTitle, guidedDesc),
    CurriculumFeature(StreamAssets.curriculumCertificatesIcon, certificatesTitle, certificatesDesc),
    CurriculumFeature(StreamAssets.curriculumLmsIcon, lmsTitle, lmsDesc)
)
