package org.hogwarts.android.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * The glyphs the web's phone menu toolbar draws, as Compose vectors.
 *
 * Material's own set is close but not the same drawing: Material's language
 * control is a globe where the web's is lucide `Languages` (the 文/A pair),
 * and its theme control is a sun or a moon where the web's is a circle cut by
 * diagonals. Side by side that reads as two products, so the path data here is
 * copied verbatim out of the live `demo.balqalam.com` DOM — the same `d`
 * strings the browser is handed, at the same 24x24 viewport, 2-unit round
 * stroke.
 *
 * Only `mail` and `search` carry a shape the SVG writes as an element rather
 * than a path; both are spelled out here as the path that draws them, so
 * every glyph goes through one parser.
 *
 * Stroked, not filled, and drawn in black: `Icon` tints the whole rendering,
 * so the colour comes from the call site.
 */
object ToolbarIcons {

    /** lucide `search`. */
    val Search: ImageVector by lazy {
        stroked(
            "Search",
            "m21 21-4.34-4.34",
            // <circle cx="11" cy="11" r="8" />
            "M11 3a8 8 0 1 0 0 16a8 8 0 1 0 0-16",
        )
    }

    /** lucide `languages` — the web's language toggle. */
    val Languages: ImageVector by lazy {
        stroked(
            "Languages",
            "m5 8 6 6",
            "m4 14 6-6 2-3",
            "M2 5h12",
            "M7 2h1",
            "m22 22-5-10-5 10",
            "M14 18h6",
        )
    }

    /**
     * The theme toggle: a circle cut by a diameter and three diagonals, so the
     * disc reads half-lit. Inline in `mode-switcher.tsx` rather than imported
     * from lucide, and it does not change with the theme — the web draws the
     * same glyph in light and dark.
     */
    val Contrast: ImageVector by lazy {
        stroked(
            "Contrast",
            "M12 12m-9 0a9 9 0 1 0 18 0a9 9 0 1 0 -18 0",
            "M12 3l0 18",
            "M12 9l4.65 -4.65",
            "M12 14.3l7.37 -7.37",
            "M12 19.6l8.85 -8.85",
        )
    }

    /** lucide `bell`. */
    val Bell: ImageVector by lazy {
        stroked(
            "Bell",
            "M10.268 21a2 2 0 0 0 3.464 0",
            "M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 " +
                "12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326",
        )
    }

    /** lucide `mail`. */
    val Mail: ImageVector by lazy {
        stroked(
            "Mail",
            "m22 7-8.991 5.727a2 2 0 0 1-2.009 0L2 7",
            // <rect x="2" y="4" width="20" height="16" rx="2" />
            "M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z",
        )
    }
}

private fun stroked(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        paths.forEach { data ->
            addPath(
                pathData = PathParser().parsePathString(data).toNodes(),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()
