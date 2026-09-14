package org.hogwarts.android.feature.fees.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.fees.data.remote.StaffDashboardDto
import org.hogwarts.android.feature.fees.domain.Gateway
import org.hogwarts.android.feature.fees.testing.overdueStudent
import org.hogwarts.android.feature.fees.testing.settledGuardian
import org.hogwarts.android.feature.fees.testing.unbilledFamily
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * `/finance` per role at phone width. Record with
 * `./gradlew :feature:fees:recordRoborazziDebug`; goldens live in
 * src/test/screenshots. Every family, amount and invoice number is invented.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FinanceScreenshotTest {

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h2700dp-xxhdpi")
    fun student_overdue_en_light() = capture("student_overdue_en_light", rtl = false) { Family() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h2700dp-xxhdpi")
    fun student_overdue_ar_light() = capture("student_overdue_ar_light", rtl = true) { Family() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h2700dp-xxhdpi")
    fun student_overdue_ar_dark() = capture("student_overdue_ar_dark", rtl = true, dark = true) { Family() }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h1500dp-xxhdpi")
    fun guardian_settled_en_light() = capture("guardian_settled_en_light", rtl = false) { Family(settled = true) }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1500dp-xxhdpi")
    fun guardian_settled_ar_light() = capture("guardian_settled_ar_light", rtl = true) { Family(settled = true) }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h844dp-xxhdpi")
    fun unbilled_ar_light() = capture("unbilled_ar_light", rtl = true) { Family(unbilled = true) }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h844dp-xxhdpi")
    fun pay_sheet_en_light() = capture("pay_sheet_en_light", rtl = false) { Sheet() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h844dp-xxhdpi")
    fun pay_sheet_ar_light() = capture("pay_sheet_ar_light", rtl = true) { Sheet(loading = true) }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h1000dp-xxhdpi")
    fun accountant_en_light() = capture("accountant_en_light", rtl = false) { Staff(UserRole.ACCOUNTANT) }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1000dp-xxhdpi")
    fun accountant_ar_light() = capture("accountant_ar_light", rtl = true) { Staff(UserRole.ACCOUNTANT) }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1000dp-xxhdpi")
    fun accountant_ar_dark() = capture("accountant_ar_dark", rtl = true, dark = true) { Staff(UserRole.ACCOUNTANT) }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h844dp-xxhdpi")
    fun admin_en_light() = capture("admin_en_light", rtl = false) { Staff(UserRole.ADMIN) }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h844dp-xxhdpi")
    fun teacher_denied_ar_light() = capture("teacher_denied_ar_light", rtl = true) {
        FinancePage(UserRole.TEACHER, onOpenHref = {}) { NoPermission() }
    }

    @Composable
    private fun Family(settled: Boolean = false, unbilled: Boolean = false) {
        val money = when {
            unbilled -> unbilledFamily()
            settled -> settledGuardian()
            else -> overdueStudent()
        }
        FinancePage(UserRole.STUDENT, onOpenHref = {}) {
            FamilyFinanceView(FamilyUiState(loading = false, money = money), onPay = {}, onOpenInvoice = {}, onRetry = {})
        }
    }

    @Composable
    private fun Sheet(loading: Boolean = false) {
        Box(Modifier.background(HogwartsTheme.colors.background).padding(top = 24.dp)) {
            PaySheetContent(
                PaySheet(
                    feeAssignmentId = "fee-1",
                    label = "Tuition · 2026-2027",
                    gateways = listOf(Gateway.STRIPE, Gateway.BANKAK, Gateway.CASHI),
                    loading = if (loading) Gateway.STRIPE else null,
                ),
                onChoose = {},
            )
        }
    }

    @Composable
    private fun Staff(role: UserRole) {
        val stats = if (role == UserRole.ACCOUNTANT) {
            StaffDashboardDto(role = "ACCOUNTANT", pendingInvoices = 12, pendingAmount = 48000.0, overdueInvoices = 3, overdueAmount = 9000.0, collectedToday = 15250.0)
        } else {
            null
        }
        FinancePage(role, onOpenHref = {}) { StaffFinanceView(StaffUiState(loading = false, role = role, stats = stats), onOpenHref = {}) }
    }

    private fun capture(name: String, rtl: Boolean, dark: Boolean = false, content: @Composable () -> Unit) {
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) {
                    Box(Modifier.background(HogwartsTheme.colors.background)) { content() }
                }
            }
        }
    }
}
