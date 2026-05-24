package org.hogwarts.android.feature.stream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.atom.HWButtonVariant
import org.hogwarts.android.core.designsystem.atom.HogwartsButton

@Composable
fun StreamAiFluencySection(
    badge: String,
    title: String,
    description: String,
    cta: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .height(450.dp)
    ) {
        // Background image — same asset as web (flipped horizontally in RTL)
        AsyncImage(
            model = StreamAssets.aiFluencyHero,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .flipForRtl(),
            contentScale = ContentScale.Crop
        )

        // Card overlay — anchored to start via Column's Start alignment
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 48.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFC2E9EB))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF0D5261)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1C1D1F)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6A6F73)
                )
                Spacer(Modifier.height(4.dp))
                HogwartsButton(
                    text = cta,
                    onClick = onCtaClick,
                    variant = HWButtonVariant.Default,
                    isFullWidth = false
                )
            }
        }
    }
}

// Retained for potential future gradient fallback when CDN is unreachable.
@Suppress("unused")
private val AiFluencyFallbackGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE8EFF5), Color(0xFFC2E9EB))
)
