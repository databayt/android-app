package org.hogwarts.android.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.kit.FormButton
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.auth.R

/**
 * First screen when signed out. The web has no landing before its login (a
 * tenant host goes straight to /login), so this is only the brand mark — the
 * orange feather app icon — and the way in.
 */
@Composable
fun WelcomeScreen(onNavigateToLogin: () -> Unit) {
    WelcomeContent(onLogin = onNavigateToLogin)
}

@Composable
internal fun WelcomeContent(onLogin: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HogwartsTheme.colors.background)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(96.dp)
                .clip(HogwartsShapes.Tile)
                .background(BrandColors.AppIcon),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.auth_feather),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(56.dp),
            )
        }
        FormButton(
            label = stringResource(R.string.auth_sign_in),
            onClick = onLogin,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 350.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        )
    }
}
