package org.hogwarts.android.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.auth.R
import kotlinx.coroutines.delay

/** How long the mark holds before the login page takes over. */
private const val WELCOME_HOLD_MS = 1_200L

/**
 * First screen when signed out: the brand mark alone — the orange feather
 * app icon — held for a moment after the system splash, then the login page.
 * No button: the web has no landing before its login either (a tenant host
 * goes straight to /login), so this is a beat, not a stop.
 */
@Composable
fun WelcomeScreen(onNavigateToLogin: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(WELCOME_HOLD_MS)
        onNavigateToLogin()
    }
    WelcomeContent()
}

@Composable
internal fun WelcomeContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HogwartsTheme.colors.background)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(HogwartsShapes.Tile)
                .background(BrandColors.AppIcon),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.auth_feather),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(48.dp),
            )
        }
    }
}
