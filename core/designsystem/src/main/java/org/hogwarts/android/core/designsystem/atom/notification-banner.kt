package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Push notification banner — Figma node 4:1613 (386×64 min-height).
 *
 * Container: radius=24, min-h=64, padding horizontal=14 vertical=12, gap=10.
 * Glass: rgba(255,255,255,0.07) mix-blend-screen, shadow 0x8y 40blur @ rgba(0,0,0,0.2).
 * Icon: 38dp rounded. Title: 15sp SemiBold line=17 tracking=-0.23.
 * Description: 15sp Regular line=18. Time: 15sp Regular line=17 right aligned.
 */
@Composable
fun NotificationBanner(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    time: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val containerShape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 40.dp,
                shape = containerShape,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(containerShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = containerShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(PaddingValues(horizontal = 14.dp, vertical = 12.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (icon != null) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(7.dp)
                                .size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Text content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 17.sp,
                            letterSpacing = (-0.23).sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.size(1.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            letterSpacing = (-0.23).sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (time != null) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 17.sp,
                            letterSpacing = (-0.23).sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationBannerPreview() {
    HogwartsTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            NotificationBanner(
                title = "Title",
                description = "Description",
                time = "9:41 AM",
                icon = Icons.Default.Notifications,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun NotificationBannerRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(modifier = Modifier.padding(16.dp)) {
                NotificationBanner(
                    title = "هوقورتس",
                    description = "تم قبول طلب التسجيل الخاص بك.",
                    time = "الآن",
                    icon = Icons.Default.Notifications,
                    onClick = {}
                )
            }
        }
    }
}

@Suppress("UnusedPrivateMember")
private fun widthHint() = Modifier.width(386.dp) // keep import stable; Figma exact width 386
