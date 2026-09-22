package org.hogwarts.android.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/** `coverColor` as a Compose colour, `#1a1a2e` when missing or malformed (the web's default). */
internal fun coverGround(hex: String?): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color(0xFF1A1A2E))

/**
 * `book-jacket.tsx`: the art wearing a jacket so it reads as a book — square
 * at the spine and round at the fore-edge, a crease down the binding and a
 * highlight off it, a hairline so a pale cover holds on a pale ground. The
 * spine is on the PHYSICAL left in both languages: artwork does not mirror.
 */
@Composable
internal fun BookJacket(
    coverUrl: String?,
    coverColor: String?,
    title: String,
    author: String,
    modifier: Modifier = Modifier,
    titleSize: TextUnit = 16.sp,
) {
    val shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 6.dp, bottomEnd = 6.dp)
    var failed by remember(coverUrl) { mutableStateOf(false) }
    Box(
        modifier
            .clip(shape.physicalLeft())
            .background(coverGround(coverColor))
            .drawWithContent {
                drawContent()
                val w = size.width
                drawRect(
                    Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Black.copy(alpha = 0.05f)), 0f, w * 0.03f),
                    Offset.Zero, Size(w * 0.03f, size.height),
                )
                drawRect(
                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.45f), Color.Transparent), w * 0.03f, w * 0.08f),
                    Offset(w * 0.03f, 0f), Size(w * 0.05f, size.height),
                )
            }
            .border(1.dp, Color.Black.copy(alpha = 0.10f), shape.physicalLeft()),
    ) {
        if (!coverUrl.isNullOrBlank() && !failed) {
            AsyncImage(
                model = coverUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                onError = { failed = true },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                Modifier.fillMaxSize().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            ) {
                Text(title, color = Color.White, fontSize = titleSize, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text(author, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

/** The jacket's corners stay on the physical sides under RTL — see [BookJacket]. */
private fun RoundedCornerShape.physicalLeft() = androidx.compose.foundation.shape.AbsoluteRoundedCornerShape(
    topLeft = 2.dp, bottomLeft = 2.dp, topRight = 6.dp, bottomRight = 6.dp,
)

