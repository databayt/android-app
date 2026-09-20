package org.hogwarts.android.feature.lumos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.lumos.domain.model.Course

/**
 * One of the four cards under the hero — `content.tsx`'s feature grid, two to
 * a row: the emoji the dictionary carries, the title, the blurb.
 */
@Composable
fun FeatureCard(
    emoji: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    Column(
        modifier
            .clip(HogwartsShapes.Card)
            .border(1.dp, colors.border, HogwartsShapes.Card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(emoji, fontSize = 28.sp, lineHeight = 32.sp)
        Text(
            text = title,
            style = HogwartsTheme.type.bodyMedium,
            color = colors.foreground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = description,
            style = HogwartsTheme.type.caption,
            color = colors.mutedForeground,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A shelf of courses — `hot-releases-section.tsx` and the continue-watching
 * row share one shape: a heading over a sideways row of artwork.
 */
@Composable
fun CourseShelf(
    title: String,
    courses: List<Course>,
    onOpenCourse: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (courses.isEmpty()) return
    Column(modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            color = HogwartsTheme.colors.foreground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(courses, key = { it.id }) { course ->
                CourseCard(course = course, onClick = { onOpenCourse(course.id) })
            }
        }
    }
}

/**
 * The web's course card: the artwork on top, then the level, the title, and
 * what kind of course it is. The image is 16:9 rather than a square — these
 * are illustrated scenes, and squaring them cuts the scene in half.
 */
@Composable
private fun CourseCard(course: Course, onClick: () -> Unit) {
    val colors = HogwartsTheme.colors
    Column(
        modifier = Modifier
            .width(220.dp)
            .clip(HogwartsShapes.Card)
            .border(1.dp, colors.border, HogwartsShapes.Card)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(124.dp)
                .background(colors.muted),
            contentAlignment = Alignment.Center,
        ) {
            val art = course.thumbnailUrl ?: course.bannerUrl
            if (!art.isNullOrBlank()) {
                AsyncImage(
                    model = art,
                    contentDescription = course.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (course.category.isNotBlank()) {
                Text(
                    text = course.category,
                    style = HogwartsTheme.type.caption,
                    color = colors.mutedForeground,
                    maxLines = 1,
                )
            }
            Text(
                text = course.title,
                style = HogwartsTheme.type.bodyMedium,
                color = colors.foreground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (course.instructorName.isNotBlank()) {
                Text(
                    text = course.instructorName,
                    style = HogwartsTheme.type.caption,
                    color = colors.mutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * `teaching-hero-section.tsx` — the invitation at the foot of the page, a
 * bordered card rather than a second green ground: one green surface on a
 * page reads as a statement, two read as a theme.
 */
@Composable
fun TeachingCard(
    title: String,
    description: String,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .border(1.dp, colors.border, HogwartsShapes.Card)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            color = colors.foreground,
        )
        Text(
            text = description,
            style = HogwartsTheme.type.body,
            color = colors.mutedForeground,
        )
        Row(
            Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) { actions() }
    }
}
