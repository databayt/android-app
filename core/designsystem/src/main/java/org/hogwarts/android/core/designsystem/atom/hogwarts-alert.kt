package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * iOS-style alert dialog — Figma node 7:2186 (402×300 canvas, alert 270 wide).
 *
 * Container: w=270, radius=14, pt=19 pb=0.
 * Title+Description block: pb=15, px=16, gap=2, center-aligned.
 *   Title: 17sp SemiBold(590), line=22, tracking=-0.43.
 *   Description: 13sp Regular(400), line=18, tracking=-0.08.
 * Button row: border-top 0.333dp rgba(128,128,128,0.55) ≈ outlineVariant.
 *   Height: 44. Text 17sp SemiBold(590) line=22 tracking=-0.43, color=primary (#007AFF).
 *   Two-button variant: vertical separator between, same hairline color, equal widths.
 */
@Composable
fun HogwartsAlert(
    title: String,
    message: String? = null,
    primaryAction: String,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryAction: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    val separatorColor = MaterialTheme.colorScheme.outlineVariant
    val separatorThickness = 0.333.dp

    Surface(
        modifier = modifier.width(270.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Title + Description
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 19.dp,
                            bottom = 15.dp
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        letterSpacing = (-0.43).sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            letterSpacing = (-0.08).sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Button row separator (top border)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(separatorThickness)
                    .background(separatorColor)
            )

            if (secondaryAction != null && onSecondaryAction != null) {
                // Two buttons side by side (cancel + confirm), divided by vertical hairline
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    AlertTextButton(
                        label = secondaryAction,
                        onClick = onSecondaryAction,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .width(separatorThickness)
                            .fillMaxHeight()
                            .background(separatorColor)
                    )
                    AlertTextButton(
                        label = primaryAction,
                        onClick = onPrimaryAction,
                        modifier = Modifier.weight(1f),
                        emphasized = true
                    )
                }
            } else {
                // Single button, full width
                AlertTextButton(
                    label = primaryAction,
                    onClick = onPrimaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    emphasized = true
                )
            }
        }
    }
}

@Composable
private fun AlertTextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                lineHeight = 22.sp,
                letterSpacing = (-0.43).sp,
                fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

@Suppress("UnusedPrivateMember")
private val Transparent: Color = Color.Transparent // keep import stable

@Preview(showBackground = true)
@Composable
private fun HogwartsAlertSingleActionPreview() {
    HogwartsTheme {
        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            HogwartsAlert(
                title = "A Short Title Is Best",
                message = "A description should be a short, complete sentence.",
                primaryAction = "Action",
                onPrimaryAction = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HogwartsAlertTwoActionsPreview() {
    HogwartsTheme {
        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            HogwartsAlert(
                title = "Delete Item",
                message = "This cannot be undone.",
                primaryAction = "Delete",
                onPrimaryAction = {},
                secondaryAction = "Cancel",
                onSecondaryAction = {}
            )
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun HogwartsAlertRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                HogwartsAlert(
                    title = "تنبيه",
                    message = "هل أنت متأكد؟",
                    primaryAction = "موافق",
                    onPrimaryAction = {},
                    secondaryAction = "إلغاء",
                    onSecondaryAction = {}
                )
            }
        }
    }
}
