package org.hogwarts.android.feature.auth.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.core.designsystem.R as DesignR
import org.hogwarts.android.feature.auth.R

/**
 * Welcome screen — first screen after splash.
 *
 * Simple layout: logo centered + "Get Started" button that navigates to Login.
 * Accounts are created on the web app by school admins — mobile is login-only.
 */
@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit
) {
    WelcomeScreenContent(
        onGetStartedClick = onNavigateToLogin
    )
}

@Composable
private fun WelcomeScreenContent(
    onGetStartedClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Logo centered
            Image(
                painter = painterResource(id = DesignR.drawable.ic_logo),
                contentDescription = stringResource(R.string.auth_welcome_logo_description),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
            )

            // Get Started button
            HogwartsButton(
                text = stringResource(R.string.auth_welcome_get_started_button),
                onClick = onGetStartedClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 350.dp)
                    .fillMaxWidth()
                    .padding(bottom = 64.dp)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun WelcomeScreenPreview() {
    HogwartsTheme {
        WelcomeScreenContent(
            onGetStartedClick = {}
        )
    }
}
