package org.hogwarts.android.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.hogwarts.android.core.security.BiometricHelper
import org.hogwarts.android.feature.auth.ui.ForgotPasswordScreen
import org.hogwarts.android.feature.auth.ui.LoginScreen
import org.hogwarts.android.feature.auth.ui.NewPasswordScreen
import org.hogwarts.android.feature.auth.ui.VerifyOtpScreen
import org.hogwarts.android.feature.auth.ui.WelcomeScreen

@Serializable data object AuthGraph
@Serializable data object Welcome
@Serializable data object Login
@Serializable data object ForgotPassword
@Serializable data class VerifyOtp(val email: String)
@Serializable data class NewPassword(val email: String, val otp: String)

/**
 * Auth flow, mirroring hogwarts `(auth)`:
 * Welcome → Login (→ school picker for a multi-school Google identity) → Dashboard,
 * and Login → Forgot password → Code → New password → Login.
 * Accounts are created on the web; "Don't have an account?" opens web /join.
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit,
    biometricHelper: BiometricHelper
) {
    navigation<AuthGraph>(startDestination = Welcome) {
        composable<Welcome> {
            WelcomeScreen(onNavigateToLogin = { navController.navigate(Login) { launchSingleTop = true } })
        }

        composable<Login> {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onNavigateToForgotPassword = { navController.navigate(ForgotPassword) { launchSingleTop = true } },
                biometricHelper = biometricHelper,
            )
        }

        composable<ForgotPassword> {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onCodeSent = { email -> navController.navigate(VerifyOtp(email)) { launchSingleTop = true } },
            )
        }

        composable<VerifyOtp> { entry ->
            val email = entry.toRoute<VerifyOtp>().email
            VerifyOtpScreen(
                onCodeEntered = { otp -> navController.navigate(NewPassword(email, otp)) { launchSingleTop = true } },
                onWrongEmail = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack<Login>(inclusive = false) },
            )
        }

        composable<NewPassword> {
            NewPasswordScreen(
                onBackToCode = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack<Login>(inclusive = false) },
            )
        }
    }
}
