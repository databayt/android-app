package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsError
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Hogwarts button with 6 variants matching iOS + web.
 *
 * Source: Figma iOS 26 — Action Sheet (node 1:58)
 * Parity: swift-app/hogwarts/shared/atom/hw-button.swift
 */
enum class HWButtonVariant {
    Default,     // Blue filled
    Secondary,   // Gray filled
    Outline,     // Bordered, no fill
    Ghost,       // No background
    Destructive, // Red filled
    Link         // Text only, blue
}

@Composable
fun HogwartsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HWButtonVariant = HWButtonVariant.Default,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isFullWidth: Boolean = true
) {
    val buttonModifier = if (isFullWidth) {
        modifier.fillMaxWidth().height(44.dp)
    } else {
        modifier.height(44.dp)
    }
    val shape = RoundedCornerShape(12.dp)

    when (variant) {
        HWButtonVariant.Default -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ButtonContent(text = text, isLoading = isLoading, color = MaterialTheme.colorScheme.onPrimary)
        }

        HWButtonVariant.Secondary -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ButtonContent(text = text, isLoading = isLoading, color = MaterialTheme.colorScheme.onSurface)
        }

        HWButtonVariant.Outline -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            shape = shape,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ButtonContent(text = text, isLoading = isLoading, color = MaterialTheme.colorScheme.primary)
        }

        HWButtonVariant.Ghost -> TextButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            shape = shape,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ButtonContent(text = text, isLoading = isLoading, color = MaterialTheme.colorScheme.primary)
        }

        HWButtonVariant.Destructive -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = HogwartsError,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ButtonContent(text = text, isLoading = isLoading, color = Color.White)
        }

        HWButtonVariant.Link -> TextButton(
            onClick = onClick,
            modifier = if (isFullWidth) modifier.fillMaxWidth() else modifier,
            enabled = enabled && !isLoading,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ButtonContent(text = text, isLoading = isLoading, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ButtonContent(text: String, isLoading: Boolean, color: Color) {
    Box(contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = color,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HogwartsButtonVariantsPreview() {
    HogwartsTheme {
        Column {
            HogwartsButton(text = "Default", onClick = {})
            Spacer(Modifier.height(8.dp))
            HogwartsButton(text = "Secondary", onClick = {}, variant = HWButtonVariant.Secondary)
            Spacer(Modifier.height(8.dp))
            HogwartsButton(text = "Outline", onClick = {}, variant = HWButtonVariant.Outline)
            Spacer(Modifier.height(8.dp))
            HogwartsButton(text = "Ghost", onClick = {}, variant = HWButtonVariant.Ghost)
            Spacer(Modifier.height(8.dp))
            HogwartsButton(text = "Destructive", onClick = {}, variant = HWButtonVariant.Destructive)
            Spacer(Modifier.height(8.dp))
            HogwartsButton(text = "Link", onClick = {}, variant = HWButtonVariant.Link)
            Spacer(Modifier.height(8.dp))
            HogwartsButton(text = "Loading...", onClick = {}, isLoading = true)
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun HogwartsButtonRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column {
                HogwartsButton(text = "تسجيل الدخول", onClick = {})
                Spacer(Modifier.height(8.dp))
                HogwartsButton(text = "ثانوي", onClick = {}, variant = HWButtonVariant.Secondary)
                Spacer(Modifier.height(8.dp))
                HogwartsButton(text = "إلغاء", onClick = {}, variant = HWButtonVariant.Destructive)
                Spacer(Modifier.height(8.dp))
                HogwartsButton(text = "رابط", onClick = {}, variant = HWButtonVariant.Link)
            }
        }
    }
}
