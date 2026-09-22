package org.hogwarts.android.feature.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.hogwarts.android.feature.auth.domain.model.DemoRole
import org.hogwarts.android.feature.auth.domain.model.FieldError
import org.hogwarts.android.feature.auth.domain.model.SchoolInfo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Auth screens at phone size, ar/en × light/dark. Record with
 * `./gradlew :feature:auth:recordRoborazziDebug`; goldens in src/test/screenshots.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h844dp-xxhdpi")
class AuthScreenshotTest {

    private val filled = LoginUiState(identifier = "admin@balqalam.com", password = "1234")

    @Test fun login_ar_light() = capture("login_ar_light", ar = true, dark = false) { LoginContent(LoginUiState()) }
    @Test fun login_ar_dark() = capture("login_ar_dark", ar = true, dark = true) { LoginContent(filled) }
    @Test fun login_en_light() = capture("login_en_light", ar = false, dark = false) { LoginContent(filled) }
    @Test fun login_en_dark() = capture("login_en_dark", ar = false, dark = true) { LoginContent(LoginUiState()) }

    @Test fun login_error_ar() = capture("login_error_ar", ar = true, dark = false) {
        LoginContent(LoginUiState(identifier = "admin@balqalam.com", error = AuthError.InvalidCredentials))
    }

    @Test fun login_fields_en() = capture("login_fields_en", ar = false, dark = false) {
        LoginContent(
            LoginUiState(
                identifier = "a b",
                identifierError = FieldError.InvalidIdentifier,
                passwordError = FieldError.PasswordRequired,
                demoRoles = DemoRole.entries,
                canUseBiometric = true,
            ),
            onBiometric = {},
        )
    }

    @Test fun demo_ar() = capture("demo_ar", ar = true, dark = false) {
        DemoLoginContent(LoginUiState(mode = LoginMode.Demo, demoRoles = DemoRole.entries, demoRole = DemoRole.Admin), {}, {}, {})
    }

    @Test fun school_picker_ar() = capture("school_picker_ar", ar = true, dark = false) {
        SchoolPickerContent(schools, arabic = true, state = LoginUiState(schools = schools), onSelect = {}, onBack = {})
    }

    @Test fun school_picker_en_dark() = capture("school_picker_en_dark", ar = false, dark = true) {
        SchoolPickerContent(schools, arabic = false, state = LoginUiState(schools = schools), onSelect = {}, onBack = {})
    }

    @Test fun forgot_ar() = capture("forgot_ar", ar = true, dark = false) {
        ForgotPasswordContent(ForgotPasswordUiState(email = "parent", emailError = FieldError.InvalidEmail))
    }

    @Test fun otp_ar() = capture("otp_ar", ar = true, dark = false) {
        VerifyOtpContent(VerifyOtpUiState(email = "parent@balqalam.com", otp = "4831", resendCooldown = 72))
    }

    @Test fun otp_en_dark_error() = capture("otp_en_dark_error", ar = false, dark = true) {
        VerifyOtpContent(VerifyOtpUiState(email = "parent@balqalam.com", otp = "483", error = AuthError.InvalidCode))
    }

    @Test fun new_password_en() = capture("new_password_en", ar = false, dark = false) {
        NewPasswordContent(NewPasswordUiState(password = "secret", error = AuthError.CodeExpired))
    }

    @Test fun new_password_done_ar() = capture("new_password_done_ar", ar = true, dark = false) {
        NewPasswordContent(NewPasswordUiState(updated = true))
    }

    @Test fun welcome_ar() = capture("welcome_ar", ar = true, dark = false) { WelcomeContent() }

    private val schools = listOf(
        SchoolInfo("s1", "الملك فهد", "King Fahd", null, "kingfahd"),
        SchoolInfo("s2", "القبس", "Alqabs", null, "alqabs"),
    )

    private fun capture(name: String, ar: Boolean, dark: Boolean, content: @Composable () -> Unit) {
        RuntimeEnvironment.setQualifiers(if (ar) "+ar-ldrtl" else "+en-ldltr")
        if (dark) RuntimeEnvironment.setQualifiers("+night")
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (ar) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) { content() }
            }
        }
    }
}
