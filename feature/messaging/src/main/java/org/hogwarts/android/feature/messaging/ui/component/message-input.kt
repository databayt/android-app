package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.domain.model.Message

@Composable
fun MessageInput(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    replyTo: Message?,
    onCancelReply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val waColors = LocalWhatsAppColors.current
    val hasText = text.isNotBlank()

    Column(modifier = modifier.fillMaxWidth()) {
        // Reply preview strip
        if (replyTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(waColors.surfacePanel)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .background(waColors.surfaceProduct, RoundedCornerShape(2.dp))
                        .padding(vertical = 2.dp),
                )
                Column(
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                ) {
                    Text(
                        text = replyTo.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = waColors.textProduct,
                    )
                    Text(
                        text = replyTo.preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Cancel reply",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(waColors.surfaceSearchChat)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Attach button
            IconButton(
                onClick = { /* TODO: attachment picker */ },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    Icons.Filled.AttachFile,
                    contentDescription = "Attach",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Text field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(21.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.messaging_type_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp),
                    maxLines = 5,
                    cursorBrush = SolidColor(waColors.textProduct),
                )
            }

            // Send / Mic button
            IconButton(
                onClick = { if (hasText) onSend() },
                enabled = hasText && !isSending,
                modifier = Modifier
                    .size(40.dp)
                    .background(waColors.surfaceProduct, CircleShape),
            ) {
                Icon(
                    imageVector = if (hasText) Icons.AutoMirrored.Filled.Send else Icons.Filled.Mic,
                    contentDescription = if (hasText) "Send" else "Voice",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
