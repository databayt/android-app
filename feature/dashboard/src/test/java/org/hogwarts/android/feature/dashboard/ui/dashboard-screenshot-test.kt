package org.hogwarts.android.feature.dashboard.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.dashboard.data.remote.DashboardDto
import org.hogwarts.android.feature.dashboard.data.remote.NextActionDto
import org.hogwarts.android.feature.dashboard.data.remote.PeriodDto
import org.hogwarts.android.feature.dashboard.data.remote.QuickActionDto
import org.hogwarts.android.feature.dashboard.data.remote.TodayTimetableDto
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h1400dp-xxhdpi")
class DashboardScreenshotTest {

    private val teacher = DashboardDto(
        role = "TEACHER",
        eventsToday = 2,
        totalClasses = 6,
        todayClasses = 4,
        nextActions = listOf(
            NextActionDto("attendanceDue", "2", "/attendance"),
            NextActionDto("pendingGrading", "14", "/grades"),
        ),
        quickActions = listOf(
            QuickActionDto("attendance", "Attendance", href = "/attendance"),
            QuickActionDto("grades", "Grades", href = "/grades"),
            QuickActionDto("assignments", "Assignments", href = "/assignments"),
            QuickActionDto("timetable", "Schedule", href = "/timetable"),
        ),
        todayTimetable = TodayTimetableDto(
            dayOfWeek = 0,
            periods = listOf(
                PeriodDto("p0", "الحصة 0", "06:45", "07:25"),
                PeriodDto("p1", "الحصة 1", "07:30", "08:15", subject = "الرياضيات", className = "الخامس · أ", room = "ب12", timetableId = "t1"),
                PeriodDto("p2", "استراحة", "08:15", "08:30", isBreak = true),
                PeriodDto("p3", "الحصة 2", "08:30", "09:15", subject = "العلوم", className = "السادس · ب", room = "Lab", timetableId = "t3"),
            ),
        ),
    )

    private val accountant = DashboardDto(
        role = "ACCOUNTANT",
        eventsToday = 0,
        pendingInvoices = 12, pendingAmount = 48000.0, overdueInvoices = 3, overdueAmount = 9000.0, collectedToday = 15250.0,
        nextActions = listOf(NextActionDto("overdueInvoices", "3", "/finance/invoice")),
        quickActions = listOf(
            QuickActionDto("finance_invoice", "Invoices", href = "/finance/invoice"),
            QuickActionDto("finance_fees", "Fees", href = "/finance/fees"),
            QuickActionDto("finance", "Finance", href = "/finance"),
            QuickActionDto("finance_receipt", "Receipts", href = "/finance/receipt"),
        ),
    )

    @Config(qualifiers = "+ar")
    @Test fun teacher_ar() = shot("dashboard_teacher_ar", teacher, UserRole.TEACHER, rtl = true, dark = false)
    @Test fun accountant_en() = shot("dashboard_accountant_en", accountant, UserRole.ACCOUNTANT, rtl = false, dark = false)
    @Config(qualifiers = "+ar")
    @Test fun teacher_ar_dark() = shot("dashboard_teacher_ar_dark", teacher, UserRole.TEACHER, rtl = true, dark = true)

    private fun shot(name: String, data: DashboardDto, role: UserRole, rtl: Boolean, dark: Boolean) {
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) {
                    DashboardContent(
                        state = DashboardUiState(isLoading = false, role = role, data = data),
                        onOpenHref = {},
                        onAcknowledge = {},
                        onRefresh = {},
                    )
                }
            }
        }
    }
}
