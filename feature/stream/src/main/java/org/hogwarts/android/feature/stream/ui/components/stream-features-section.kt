package org.hogwarts.android.feature.stream.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.atom.GlassCard
import org.hogwarts.android.core.designsystem.atom.GlassVariant

data class StreamFeature(
    val iconUrl: String,
    val title: String,
    val description: String
)

@Composable
fun StreamFeaturesSection(
    features: List<StreamFeature>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Fixed card height so all 4 cells match regardless of description length.
        val cardHeight = 200.dp
        val rows = features.chunked(2)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { feature ->
                    FeatureCard(
                        feature = feature,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                if (rowItems.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: StreamFeature, modifier: Modifier = Modifier) {
    // iOS 26 Liquid Glass — Figma node 112:608 (HqgFh4Lxp8QtTnW04czQQN).
    // Radius 34dp, Regular material (≈rgba(250,250,250,0.7)), soft 8/40 shadow.
    GlassCard(
        modifier = modifier,
        variant = GlassVariant.RegularLarge
    ) {
        Column {
            AsyncImage(
                model = feature.iconUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

object StreamFeatureDefaults {
    @Composable
    fun defaults(
        curatedTitle: String,
        curatedDesc: String,
        interactiveTitle: String,
        interactiveDesc: String,
        progressTitle: String,
        progressDesc: String,
        communityTitle: String,
        communityDesc: String
    ): List<StreamFeature> = listOf(
        StreamFeature(StreamAssets.featureCuratedCourses, curatedTitle, curatedDesc),
        StreamFeature(StreamAssets.featureInteractiveLearning, interactiveTitle, interactiveDesc),
        StreamFeature(StreamAssets.featureProgressTracking, progressTitle, progressDesc),
        StreamFeature(StreamAssets.featureCommunity, communityTitle, communityDesc)
    )
}
