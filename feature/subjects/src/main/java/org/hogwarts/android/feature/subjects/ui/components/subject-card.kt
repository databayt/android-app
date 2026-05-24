package org.hogwarts.android.feature.subjects.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.subjects.domain.model.Subject

/**
 * Horizontal subject card matching the web grid at
 * components/school-dashboard/listings/subjects/catalog-subjects-grid.tsx.
 *
 * Layout:  [64dp thumbnail — rounded start]  [name + badges + rating]
 */
@Composable
fun SubjectCard(
    subject: Subject,
    levelLabel: String,
    gradeLabel: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SubjectThumbnail(
                imageUrl = subject.thumbnailUrl,
                color = subject.color,
                contentDescription = subject.name,
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .padding(end = 12.dp, top = 10.dp, bottom = 10.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusBadge(
                        text = levelLabel,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    if (gradeLabel != null) {
                        StatusBadge(
                            text = gradeLabel,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                if (subject.averageRating > 0f) {
                    SubjectRating(
                        rating = subject.averageRating,
                        ratingCount = subject.ratingCount,
                    )
                }
            }
        }
    }
}

@Composable
internal fun SubjectThumbnail(
    imageUrl: String?,
    color: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val fallback = parseColor(color) ?: MaterialTheme.colorScheme.surfaceVariant
    val shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(shape)
            .background(fallback),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Parse a "#RRGGBB" or "#AARRGGBB" hex string. Returns null on unrecognized input. */
internal fun parseColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
}
