package org.hogwarts.android.core.designsystem

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.unit.LayoutDirection
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

fun ComposeContentTestRule.setContentRtl(
    content: @androidx.compose.runtime.Composable () -> Unit
) {
    setContent {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            HogwartsTheme {
                content()
            }
        }
    }
}

fun ComposeContentTestRule.setContentLtr(
    content: @androidx.compose.runtime.Composable () -> Unit
) {
    setContent {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            HogwartsTheme {
                content()
            }
        }
    }
}
