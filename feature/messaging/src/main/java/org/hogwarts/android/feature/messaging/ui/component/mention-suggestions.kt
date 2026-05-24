package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.feature.messaging.domain.model.Contact

/** Detect `@query` at the end of the text.
 *  Returns the query (without @) if the cursor is mid-mention, else null. */
fun activeMentionQuery(text: String): String? {
    val atIdx = text.lastIndexOf('@')
    if (atIdx < 0) return null
    val prior = if (atIdx == 0) ' ' else text[atIdx - 1]
    if (!prior.isWhitespace() && atIdx != 0) return null
    val after = text.substring(atIdx + 1)
    if (after.contains(' ')) return null
    return after
}

/** Replace the current `@query` fragment with `@displayName ` in the input text. */
fun applyMention(text: String, displayName: String): String {
    val atIdx = text.lastIndexOf('@')
    if (atIdx < 0) return "$text@$displayName "
    return text.substring(0, atIdx) + "@" + displayName + " "
}

@Composable
fun MentionSuggestions(
    suggestions: List<Contact>,
    onSelect: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (suggestions.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            ),
    ) {
        LazyColumn(modifier = Modifier.height((suggestions.size * 56).coerceAtMost(224).dp)) {
            items(suggestions, key = { it.id }) { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(contact) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UserAvatar(
                        name = contact.displayName,
                        imageUrl = contact.avatarUrl,
                        size = 32.dp,
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = contact.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = contact.role.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
