package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.apple.AppleMaterial
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.liquidGlassCard
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Liquid Glass variants from iOS 26 design system.
 *
 * Figma nodes (WJPT23xMx4B6oXrCavmHbQ):
 *  - RegularLarge  (16:3092) — 160×160, radius 34, Regular material (≈rgba(250,250,250,0.7))
 *  - RegularMedium (16:3096) — 160×160, radius 34, slightly thinner tint (≈rgba(245,245,245,0.6))
 *  - RegularSmall  (16:3099) —  48×48,  radius 296 (pill), rgba(255,255,255,0.65)
 *  - Clear         (16:3105) — 160×160, radius 24, ultra-thin rgba(255,255,255,0.07)
 *
 * Shadow across variants: 0dp x / 8dp y / 40dp blur @ rgba(0,0,0,0.12); Clear uses alpha 0.2.
 */
enum class GlassVariant(
    val cornerRadius: Dp,
    val material: AppleMaterial
) {
    RegularLarge(cornerRadius = 34.dp, material = AppleMaterial.Regular),
    RegularMedium(cornerRadius = 34.dp, material = AppleMaterial.Thin),
    RegularSmall(cornerRadius = 296.dp, material = AppleMaterial.Thin),
    Clear(cornerRadius = 24.dp, material = AppleMaterial.UltraThin)
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.RegularMedium,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .liquidGlassCard(cornerRadius = variant.cornerRadius, material = variant.material)
            .padding(AppleSpacing.Standard),
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    material: AppleMaterial,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .liquidGlassCard(material = material)
            .padding(AppleSpacing.Standard),
        content = content
    )
}

/**
 * Pill-shaped Liquid Glass button.
 * Figma node 1:556 / I17:3252;5583:24442 — Button - Liquid Glass - Text.
 * Outer: h=52, radius=1000, full-round, padding horizontal=20 vertical=6, gap=4.
 * Inner text block: h=36, centered. Text: 17sp Medium (weight 510), white on tinted blue fill.
 *
 * HIG: https://developer.apple.com/design/human-interface-guidelines/buttons
 */
@Composable
fun LiquidGlassButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .liquidGlassCard(cornerRadius = 1000.dp, material = AppleMaterial.Thin)
            .clickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(PaddingValues(horizontal = 20.dp, vertical = 6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.height(36.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            trailingIcon?.invoke()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LiquidGlassButtonPreview() {
    HogwartsTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LiquidGlassButton(
                    label = "Collaborate",
                    onClick = {}
                )
                LiquidGlassButton(
                    label = "Label",
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun LiquidGlassButtonRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface {
                LiquidGlassButton(
                    label = "تعاون",
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun GlassCardRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            GlassCard {
                Column {
                    Text(
                        text = "بطاقة الطالب",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "معلومات القبول والتسجيل",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
