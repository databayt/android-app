package org.hogwarts.android.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import org.hogwarts.android.core.security.BiometricHelper
import org.hogwarts.android.feature.auth.ui.ForgotPasswordScreen
import org.hogwarts.android.feature.auth.ui.LoginScreen
import org.hogwarts.android.feature.auth.ui.WelcomeScreen

@Serializable data object AuthGraph
@Serializable data object Welcome
@Serializable data object Login
@Serializable data object ForgotPassword

/**
 * Auth navigation graph.
 *
 * Flow: Welcome → Login → Dashboard
 * Accounts are created on the web app by school admins — mobile is login-only.
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit,
    biometricHelper: BiometricHelper
) {
    navigation<AuthGraph>(
        startDestination = Welcome
    ) {
        // Welcome — "Get Started" leads to Login
        composable<Welcome> {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Login)
                }
            )
        }

        // Login
        composable<Login> {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onNavigateToForgotPassword = {
                    navController.navigate(ForgotPassword)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                biometricHelper = biometricHelper
            )
        }

        // Forgot Password
        composable<ForgotPassword> {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onResetSuccess = {
                    navController.popBackStack<Login>(inclusive = false)
                }
            )
        }
    }
}
