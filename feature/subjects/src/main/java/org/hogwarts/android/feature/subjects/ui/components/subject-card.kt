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
 * One subject, as `/subjects` draws it: a 58dp row with the name over its
 * level and grade, and the textbook's own cover square at the end — measured
 * off the live grid, which lays 186x58 cards out two to a row.
 *
 * The cover sits at the END, where the web puts it. It used to lead the row,
 * which in Arabic put it on the opposite side from the site. It is square and
 * uncut: the artwork is a book cover, and rounding its corners crops the
 * printing.
 *
 * No rating. The web's card carries none — the stars belong to the subject
 * page, not to a tile a reader is scanning twelve of.
 */
@Composable
fun SubjectCard(
    subject: Subject,
    levelLabel: String,
    gradeLabel: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = subject.name,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = colors.foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(levelLabel, gradeLabel).joinToString(" · "),
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.mutedForeground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SubjectThumbnail(
            imageUrl = subject.thumbnailUrl,
            color = subject.color,
            contentDescription = subject.name,
        )
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
    // 56dp square, uncut. The web rounds nothing here: the artwork is a book
    // cover and a radius crops the printing.
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
