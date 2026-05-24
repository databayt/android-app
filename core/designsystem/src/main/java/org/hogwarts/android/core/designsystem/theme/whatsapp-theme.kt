package org.hogwarts.android.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun WhatsAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) WhatsAppColors.dark else WhatsAppColors.light
    CompositionLocalProvider(LocalWhatsAppColors provides colors, content = content)
}
