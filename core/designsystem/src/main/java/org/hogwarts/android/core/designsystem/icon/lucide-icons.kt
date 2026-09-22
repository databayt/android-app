package org.hogwarts.android.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Lucide glyphs the web's block pages draw, as Compose vectors.
 *
 * Copied verbatim from `lucide-react`'s icon nodes in the hogwarts install, at
 * the same 24x24 viewport and 2-unit round stroke, for the reason
 * [ToolbarIcons] gives: Material's nearest glyph is a different drawing, and
 * side by side the phone reads as another product. Elements lucide writes as
 * `rect` or `circle` are spelled out as the path that draws them, so every
 * glyph goes through one parser.
 */
object LucideIcons {

    /** lucide `history` — the catch-up shelf's heading. */
    val History: ImageVector by lazy {
        stroked(
            "History",
            "M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8",
            "M3 3v5h5",
            "M12 7v5l4 2",
        )
    }

    /** lucide `video` — the recordings heading and the timetable card. */
    val Video: ImageVector by lazy {
        stroked(
            "Video",
            "m16 13 5.223 3.482a.5.5 0 0 0 .777-.416V7.87a.5.5 0 0 0-.752-.432L16 10.5",
            // <rect x="2" y="6" width="14" height="12" rx="2" />
            "M4 6h10a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z",
        )
    }

    /** lucide `radio` — a class that is running. */
    val Radio: ImageVector by lazy {
        stroked(
            "Radio",
            "M16.247 7.761a6 6 0 0 1 0 8.478",
            "M19.075 4.933a10 10 0 0 1 0 14.134",
            "M4.925 19.067a10 10 0 0 1 0-14.134",
            "M7.753 16.239a6 6 0 0 1 0-8.478",
            // <circle cx="12" cy="12" r="2" />
            "M12 10a2 2 0 1 0 0 4a2 2 0 1 0 0-4",
        )
    }

    /** lucide `table-2` — "all sessions". */
    val Table2: ImageVector by lazy {
        stroked(
            "Table2",
            "M9 3H5a2 2 0 0 0-2 2v4m6-6h10a2 2 0 0 1 2 2v4M9 3v18m0 0h10a2 2 0 0 0 " +
                "2-2V9M9 21H5a2 2 0 0 1-2-2V9m0 0h18",
        )
    }

    /** lucide `calendar-plus` — "schedule a class". */
    val CalendarPlus: ImageVector by lazy {
        stroked(
            "CalendarPlus",
            "M16 19h6",
            "M16 2v4",
            "M19 16v6",
            "M21 12.598V6a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h8.5",
            "M3 10h18",
            "M8 2v4",
        )
    }

    /** lucide `calendar-clock` — the empty strip. */
    val CalendarClock: ImageVector by lazy {
        stroked(
            "CalendarClock",
            "M16 14v2.2l1.6 1",
            "M16 2v4",
            "M21 7.5V6a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h3.5",
            "M3 10h5",
            "M8 2v4",
            // <circle cx="16" cy="16" r="6" />
            "M16 10a6 6 0 1 0 0 12a6 6 0 1 0 0-12",
        )
    }

    /** lucide `square-play` — "recordings". */
    val SquarePlay: ImageVector by lazy {
        stroked(
            "SquarePlay",
            // <rect x="3" y="3" width="18" height="18" rx="2" />
            "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z",
            "M9 9.003a1 1 0 0 1 1.517-.859l4.997 2.997a1 1 0 0 1 0 1.718l-4.997 " +
                "2.997A1 1 0 0 1 9 14.996z",
        )
    }

    /** lucide `clipboard-check` — a guardian's timetable card. */
    val ClipboardCheck: ImageVector by lazy {
        stroked(
            "ClipboardCheck",
            // <rect width="8" height="4" x="8" y="2" rx="1" ry="1" />
            "M9 2h6a1 1 0 0 1 1 1v2a1 1 0 0 1-1 1H9a1 1 0 0 1-1-1V3a1 1 0 0 1 1-1z",
            "M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2",
            "m9 14 2 2 4-4",
        )
    }

    /** lucide `settings`. */
    val Settings: ImageVector by lazy {
        stroked(
            "Settings",
            "M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 " +
                "0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 " +
                "1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 " +
                "2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 " +
                "2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 " +
                "2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 " +
                "2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15" +
                ".08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z",
            // <circle cx="12" cy="12" r="3" />
            "M12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6",
        )
    }

    /** lucide `activity` — "network test". */
    val Activity: ImageVector by lazy {
        stroked(
            "Activity",
            "M22 12h-2.48a2 2 0 0 0-1.93 1.46l-2.35 8.36a.25.25 0 0 1-.48 0L9.24 " +
                "2.18a.25.25 0 0 0-.48 0l-2.35 8.36A2 2 0 0 1 4.49 12H2",
        )
    }

    /**
     * lucide `play`, FILLED — the web draws it `fill-current` on the
     * recording badge, so it is the one glyph here that is a solid shape.
     */
    val PlayFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "PlayFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString(
                    "M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z",
                ).toNodes(),
                fill = SolidColor(Color.Black),
            )
        }.build()
    }
}
