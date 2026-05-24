package org.hogwarts.android.feature.stream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class StreamBeginStep(
    val imageUrl: String,
    val tabLabel: String,
    val description: String,
    val tip: String,
    val helpTitle: String,
    val helpText: String
)

@Composable
fun StreamHowToBeginSection(
    heading: String,
    steps: List<StreamBeginStep>,
    modifier: Modifier = Modifier
) {
    var activeIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = steps.withIndex().toList(), key = { it.index }) { indexed ->
                TabItem(
                    label = indexed.value.tabLabel,
                    isActive = activeIndex == indexed.index,
                    onClick = { activeIndex = indexed.index }
                )
            }
        }

        val current = steps.getOrNull(activeIndex) ?: return@Column

        // Step illustration from Udemy CDN (same as web) — flipped in RTL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = current.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .flipForRtl(),
                contentScale = ContentScale.Fit
            )
        }

        Text(
            text = current.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = current.tip,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = current.helpTitle,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = current.helpText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TabItem(label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (isActive) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isActive) 2.dp else 0.dp)
                .background(
                    if (isActive) MaterialTheme.colorScheme.onBackground
                    else Color.Transparent
                )
        )
    }
}

@Composable
fun defaultBeginSteps(
    planTab: String,
    planDesc: String,
    planTip: String,
    recordTab: String,
    recordDesc: String,
    recordTip: String,
    launchTab: String,
    launchDesc: String,
    launchTip: String,
    helpTitle: String,
    planHelp: String,
    recordHelp: String,
    launchHelp: String
): List<StreamBeginStep> = listOf(
    StreamBeginStep(StreamAssets.beginPlanIllustration, planTab, planDesc, planTip, helpTitle, planHelp),
    StreamBeginStep(StreamAssets.beginRecordIllustration, recordTab, recordDesc, recordTip, helpTitle, recordHelp),
    StreamBeginStep(StreamAssets.beginLaunchIllustration, launchTab, launchDesc, launchTip, helpTitle, launchHelp)
)
