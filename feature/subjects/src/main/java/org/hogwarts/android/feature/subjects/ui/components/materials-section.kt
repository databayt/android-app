package org.hogwarts.android.feature.subjects.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.domain.model.MaterialItem

/**
 * Fixed order of material type tiles, matching MATERIAL_TYPE_PIPELINE in
 * catalog-content-sections.tsx lines 127–139. Tiles always render, count=0
 * when no items exist — this is a "pipeline" not a list.
 */
private val MATERIAL_TYPE_PIPELINE = listOf(
    "TEXTBOOK" to Icons.AutoMirrored.Filled.MenuBook,
    "SYLLABUS" to Icons.Outlined.Assignment,
    "REFERENCE" to Icons.Filled.LibraryBooks,
    "STUDY_GUIDE" to Icons.Filled.School,
    "PROJECT" to Icons.Filled.Folder,
    "WORKSHEET" to Icons.Filled.Edit,
    "PRESENTATION" to Icons.Filled.Slideshow,
    "LESSON_NOTES" to Icons.AutoMirrored.Filled.Note,
    "VIDEO_GUIDE" to Icons.Filled.Videocam,
    "LAB_MANUAL" to Icons.Filled.Science,
    "OTHER" to Icons.Filled.Description,
)

private data class MaterialTypeGroup(
    val key: String,
    val icon: ImageVector,
    val count: Int,
    val avgPages: Int?,
)

@Composable
fun MaterialsSection(
    materials: List<MaterialItem>,
    accentColor: Color,
    textbookPdfUrl: String?,
    textbookCoverUrl: String?,
    modifier: Modifier = Modifier,
    onTextbookClick: (() -> Unit)? = null,
) {
    val groups = MATERIAL_TYPE_PIPELINE.map { (key, icon) ->
        val items = materials.filter { it.type == key }
        val avgPages = items.mapNotNull { it.pageCount }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toInt()
        MaterialTypeGroup(
            key = key,
            icon = icon,
            count = items.size,
            avgPages = avgPages,
        )
    }
    val hasTextbook = !textbookPdfUrl.isNullOrBlank() || !textbookCoverUrl.isNullOrBlank()

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
    ) {
        items(items = groups, key = { it.key }) { group ->
            if (group.key == "TEXTBOOK" && hasTextbook) {
                TextbookTile(
                    label = stringResource(materialTypeLabelRes(group.key)),
                    coverUrl = textbookCoverUrl,
                    accentColor = accentColor,
                    onClick = onTextbookClick,
                )
            } else {
                MaterialTypeTile(
                    label = stringResource(materialTypeLabelRes(group.key)),
                    icon = group.icon,
                    count = group.count,
                    avgPages = group.avgPages,
                )
            }
        }
    }
}

@Composable
private fun MaterialTypeTile(
    label: String,
    icon: ImageVector,
    count: Int,
    avgPages: Int?,
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.subjects_materials_count, count),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (avgPages != null) {
                Text(
                    text = stringResource(R.string.subjects_materials_avg_pages, avgPages),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TextbookTile(
    label: String,
    coverUrl: String?,
    accentColor: Color,
    onClick: (() -> Unit)?,
) {
    val shape = RoundedCornerShape(16.dp)
    var modifier = Modifier
        .width(160.dp)
        .height(200.dp)
        .clip(shape)
        .background(accentColor.copy(alpha = 0.15f))
    if (onClick != null) modifier = modifier.clickable(onClick = onClick)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (!coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(36.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/** Map a material type key to its localized label resource. */
private fun materialTypeLabelRes(type: String): Int = when (type) {
    "TEXTBOOK" -> R.string.subjects_material_textbook
    "SYLLABUS" -> R.string.subjects_material_syllabus
    "REFERENCE" -> R.string.subjects_material_reference
    "STUDY_GUIDE" -> R.string.subjects_material_study_guide
    "PROJECT" -> R.string.subjects_material_project
    "WORKSHEET" -> R.string.subjects_material_worksheet
    "PRESENTATION" -> R.string.subjects_material_presentation
    "LESSON_NOTES" -> R.string.subjects_material_lesson_notes
    "VIDEO_GUIDE" -> R.string.subjects_material_video_guide
    "LAB_MANUAL" -> R.string.subjects_material_lab_manual
    else -> R.string.subjects_material_other
}
