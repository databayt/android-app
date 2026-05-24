package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import org.hogwarts.android.core.designsystem.wallpaper.WallpaperCatalog

/**
 * Home-screen wallpaper backdrop. Resolves the user's selected wallpaper id
 * via [WallpaperCatalog]; falls back to the default option for unknown ids.
 */
@Composable
fun HomeWallpaper(
    modifier: Modifier = Modifier,
    wallpaperId: String = WallpaperCatalog.DEFAULT_ID,
    content: @Composable BoxScope.() -> Unit
) {
    val option = WallpaperCatalog.findById(wallpaperId)
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(option.drawableRes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        content()
    }
}
