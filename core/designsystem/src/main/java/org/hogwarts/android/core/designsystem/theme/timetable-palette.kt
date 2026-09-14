package org.hogwarts.android.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The timetable grid's own inks — `timetable/views/simple-grid.tsx` paints the
 * grid with Tailwind's neutral ramp (not the semantic tokens) and colours each
 * class with `GRID_PALETTE` from `timetable/config.ts`. Tailwind v4 values in
 * sRGB; dark subject grounds keep the web's `/50` alpha over the grid surface.
 */
@Immutable
data class TimetablePalette(
    /** `bg-white` / `dark:bg-neutral-900` under the whole grid. */
    val surface: Color,
    /** Header row: `bg-neutral-50` / `dark:bg-neutral-800`. */
    val header: Color,
    /** The pinned period column: `bg-neutral-100` / `dark:bg-neutral-800`. */
    val periodCell: Color,
    /** A break row's time span: `bg-neutral-50` / `dark:bg-neutral-800/50`. */
    val breakSpan: Color,
    /** `EMPTY_CELL_STYLE`: `bg-neutral-50` / `dark:bg-neutral-800/30`. */
    val emptyCell: Color,
    /** `border-neutral-200` / `dark:border-neutral-700`. */
    val line: Color,
    /** Cell subject: `text-neutral-800` / `dark:text-neutral-100`. */
    val cellInk: Color,
    /** Period and day labels: `text-neutral-700` / `dark:text-neutral-300`. */
    val labelInk: Color,
    /** Cell second line: `text-neutral-600` / `dark:text-neutral-400`. */
    val secondaryInk: Color,
    /** Times, clock: `text-neutral-500` / `dark:text-neutral-400`. */
    val mutedInk: Color,
    /** The "-" of an empty cell: `text-neutral-400` / `dark:text-neutral-600`. */
    val dashInk: Color,
    /** `GRID_PALETTE` grounds in order: red, orange, yellow, green, blue. */
    val subjects: List<Color>,
) {
    companion object {
        val Light = TimetablePalette(
            surface = Color(0xFFFFFFFF),
            header = Color(0xFFFAFAFA),
            periodCell = Color(0xFFF5F5F5),
            breakSpan = Color(0xFFFAFAFA),
            emptyCell = Color(0xFFFAFAFA),
            line = Color(0xFFE5E5E5),
            cellInk = Color(0xFF262626),
            labelInk = Color(0xFF404040),
            secondaryInk = Color(0xFF525252),
            mutedInk = Color(0xFF737373),
            dashInk = Color(0xFFA1A1A1),
            subjects = listOf(
                Color(0xFFFFE2E2), // red-100
                Color(0xFFFFEDD4), // orange-100
                Color(0xFFFEF9C2), // yellow-100
                Color(0xFFDCFCE7), // green-100
                Color(0xFFDBEAFE), // blue-100
            ),
        )

        val Dark = TimetablePalette(
            surface = Color(0xFF171717),
            header = Color(0xFF262626),
            periodCell = Color(0xFF262626),
            breakSpan = Color(0x80262626),
            emptyCell = Color(0x4D262626),
            line = Color(0xFF404040),
            cellInk = Color(0xFFF5F5F5),
            labelInk = Color(0xFFD4D4D4),
            secondaryInk = Color(0xFFA1A1A1),
            mutedInk = Color(0xFFA1A1A1),
            dashInk = Color(0xFF525252),
            subjects = listOf(
                Color(0x8082181A), // red-900 / 50
                Color(0x807E2A0C), // orange-900 / 50
                Color(0x80733E0A), // yellow-900 / 50
                Color(0x800D542B), // green-900 / 50
                Color(0x801C398E), // blue-900 / 50
            ),
        )

        fun of(dark: Boolean): TimetablePalette = if (dark) Dark else Light
    }
}
