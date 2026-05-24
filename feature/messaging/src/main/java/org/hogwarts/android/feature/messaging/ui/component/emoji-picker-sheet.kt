package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Minimal hardcoded emoji set — covers the common reactions
 *  most chats use. Full picker can arrive in Phase 4 polish. */
private val COMMON_EMOJIS = listOf(
    "👍", "❤️", "😂", "😮", "😢", "🙏",
    "🎉", "🔥", "👏", "💯", "✅", "😊",
    "😍", "🤔", "😎", "🤷", "👎", "💔",
    "🙌", "💪", "✨", "⭐", "💡", "📌",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerSheet(
    title: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.fillMaxWidth().height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(4.dp),
            ) {
                items(COMMON_EMOJIS) { emoji ->
                    Box(
                        modifier = Modifier
                            .clickable { onPick(emoji) }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = emoji, fontSize = 28.sp)
                    }
                }
            }
        }
    }
}
