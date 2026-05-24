package org.hogwarts.android.core.designsystem.wallpaper

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.hogwarts.android.core.designsystem.R

/**
 * A user-selectable home-screen wallpaper.
 *
 * Stored in preferences by [id] (a stable string), rendered from [drawableRes].
 * The label is a string resource so it localizes for Arabic / English.
 */
data class WallpaperOption(
    val id: String,
    @DrawableRes val drawableRes: Int,
    @StringRes val labelRes: Int
)

/**
 * The single source of truth for available wallpapers. Order here is the order
 * shown in the picker.
 */
object WallpaperCatalog {

    const val DEFAULT_ID: String = "aurora"

    val options: List<WallpaperOption> = listOf(
        WallpaperOption(
            id = "aurora",
            drawableRes = R.drawable.wallpaper_aurora,
            labelRes = R.string.wallpaper_label_aurora
        ),
        WallpaperOption(
            id = "sunset",
            drawableRes = R.drawable.wallpaper_sunset,
            labelRes = R.string.wallpaper_label_sunset
        ),
        WallpaperOption(
            id = "amber",
            drawableRes = R.drawable.wallpaper_amber,
            labelRes = R.string.wallpaper_label_amber
        ),
        WallpaperOption(
            id = "ocean",
            drawableRes = R.drawable.wallpaper_ocean,
            labelRes = R.string.wallpaper_label_ocean
        ),
        WallpaperOption(
            id = "farmland",
            drawableRes = R.drawable.wallpaper_farmland,
            labelRes = R.string.wallpaper_label_farmland
        )
    )

    fun findById(id: String?): WallpaperOption =
        options.firstOrNull { it.id == id } ?: options.first { it.id == DEFAULT_ID }
}
