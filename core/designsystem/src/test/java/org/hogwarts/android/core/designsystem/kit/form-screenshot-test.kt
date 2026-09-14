package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The form pieces the auth screens compose: Input (empty, filled, error,
 * disabled), PasswordInput, InputOtp, FormButton variants, FormAlert tones.
 * Record with `./gradlew :core:designsystem:recordRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h900dp-xxhdpi")
class FormScreenshotTest {

    @Test fun form_en_light() = capture("form_en_light", rtl = false, dark = false)
    @Test fun form_ar_dark() = capture("form_ar_dark", rtl = true, dark = true)

    private fun capture(name: String, rtl: Boolean, dark: Boolean) {
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) { FormSample(rtl) }
            }
        }
    }
}

@Composable
private fun FormSample(ar: Boolean) {
    val t = { en: String, arText: String -> if (ar) arText else en }
    Column(
        Modifier
            .fillMaxWidth()
            .background(HogwartsTheme.colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Input(value = "", onValueChange = {}, placeholder = t("Email or username", "البريد الإلكتروني أو اسم المستخدم"))
        Input(value = "admin@balqalam.com", onValueChange = {})
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Input(value = "a", onValueChange = {}, isError = true)
            FieldMessage(t("Enter a valid email or username", "أدخل بريدًا إلكترونيًا أو اسم مستخدم صحيحًا"))
        }
        Input(value = "", onValueChange = {}, placeholder = t("Disabled", "معطّل"), enabled = false)
        PasswordInput(
            value = "1234",
            onValueChange = {},
            visible = false,
            onToggleVisible = {},
            showLabel = "show",
            hideLabel = "hide",
            placeholder = t("Password", "كلمة المرور"),
        )
        InputOtp(value = "483", onValueChange = {})
        FormAlert(t("Invalid email or password", "البريد الإلكتروني أو كلمة المرور غير صحيحة"))
        FormAlert(t("Password updated! You can now sign in.", "تم تحديث كلمة المرور! يمكنك الآن تسجيل الدخول."), tone = FormAlertTone.Success)
        FormButton(t("Login", "دخول"), onClick = {})
        FormButton(t("Login", "دخول"), onClick = {}, loading = true)
        FormButton(t("Google", "جوجل"), onClick = {}, variant = FormButtonVariant.Outline)
        TextLink(t("Forgot Password?", "نسيت كلمة المرور؟"), onClick = {})
    }
}
