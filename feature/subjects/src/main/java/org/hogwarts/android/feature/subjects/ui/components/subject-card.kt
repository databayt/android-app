package org.hogwarts.android.feature.subjects.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
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
 * One subject, as `/subjects` draws it on a phone (`catalog-subjects-grid.tsx`):
 * the cover at the START of the row, then the name — up to two lines — over
 * the grade in an outlined pill.
 *
 * The cover leads because the web's markup puts it first, so in Arabic it
 * sits on the right, where the reader starts. Only its start corners are
 * rounded, by the card's own clip; the side against the text stays square.
 *
 * No stage label: the web hides that badge below `sm`, where a card is too
 * narrow to carry both it and the grade. Stars appear only once a subject
 * has a rating, which is when the web draws them.
 */
@Composable
fun SubjectCard(
    subject: Subject,
    gradeLabel: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SubjectThumbnail(
            imageUrl = subject.thumbnailUrl,
            color = subject.color,
            contentDescription = subject.name,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = subject.name,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
                color = colors.foreground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (gradeLabel != null) {
                Text(
                    text = gradeLabel,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.foreground,
                    maxLines = 1,
                    modifier = Modifier
                        .border(1.dp, colors.border, RoundedCornerShape(percent = 50))
                        .padding(horizontal = 6.dp),
                )
            }
            if (subject.averageRating > 0f) {
                SubjectRating(rating = subject.averageRating, ratingCount = subject.ratingCount)
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
    // 56dp square — `h-14 w-14` on a phone-width grid.
    Box(
        modifier = modifier
            .size(56.dp)
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
