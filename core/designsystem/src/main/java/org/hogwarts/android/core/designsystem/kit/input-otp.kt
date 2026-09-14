package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * shadcn's `ui/input-otp.tsx` as the verify step styles it
 * (`h-14 w-14 rounded-md border text-2xl`, one group per slot, the active slot
 * ringed). Six 56dp slots do not fit a 390dp phone inside the auth gutter, so
 * slots default to 48dp with 8dp gaps.
 *
 * Digits only; the row is always laid out left-to-right, as codes are read.
 * [onComplete] fires when the last digit lands.
 */
@Composable
fun InputOtp(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    enabled: Boolean = true,
    isError: Boolean = false,
    slotSize: Dp = 48.dp,
    onComplete: (String) -> Unit = {},
) {
    val colors = HogwartsTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val digitStyle = HogwartsTheme.type.figureWide.copy(fontWeight = FontWeight.Normal, fontSize = 24.sp, color = colors.foreground)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                val digits = raw.filter(Char::isDigit).take(length)
                if (digits != value) {
                    onValueChange(digits)
                    if (digits.length == length) onComplete(digits)
                }
            },
            modifier = modifier.alpha(if (enabled) 1f else 0.5f),
            enabled = enabled,
            singleLine = true,
            textStyle = digitStyle.copy(color = Color.Transparent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            interactionSource = interaction,
            decorationBox = { innerTextField ->
                Box {
                    // The real field stays in the tree (focus, IME, a11y) but draws nothing.
                    Box(Modifier.size(1.dp).alpha(0f)) { innerTextField() }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(length) { index ->
                            val char = value.getOrNull(index)
                            val active = focused && index == value.length.coerceAtMost(length - 1)
                            val border = when {
                                isError -> colors.destructive
                                active -> colors.ring
                                else -> colors.input
                            }
                            Box(
                                modifier = Modifier
                                    .size(slotSize)
                                    .background(Color.Transparent, HogwartsShapes.Md)
                                    .border(if (active) 2.dp else 1.dp, border, HogwartsShapes.Md),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (char != null) {
                                    Text(char.toString(), style = digitStyle)
                                } else if (active) {
                                    Box(Modifier.width(1.dp).height(16.dp).background(colors.foreground))
                                }
                            }
                        }
                    }
                }
            },
        )
    }
}
