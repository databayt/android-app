package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

enum class FormAlertTone { Error, Success }

/**
 * The auth forms' result line — mirrors `auth/error/form-error.tsx`
 * (`bg-destructive/15 text-destructive rounded-md p-3 text-sm`, triangle) and
 * `auth/form-success.tsx` (`bg-emerald-500/15 text-emerald-500`, check).
 * Announced politely to screen readers when it appears.
 */
@Composable
fun FormAlert(
    message: String,
    modifier: Modifier = Modifier,
    tone: FormAlertTone = FormAlertTone.Error,
) {
    val ink = when (tone) {
        FormAlertTone.Error -> HogwartsTheme.colors.destructive
        FormAlertTone.Success -> BrandColors.Present
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ink.copy(alpha = 0.15f), HogwartsShapes.Md)
            .padding(12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = if (tone == FormAlertTone.Error) Icons.Outlined.WarningAmber else Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = ink,
            modifier = Modifier.size(16.dp),
        )
        Text(message, style = HogwartsTheme.type.body, color = ink)
    }
}

/** A field's own message under it — shadcn `FormMessage`: `text-destructive text-sm`. */
@Composable
fun FieldMessage(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = HogwartsTheme.type.body,
        color = HogwartsTheme.colors.destructive,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}
