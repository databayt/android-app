package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.apple.AppleMaterial
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.liquidGlassCard
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Sheet header with grabber handle, title, and optional close button.
 *
 * Source: Figma iOS 26 — Sheet (node 8:2369)
 * Parity: swift-app/hogwarts/shared/atom/hw-sheet-header.swift
 */
@Composable
fun HogwartsSheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    showGrabber: Boolean = true,
    onClose: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showGrabber) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
            )
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onClose != null) {
                Spacer(Modifier.size(30.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            if (onClose != null) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Sheet header with thumbnail, title/subtitle, collaborate button, and close button.
 * Thumbnail: 64dp with 16dp corners, 0.5dp border, shadow.
 * Close button: 44dp touch target with 36dp liquid glass circle.
 * Gap between title block and collaborate button: 12dp.
 *
 * Source: Figma iOS 26 — Activity View Header (node 15:2505)
 * HIG: https://developer.apple.com/design/human-interface-guidelines/navigation-bars
 */
@Composable
fun SheetHeaderWithThumbnail(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    thumbnailUrl: String? = null,
    showGrabber: Boolean = true,
    onCollaborate: (() -> Unit)? = null,
    collaborateLabel: String = "Collaborate",
    secondaryText: String? = null,
    onClose: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showGrabber) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 5.dp)
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppleSpacing.Standard),
            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
        ) {
            if (thumbnailUrl != null) {
                val thumbnailShape = RoundedCornerShape(16.dp)
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .padding(start = 12.dp, top = 14.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = thumbnailShape,
                            ambientColor = Color.Black.copy(alpha = 0.15f),
                            spotColor = Color.Black.copy(alpha = 0.15f)
                        )
                        .size(64.dp)
                        .clip(thumbnailShape)
                        .border(0.5.dp, Color.Black.copy(alpha = 0.15f), thumbnailShape),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp, bottom = 2.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.23).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                letterSpacing = (-0.08).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (onCollaborate != null) {
                    Spacer(Modifier.height(12.dp))
                    LiquidGlassButton(
                        label = collaborateLabel,
                        onClick = onCollaborate
                    )
                }

                if (secondaryText != null) {
                    Spacer(Modifier.height(AppleSpacing.Tiny))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = secondaryText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 15.sp,
                                lineHeight = 18.sp,
                                letterSpacing = (-0.08).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            if (onClose != null) {
                Box(
                    modifier = Modifier.size(AppleSpacing.MinTouchTarget),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .liquidGlassCard(
                                cornerRadius = 1000.dp,
                                material = AppleMaterial.Thin
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SheetHeaderWithThumbnailPreview() {
    HogwartsTheme {
        Surface {
            SheetHeaderWithThumbnail(
                title = "Page Title",
                subtitle = "example.com",
                thumbnailUrl = null,
                onCollaborate = {},
                secondaryText = "Only invited people can edit.",
                onClose = {}
            )
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun SheetHeaderWithThumbnailRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface {
                SheetHeaderWithThumbnail(
                    title = "عنوان الصفحة",
                    subtitle = "example.com",
                    onCollaborate = {},
                    collaborateLabel = "تعاون",
                    secondaryText = "يمكن للمدعوين فقط التعديل.",
                    onClose = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HogwartsSheetHeaderPreview() {
    HogwartsTheme {
        Surface {
            HogwartsSheetHeader(
                title = "Sheet Title",
                onClose = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HogwartsSheetHeaderNoClosePreview() {
    HogwartsTheme {
        Surface {
            HogwartsSheetHeader(
                title = "Simple Sheet"
            )
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun HogwartsSheetHeaderRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface {
                HogwartsSheetHeader(
                    title = "عنوان الصفحة",
                    onClose = {}
                )
            }
        }
    }
}
