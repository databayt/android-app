package org.hogwarts.android.feature.stream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.hogwarts.android.feature.stream.R

data class StreamSkill(
    val title: String,
    val learners: String,
    val category: String,
    val accent: Color,
    val imageUrl: String
)

@Composable
fun StreamSkillsSection(
    title: String,
    description: String,
    skills: List<StreamSkill>,
    onSkillClick: (StreamSkill) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = skills, key = { it.title }) { skill ->
                SkillCard(
                    skill = skill,
                    onClick = { onSkillClick(skill) }
                )
            }
        }
    }
}

@Composable
private fun SkillCard(skill: StreamSkill, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(400.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(skill.accent)
            .clickable(onClick = onClick)
    ) {
        // CDN illustration, flipped for RTL to keep direction-aware art consistent
        AsyncImage(
            model = skill.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .flipForRtl(),
            contentScale = ContentScale.Crop
        )

        // White bottom info panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.stream_home_learners_format, skill.learners),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = skill.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun defaultStreamSkills(
    generativeAi: String,
    itCertifications: String,
    dataScience: String
): List<StreamSkill> = listOf(
    StreamSkill(
        title = generativeAi,
        learners = "1.7M+",
        category = "ai",
        accent = Color(0xFFD25F87),
        imageUrl = StreamAssets.skillGenerativeAi
    ),
    StreamSkill(
        title = itCertifications,
        learners = "14M+",
        category = "it",
        accent = Color(0xFF6A9BCC),
        imageUrl = StreamAssets.skillItCertifications
    ),
    StreamSkill(
        title = dataScience,
        learners = "8.1M+",
        category = "data-science",
        accent = Color(0xFFBCD1CA),
        imageUrl = StreamAssets.skillDataScience
    )
)
