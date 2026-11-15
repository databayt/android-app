package org.hogwarts.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.core.security.BiometricHelper
import org.hogwarts.android.navigation.HogwartsNavHost
import org.hogwarts.android.shell.AppShell
import org.hogwarts.android.shell.IncomingLink
import javax.inject.Inject

/**
 * Main Activity for Hogwarts Android.
 *
 * Single-activity architecture using Jetpack Compose Navigation.
 * Splash screen is held until session check completes.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var biometricHelper: BiometricHelper

    @Inject
    lateinit var tenantContext: TenantContext

    private val viewModel: MainViewModel by viewModels()

    /** A school link the app was opened with (a class page from the web room's "open in the app"). */
    private val incomingLink = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Hold splash screen until auth state is determined
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.isLoading }

        enableEdgeToEdge()
        if (savedInstanceState == null) incomingLink.value = IncomingLink.path(intent)

        setContent {
            HogwartsApp(
                biometricHelper = biometricHelper,
                tenantContext = tenantContext,
                incomingLink = incomingLink,
                onLinkOpened = { incomingLink.value = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        IncomingLink.path(intent)?.let { incomingLink.value = it }
    }
}

@Composable
fun HogwartsApp(
    biometricHelper: BiometricHelper,
    tenantContext: TenantContext,
    incomingLink: StateFlow<String?>,
    onLinkOpened: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val link by incomingLink.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    if (uiState.isLoading) return

    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    HogwartsTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            val navHost: @Composable (Modifier) -> Unit = { modifier ->
                HogwartsNavHost(
                    navController = navController,
                    isAuthenticated = uiState.isAuthenticated,
                    onLogout = viewModel::logout,
                    biometricHelper = biometricHelper,
                    tenantContext = tenantContext,
                    modifier = modifier
                )
            }

            if (uiState.isAuthenticated) {
                // A link waits through sign-in: the shell opens it once there is one.
                AppShell(
                    navController = navController, content = navHost, onLogout = viewModel::logout,
                    pendingHref = link, onHrefOpened = onLinkOpened,
                )
            } else {
                navHost(Modifier)
            }
        }
    }
}
