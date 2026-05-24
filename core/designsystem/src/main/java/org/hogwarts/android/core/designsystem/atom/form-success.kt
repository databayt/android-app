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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.FormSuccessBackground
import org.hogwarts.android.core.designsystem.theme.FormSuccessForeground
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Success message component matching web app design.
 *
 * Displays a success message with a checkmark icon on a
 * translucent emerald background (emerald-500/15).
 *
 * Web equivalent: /src/components/auth/form-success.tsx
 */
@Composable
fun FormSuccess(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(FormSuccessBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = FormSuccessForeground,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            color = FormSuccessForeground,
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FormSuccessPreview() {
    HogwartsTheme {
        FormSuccess(message = "Login successful!")
    }
}
