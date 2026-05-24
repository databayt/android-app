package org.hogwarts.android.feature.subjects.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hogwarts.android.feature.subjects.domain.model.SubjectChapter

/**
 * A chapter section rendering `chapter.name` as a header followed by a
 * 2-column grid of [TopicCard]s for each lesson.
 *
 * Mirrors components/school-dashboard/listings/subjects/year-section.tsx.
 * Uses a plain Column + Rows instead of LazyVerticalGrid so it composes
 * cleanly inside the screen's outer LazyColumn.
 */
@Composable
fun ChapterSection(
    chapter: SubjectChapter,
    subjectFallbackColor: String?,
    modifier: Modifier = Modifier,
) {
    if (chapter.lessons.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = chapter.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        chapter.lessons.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TopicCard(
                    lesson = pair[0],
                    fallbackColor = chapter.color ?: subjectFallbackColor,
                    modifier = Modifier.weight(1f),
                )
                if (pair.size > 1) {
                    TopicCard(
                        lesson = pair[1],
                        fallbackColor = chapter.color ?: subjectFallbackColor,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
