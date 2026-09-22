package org.hogwarts.android.feature.live.ui.session

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** lucide glyphs for the session page and the room, from `lucide-react`. */
internal object LiveIcons {
    val Film by lazy {
        icon("Film", "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z", "M7 3v18", "M3 7.5h4", "M3 12h18", "M3 16.5h4", "M17 3v18", "M17 7.5h4", "M17 16.5h4")
    }
    val Mic by lazy { icon("Mic", "M12 19v3", "M19 10v2a7 7 0 0 1-14 0v-2", "M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z") }
    val MicOff by lazy {
        icon("MicOff", "m2 2 20 20", "M18.89 13.23A7.12 7.12 0 0 0 19 12v-2", "M5 10v2a7 7 0 0 0 12 5", "M15 9.34V5a3 3 0 0 0-5.68-1.33", "M9 9v3a3 3 0 0 0 5.12 2.12", "M12 19v3")
    }
    val Video by lazy {
        icon("Video", "m16 13 5.223 3.482a.5.5 0 0 0 .777-.416V7.87a.5.5 0 0 0-.752-.432L16 10.5", "M4 6h10a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z")
    }
    val VideoOff by lazy {
        icon("VideoOff", "M10.66 6H14a2 2 0 0 1 2 2v2.5l5.248-3.062A.5.5 0 0 1 22 7.87v8.196", "M16 16a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h2", "m2 2 20 20")
    }
    val Users by lazy {
        icon("Users", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2", "M16 3.128a4 4 0 0 1 0 7.744", "M22 21v-2a4 4 0 0 0-3-3.87", "M9 3a4 4 0 1 0 0 8a4 4 0 1 0 0-8")
    }
    val X by lazy { icon("X", "M18 6 6 18", "m6 6 12 12") }

    private fun icon(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach {
                addPath(
                    pathData = PathParser().parsePathString(it).toNodes(),
                    stroke = SolidColor(Color.Black), strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
}
