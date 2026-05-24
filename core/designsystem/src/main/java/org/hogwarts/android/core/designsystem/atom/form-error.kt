package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.FormErrorBackground
import org.hogwarts.android.core.designsystem.theme.FormErrorForeground
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Error message component matching web app design.
 *
 * Displays an error message with a warning icon on a
 * translucent red background (destructive/15).
 *
 * Web equivalent: /src/components/auth/form-error.tsx
 */
@Composable
fun FormError(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(FormErrorBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = FormErrorForeground,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            color = FormErrorForeground,
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FormErrorPreview() {
    HogwartsTheme {
        FormError(message = "Invalid email or password")
    }
}
