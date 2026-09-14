package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/** The web Input's height on the auth forms: `h-10`. */
val InputHeight: Dp = 40.dp

/**
 * shadcn's `ui/input.tsx` as the web phone renders it:
 * `h-10 rounded-md border border-input bg-transparent px-3 text-base`,
 * `focus:border-foreground`, `aria-invalid:border-destructive`,
 * `dark:bg-input/30`, placeholder in muted-foreground, disabled at 50%.
 *
 * [trailing] sits inside the border at the end (the password eye).
 */
@Composable
fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = HogwartsTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val borderColor = when {
        isError -> colors.destructive
        focused -> colors.foreground
        else -> colors.input
    }
    // `dark:bg-input/30` — the input token at 30% of its own alpha.
    val ground = if (colors.isDark) colors.input.copy(alpha = colors.input.alpha * 0.3f) else Color.Transparent
    val textStyle = HogwartsTheme.type.body.copy(fontSize = 16.sp, lineHeight = 24.sp, color = colors.foreground)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(InputHeight)
            .alpha(if (enabled) 1f else 0.5f),
        enabled = enabled,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.foreground),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interaction,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(InputHeight)
                    .clip(HogwartsShapes.Md)
                    .background(ground)
                    .border(1.dp, borderColor, HogwartsShapes.Md)
                    .padding(start = 12.dp, end = if (trailing != null) 0.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(placeholder, style = textStyle, color = colors.mutedForeground, maxLines = 1)
                    }
                    innerTextField()
                }
                if (trailing != null) trailing()
            }
        },
    )
}

/**
 * A password [Input] with the show/hide eye inside its end edge. The two
 * labels are the eye's accessible names, supplied by the feature's strings.
 */
@Composable
fun PasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    showLabel: String,
    hideLabel: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Input(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
        isError = isError,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailing = {
            val label = if (visible) hideLabel else showLabel
            IconButton(
                onClick = onToggleVisible,
                enabled = enabled,
                modifier = Modifier
                    .size(InputHeight)
                    .semantics { contentDescription = label },
            ) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = HogwartsTheme.colors.mutedForeground,
                    modifier = Modifier.size(16.dp),
                )
            }
        },
    )
}
