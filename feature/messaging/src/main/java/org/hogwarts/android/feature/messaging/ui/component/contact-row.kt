package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.domain.model.Contact
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class RoleColor(val bg: Color, val fg: Color)

private val roleColors = mapOf(
    "ADMIN" to RoleColor(Color(0xFFFECACA), Color(0xFFB91C1C)),
    "TEACHER" to RoleColor(Color(0xFFBFDBFE), Color(0xFF1D4ED8)),
    "STUDENT" to RoleColor(Color(0xFFBBF7D0), Color(0xFF15803D)),
    "GUARDIAN" to RoleColor(Color(0xFFE9D5FF), Color(0xFF6D28D9)),
    "STAFF" to RoleColor(Color(0xFFFED7AA), Color(0xFFC2410C)),
    "ACCOUNTANT" to RoleColor(Color(0xFFFDE68A), Color(0xFF92400E)),
)

@Composable
fun ContactRow(
    contact: Contact,
    isOnline: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val waColors = LocalWhatsAppColors.current
    val hasConversation = contact.conversationId != null
    val hasUnread = contact.unreadCount > 0
    val background = if (isActive) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box {
            UserAvatar(
                name = contact.displayName,
                imageUrl = contact.avatarUrl,
                size = 49.dp,
            )
            OnlineIndicator(
                isOnline = isOnline,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (hasConversation && contact.lastMessageAt != null) {
                    val yesterdayLabel = stringResource(R.string.messaging_ui_yesterday)
                    Text(
                        text = remember(contact.lastMessageAt, yesterdayLabel) {
                            formatContactTime(contact.lastMessageAt, yesterdayLabel)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasUnread) waColors.surfaceProduct else waColors.textSecondary,
                        fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                    )
                } else {
                    RoleBadge(role = contact.role)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                val typingText = stringResource(R.string.messaging_ui_is_typing)
                val secondaryText = when {
                    contact.isTyping -> typingText
                    hasConversation -> contact.lastMessage
                        ?: contact.contextLabel
                        ?: ""
                    else -> contact.contextLabel
                        ?: contact.role.lowercase(Locale.getDefault())
                            .replaceFirstChar { it.uppercase(Locale.getDefault()) }
                }
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (contact.isTyping) waColors.textProduct
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (contact.isTyping) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (contact.isPinned) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (contact.isMuted) MaterialTheme.colorScheme.onSurfaceVariant
                                else waColors.surfaceProduct,
                                CircleShape,
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (contact.unreadCount > 99) "99+" else contact.unreadCount.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    val fallback = RoleColor(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val color = roleColors[role.uppercase()] ?: fallback
    Box(
        modifier = Modifier
            .background(color.bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = role.lowercase().replaceFirstChar { it.uppercase() },
            fontSize = 10.sp,
            color = color.fg,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun formatContactTime(instant: Instant, yesterdayLabel: String): String {
    val locale = Locale.getDefault()
    val zone = ZoneId.systemDefault()
    val date = instant.atZone(zone).toLocalDate()
    val today = LocalDate.now()
    return when {
        date == today -> DateTimeFormatter.ofPattern("HH:mm", locale).withZone(zone).format(instant)
        date == today.minusDays(1) -> yesterdayLabel
        Duration.between(instant, Instant.now()).toDays() < 7 ->
            DateTimeFormatter.ofPattern("EEE", locale).withZone(zone).format(instant)
        else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", locale))
    }
}
