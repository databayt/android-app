package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

enum class FormButtonVariant { Default, Outline, Ghost }

/**
 * shadcn's `ui/button.tsx` as the auth forms use it: a full-width
 * `h-11 rounded-md` bar, `text-sm font-medium`. Unlike [PillButton] (the phone
 * kit's side-by-side action) this is the one primary action closing a form.
 *
 * [loading] keeps the label, adds a spinner and disables the press, like the
 * web's `disabled={isPending}`.
 */
@Composable
fun FormButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FormButtonVariant = FormButtonVariant.Default,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 44.dp,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = HogwartsTheme.colors
    val (bg, fg) = when (variant) {
        FormButtonVariant.Default -> colors.primary to colors.primaryForeground
        FormButtonVariant.Outline ->
            (if (colors.isDark) colors.input.copy(alpha = colors.input.alpha * 0.3f) else colors.background) to colors.foreground
        FormButtonVariant.Ghost -> Color.Transparent to colors.foreground
    }
    val active = enabled && !loading
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            // Before the ground, so a disabled button fades as a whole (`disabled:opacity-50`).
            .alpha(if (active) 1f else 0.5f)
            .clip(HogwartsShapes.Md)
            .background(bg)
            .then(
                if (variant == FormButtonVariant.Outline) {
                    Modifier.border(1.dp, if (colors.isDark) colors.input else colors.border, HogwartsShapes.Md)
                } else {
                    Modifier
                },
            )
            .clickable(enabled = active, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        if (loading) {
            CircularProgressIndicator(color = fg, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
        } else if (leading != null) {
            leading()
        }
        Text(label, style = HogwartsTheme.type.bodyMedium, color = fg, maxLines = 1)
    }
}

/**
 * The forms' quiet links — `muted underline-offset-4`: "Forgot password?",
 * "Don't have an account?", "Back to login".
 */
@Composable
fun TextLink(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textAlign: TextAlign? = null,
) {
    Text(
        text = label,
        style = HogwartsTheme.type.body,
        color = HogwartsTheme.colors.mutedForeground,
        textAlign = textAlign,
        modifier = modifier
            .clip(HogwartsShapes.Sm)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f)
            .padding(vertical = 4.dp),
    )
}
