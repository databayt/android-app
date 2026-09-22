package org.hogwarts.android.feature.library.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** The lucide glyphs the book page draws (chevron-right, book-open, info, star, check), from `lucide-react`. */
internal object LibraryIcons {
    val ChevronRight by lazy { icon("ChevronRight", listOf("m9 18 6-6-6-6")) }
    val BookOpen by lazy {
        icon(
            "BookOpen",
            listOf(
                "M12 7v14",
                "M3 18a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h5a4 4 0 0 1 4 4 4 4 0 0 1 4-4h5a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1h-6a3 3 0 0 0-3 3 3 3 0 0 0-3-3z",
            ),
        )
    }
    val Info by lazy { icon("Info", listOf("M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0-20", "M12 16v-4", "M12 8h.01")) }
    private const val STAR =
        "M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z"
    val StarFilled by lazy { icon("StarFilled", listOf(STAR), fill = true) }
    val Check by lazy { icon("Check", listOf("M20 6 9 17l-5-5")) }

    private fun icon(name: String, paths: List<String>, fill: Boolean = false): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach {
                addPath(
                    pathData = PathParser().parsePathString(it).toNodes(),
                    fill = if (fill) SolidColor(Color.Black) else null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
}
