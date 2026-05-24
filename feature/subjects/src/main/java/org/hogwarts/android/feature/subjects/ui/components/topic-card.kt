package org.hogwarts.android.feature.subjects.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.hogwarts.android.feature.subjects.domain.model.SubjectLesson

/**
 * Topic card matching components/school-dashboard/listings/subjects/topic-card.tsx.
 *
 * Layout: 16:9 image with vertical gradient and title overlay at bottom-start.
 */
@Composable
fun TopicCard(
    lesson: SubjectLesson,
    fallbackColor: String?,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val fallback = parseColor(lesson.color ?: fallbackColor)
        ?: MaterialTheme.colorScheme.surfaceVariant
    val shape = RoundedCornerShape(12.dp)

    var boxModifier = modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .clip(shape)
        .background(fallback)
    if (onClick != null) {
        boxModifier = boxModifier.clickable(onClick = onClick)
    }

    Box(modifier = boxModifier) {
        if (!lesson.thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = lesson.thumbnailUrl,
                contentDescription = lesson.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.7f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!lesson.description.isNullOrBlank()) {
                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
