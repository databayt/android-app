package org.hogwarts.android.feature.subjects.textbook.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * The reader's glyphs: lucide's (x, list, search, share, bookmark, mic, sun,
 * moon, align-justify, check) copied from `lucide-react`, and the three the
 * web draws itself in `sheets.tsx` — the menu, rotation lock and line guide.
 */
internal object ReaderIcons {
    val X by lazy { stroked("X", 2f, "M18 6 6 18", "m6 6 12 12") }
    val XThin by lazy { stroked("XThin", 1.5f, "M18 6 6 18", "m6 6 12 12") }
    val XBold by lazy { stroked("XBold", 2.2f, "M18 6 6 18", "m6 6 12 12") }
    val List by lazy { stroked("List", 2f, "M3 12h.01", "M3 18h.01", "M3 6h.01", "M8 12h13", "M8 18h13", "M8 6h13") }
    val Search by lazy { stroked("Search", 2f, "m21 21-4.34-4.34", "M11 3a8 8 0 1 0 0 16a8 8 0 1 0 0-16") }
    val SearchBold by lazy { stroked("SearchBold", 2.25f, "m21 21-4.34-4.34", "M11 3a8 8 0 1 0 0 16a8 8 0 1 0 0-16") }
    val Share by lazy { stroked("Share", 2f, "M12 2v13", "m16 6-4-4-4 4", "M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8") }
    private const val BOOKMARK = "m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z"
    val Bookmark by lazy { stroked("Bookmark", 2f, BOOKMARK) }
    val BookmarkFilled by lazy {
        build("BookmarkFilled") {
            path(BOOKMARK, fill = true)
            path(BOOKMARK, stroke = 2f)
        }
    }
    val Mic by lazy {
        stroked("Mic", 2f, "M12 19v3", "M19 10v2a7 7 0 0 1-14 0v-2", "M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z")
    }
    val Sun by lazy {
        stroked(
            "Sun", 2f, "M12 8a4 4 0 1 0 0 8a4 4 0 1 0 0-8", "M12 2v2", "M12 20v2", "m4.93 4.93 1.41 1.41",
            "m17.66 17.66 1.41 1.41", "M2 12h2", "M20 12h2", "m6.34 17.66-1.41 1.41", "m19.07 4.93-1.41 1.41",
        )
    }
    val Moon by lazy { stroked("Moon", 2f, "M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z") }
    val AlignJustify by lazy { stroked("AlignJustify", 2f, "M3 12h18", "M3 18h18", "M3 6h18") }
    val Check by lazy { stroked("Check", 2f, "M20 6 9 17l-5-5") }
    val FileText by lazy {
        stroked(
            "FileText", 2f, "M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z", "M14 2v4a2 2 0 0 0 2 2h4",
            "M10 9H8", "M16 13H8", "M16 17H8",
        )
    }

    /** The reading-menu glyph: two rules over three dots. */
    val Menu by lazy {
        build("Menu") {
            path("M4 7h16M4 12h16", stroke = 2f)
            path(circle(6f, 17.5f, 1.3f), fill = true)
            path(circle(12f, 17.5f, 1.3f), fill = true)
            path(circle(18f, 17.5f, 1.3f), fill = true)
        }
    }

    /** Rotation lock: a turning arrow round a padlock, its shackle open until the screen is held. */
    fun rotationLock(locked: Boolean) = build("RotationLock$locked") {
        path("M16.68 4.79A8.6 8.6 0 1 0 18.08 18.08", stroke = 1.7f)
        path("M16.5 7.2h5.3L19.15 12.8z", fill = true)
        path(roundRect(8.65f, 10.9f, 6.7f, 3.8f, 1.1f), fill = true)
        path(
            if (locked) "M10.5 10.9V9.7a1.75 1.75 0 0 1 3.5 0v1.2" else "M10.5 10.9V9.7a1.75 1.75 0 0 1 3.5 0",
            stroke = 1.5f,
        )
    }

    /** Line guide: stacked rules with the one being read held in its capsule. */
    val LineGuide by lazy {
        build("LineGuide") {
            path(roundRect(5f, 6.1f, 14f, 1.9f, 0.95f), fill = true)
            path(roundRect(3.5f, 9.4f, 17f, 4.7f, 2.35f), stroke = 1.3f)
            path(roundRect(5.6f, 10.9f, 12.8f, 1.7f, 0.85f), fill = true)
            path(roundRect(5f, 15.6f, 14f, 1.9f, 0.95f), fill = true)
            path(roundRect(5f, 18.6f, 14f, 1.9f, 0.95f), fill = true)
        }
    }

    private fun circle(cx: Float, cy: Float, r: Float) =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0z"

    private fun roundRect(x: Float, y: Float, w: Float, h: Float, r: Float) =
        "M${x + r} ${y}h${w - 2 * r}a$r $r 0 0 1 $r ${r}v${h - 2 * r}a$r $r 0 0 1 ${-r} ${r}" +
            "h${-(w - 2 * r)}a$r $r 0 0 1 ${-r} ${-r}v${-(h - 2 * r)}a$r $r 0 0 1 $r ${-r}z"

    private class Builder(val b: ImageVector.Builder) {
        fun path(data: String, fill: Boolean = false, stroke: Float = 0f) {
            b.addPath(
                pathData = PathParser().parsePathString(data).toNodes(),
                fill = if (fill) SolidColor(Color.Black) else null,
                stroke = if (stroke > 0f) SolidColor(Color.Black) else null,
                strokeLineWidth = stroke,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }

    private fun build(name: String, block: Builder.() -> Unit): ImageVector {
        val b = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        Builder(b).block()
        return b.build()
    }

    private fun stroked(name: String, width: Float, vararg paths: String) = build(name) {
        paths.forEach { path(it, stroke = width) }
    }
}
