package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.R
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Social auth button component matching web app design.
 *
 * Outline button with icon and label for OAuth providers.
 * Used for Google, Facebook, etc. login buttons.
 *
 * Web equivalent: Buttons in /src/components/auth/social.tsx
 */
@Composable
fun SocialButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.Unspecified,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Row of social auth buttons (Google and Facebook).
 *
 * Two-column grid layout matching the web app design.
 */
@Composable
fun SocialAuthButtons(
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SocialButton(
            text = "Google",
            icon = painterResource(id = R.drawable.ic_google),
            onClick = onGoogleClick,
            modifier = Modifier.weight(1f),
            enabled = enabled
        )
        SocialButton(
            text = "Facebook",
            icon = painterResource(id = R.drawable.ic_facebook),
            onClick = onFacebookClick,
            modifier = Modifier.weight(1f),
            iconTint = Color(0xFF1877F2),
            enabled = enabled
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SocialAuthButtonsPreview() {
    HogwartsTheme {
        SocialAuthButtons(
            onGoogleClick = {},
            onFacebookClick = {}
        )
    }
}
