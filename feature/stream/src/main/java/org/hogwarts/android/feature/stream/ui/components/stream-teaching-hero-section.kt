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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.atom.HWButtonVariant
import org.hogwarts.android.core.designsystem.atom.HogwartsButton

@Composable
fun StreamTeachingHeroSection(
    title: String,
    description: String,
    primaryLabel: String,
    secondaryLabel: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pink illustration card — same hex (#D25F87) and Webflow asset as the web
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFD25F87)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = StreamAssets.teachingHeroIllustration,
                contentDescription = null,
                modifier = Modifier
                    .size(144.dp)
                    .flipForRtl(),
                contentScale = ContentScale.Fit
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HogwartsButton(
                text = primaryLabel,
                onClick = onPrimary,
                variant = HWButtonVariant.Default,
                isFullWidth = false
            )
            HogwartsButton(
                text = secondaryLabel,
                onClick = onSecondary,
                variant = HWButtonVariant.Ghost,
                isFullWidth = false
            )
        }
    }
}
