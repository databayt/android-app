package org.hogwarts.android.feature.attendance.ui

import androidx.compose.runtime.Composable
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.attendance.domain.model.AttendanceEntry
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStats
import org.hogwarts.android.feature.attendance.domain.model.ChildAttendance
import org.hogwarts.android.feature.attendance.domain.model.MarkStatus
import org.hogwarts.android.feature.attendance.domain.model.QuickSection
import org.hogwarts.android.feature.attendance.domain.model.RosterStudent
import org.hogwarts.android.feature.attendance.domain.model.StudentAttendance
import org.hogwarts.android.feature.attendance.domain.model.TodayTotals
import org.hogwarts.android.feature.attendance.ui.mine.MineUiState
import org.hogwarts.android.feature.attendance.ui.mine.MineView
import org.hogwarts.android.feature.attendance.ui.overview.StaffOverviewUiState
import org.hogwarts.android.feature.attendance.ui.overview.StaffOverviewView
import org.hogwarts.android.feature.attendance.ui.quick.QuickAttendanceUiState
import org.hogwarts.android.feature.attendance.ui.quick.QuickAttendanceView
import org.hogwarts.android.feature.attendance.ui.quick.SavedPanel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/**
 * Each role's `/attendance` landing at phone width, Arabic and English, light.
 * Record with `./gradlew :feature:attendance:recordRoborazziDebug`; goldens
 * live in src/test/screenshots.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AttendanceScreenshotTest {

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h844dp-xxhdpi")
    fun teacher_quick_ar_light() = capture("teacher_quick_ar_light") { Quick(ar = true) }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h844dp-xxhdpi")
    fun teacher_quick_en_light() = capture("teacher_quick_en_light") { Quick(ar = false) }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h844dp-xxhdpi")
    fun teacher_saved_ar_light() = capture("teacher_saved_ar_light") { Quick(ar = true, saved = true) }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h844dp-xxhdpi")
    fun teacher_saved_en_light() = capture("teacher_saved_en_light") { Quick(ar = false, saved = true) }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h844dp-xxhdpi")
    fun admin_overview_ar_light() = capture("admin_overview_ar_light") { Staff() }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h844dp-xxhdpi")
    fun admin_overview_en_light() = capture("admin_overview_en_light") { Staff() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1100dp-xxhdpi")
    fun student_ar_light() = capture("student_ar_light") { Student(ar = true) }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h1100dp-xxhdpi")
    fun student_en_light() = capture("student_en_light") { Student(ar = false) }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1500dp-xxhdpi")
    fun guardian_ar_light() = capture("guardian_ar_light") { Guardian(ar = true) }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h1500dp-xxhdpi")
    fun guardian_en_light() = capture("guardian_en_light") { Guardian(ar = false) }

    private fun capture(name: String, content: @Composable () -> Unit) {
        captureRoboImage("src/test/screenshots/$name.png") {
            HogwartsTheme(darkTheme = false) { content() }
        }
    }
}

private val TODAY = LocalDate.of(2026, 9, 14)

private fun tabs(role: UserRole): @Composable () -> Unit = { AttendanceTabs(tabsForRole(role), onNavigate = {}) }

@Composable
private fun Quick(ar: Boolean, saved: Boolean = false) {
    val n = { en: String, arabic: String -> if (ar) arabic else en }
    val sections = listOf(
        QuickSection("a", n("Grade 5 · A", "الخامس · أ"), "", 6, 0, true, "P2", "08:20", "09:00", isCurrent = true),
        QuickSection("b", n("Grade 5 · B", "الخامس · ب"), "", 6, 6, true, "P4", "10:10", "10:50", isCurrent = false),
        QuickSection("c", n("Grade 6 · A", "السادس · أ"), "", 5, 0, false, null, null, null, isCurrent = false),
    )
    val roster = listOf(
        RosterStudent("1", n("Ahmed Ali", "أحمد علي"), MarkStatus.Present),
        RosterStudent("2", n("Bashir Osman", "بشير عثمان"), MarkStatus.Absent),
        RosterStudent("3", n("Dalia Hassan", "داليا حسن"), MarkStatus.Present),
        RosterStudent("4", n("Hiba Mohamed", "هبة محمد"), MarkStatus.Late),
        RosterStudent("5", n("Omar Ibrahim", "عمر إبراهيم"), MarkStatus.Present),
        RosterStudent("6", n("Sara Khalid", "سارة خالد"), MarkStatus.Present),
    )
    QuickAttendanceView(
        state = QuickAttendanceUiState(
            today = TODAY,
            sections = sections,
            selectedSectionId = "a",
            roster = roster,
            saved = if (saved) {
                SavedPanel(present = 4, absent = 1, late = 1, guardiansNotified = 1, absentNames = listOf(roster[1].name), queued = false)
            } else {
                null
            },
        ),
        nav = tabs(UserRole.TEACHER),
        onSelectSection = {}, onCycle = {}, onSearch = {}, onSave = {}, onMarkAnother = {}, onMessageGuardian = {}, onRetry = {},
    )
}

@Composable
private fun Staff() {
    StaffOverviewView(
        state = StaffOverviewUiState(loading = false, totals = TodayTotals(marked = 439, present = 412, absent = 18, late = 9), isAdmin = true),
        nav = tabs(UserRole.ADMIN),
        onOpenTile = {},
        onRetry = {},
    )
}

@Composable
private fun Student(ar: Boolean) {
    val n = { en: String, arabic: String -> if (ar) arabic else en }
    val records = listOf(
        AttendanceEntry("1", TODAY, "PRESENT", n("Period 1 · A", "الحصة الأولى · أ")),
        AttendanceEntry("2", TODAY.minusDays(1), "LATE", n("Period 1 · A", "الحصة الأولى · أ")),
        AttendanceEntry("3", TODAY.minusDays(2), "ABSENT", n("Period 1 · A", "الحصة الأولى · أ")),
        AttendanceEntry("4", TODAY.minusDays(3), "EXCUSED", null),
        AttendanceEntry("5", TODAY.minusDays(4), "PRESENT", n("Period 1 · A", "الحصة الأولى · أ")),
    )
    MineView(
        state = MineUiState.Student(StudentAttendance(AttendanceStats(totalDays = 42, present = 38, absent = 2, late = 1, excused = 1), records)),
        nav = tabs(UserRole.STUDENT),
        onRetry = {},
    )
}

@Composable
private fun Guardian(ar: Boolean) {
    val n = { en: String, arabic: String -> if (ar) arabic else en }
    val children = listOf(
        ChildAttendance(
            "c1", n("Sara Ahmed", "سارة أحمد"), n("Grade 5 · A", "الخامس · أ"),
            AttendanceStats(42, 39, 2, 1, 0),
            listOf(
                AttendanceEntry("a1", TODAY.minusDays(1), "ABSENT", n("Period 1 · A", "الحصة الأولى · أ")),
                AttendanceEntry("a2", TODAY.minusDays(6), "LATE", n("Period 1 · A", "الحصة الأولى · أ")),
            ),
        ),
        ChildAttendance("c2", n("Omar Ahmed", "عمر أحمد"), n("Grade 2 · B", "الثاني · ب"), AttendanceStats(40, 40, 0, 0, 0), emptyList()),
    )
    MineView(state = MineUiState.Guardian(children), nav = tabs(UserRole.GUARDIAN), onRetry = {})
}
