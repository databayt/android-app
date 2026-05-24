package org.hogwarts.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.core.security.BiometricHelper
import org.hogwarts.android.navigation.HogwartsNavHost
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

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Hold splash screen until auth state is determined
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.isLoading }

        enableEdgeToEdge()

        setContent {
            HogwartsApp(
                biometricHelper = biometricHelper,
                tenantContext = tenantContext
            )
        }
    }
}

@Composable
fun HogwartsApp(
    biometricHelper: BiometricHelper,
    tenantContext: TenantContext,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) return

    HogwartsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            HogwartsNavHost(
                navController = navController,
                isAuthenticated = uiState.isAuthenticated,
                onLogout = viewModel::logout,
                biometricHelper = biometricHelper,
                tenantContext = tenantContext
            )
        }
    }
}
