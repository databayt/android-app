package org.hogwarts.android.feature.timetable.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.timetable.domain.model.RangeMode
import org.hogwarts.android.feature.timetable.domain.model.toChild
import org.hogwarts.android.feature.timetable.domain.model.toWeek
import org.hogwarts.android.feature.timetable.testing.Fixtures
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Fictional schools only — this repository is public. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h844dp-xxhdpi")
class TimetableScreenshotTest {

    /** Monday, 08:30 — inside Period 2, which is online today. */
    private val now = 8 * 60 + 30

    private fun student(arabic: Boolean, range: RangeMode, closure: String? = null) = TimetableUiState(
        role = UserRole.STUDENT,
        isLoading = false,
        week = Fixtures.studentWeek(arabic, today = 1, closure = closure).toWeek(1),
        pickedRange = range,
        nowMinutes = now,
    )

    private fun teacher(arabic: Boolean, tab: TimetableTab) = TimetableUiState(
        role = UserRole.TEACHER,
        userName = if (arabic) "نور حداد" else "Nour Haddad",
        isLoading = false,
        week = Fixtures.teacherWeek(arabic, today = 1).toWeek(1),
        tab = tab,
        nowMinutes = 7 * 60 + 40,
    )

    private fun guardian(arabic: Boolean, tab: TimetableTab) = TimetableUiState(
        role = UserRole.GUARDIAN,
        isLoading = false,
        week = Fixtures.studentWeek(arabic, today = 1, liveOnToday = false).copy(today = null).toWeek(1),
        children = Fixtures.children(arabic).map { it.toChild() },
        selectedChildId = "child-1",
        tab = tab,
        nowMinutes = now,
    )

    @Test fun student_day_en() = shot("timetable_student_day_en", student(false, RangeMode.Day, closure = "Founders Day"), rtl = false, dark = false)

    @Config(qualifiers = "+ar")
    @Test fun student_week_ar() = shot("timetable_student_week_ar", student(true, RangeMode.Week), rtl = true, dark = false)

    @Config(qualifiers = "+ar")
    @Test fun student_day_ar_dark() = shot("timetable_student_day_ar_dark", student(true, RangeMode.Day), rtl = true, dark = true)

    @Config(qualifiers = "ar-w390dp-h1100dp-xxhdpi")
    @Test fun teacher_today_ar() = shot("timetable_teacher_today_ar", teacher(true, TimetableTab.Today), rtl = true, dark = false)

    @Test fun teacher_week_en() = shot("timetable_teacher_week_en", teacher(false, TimetableTab.Full), rtl = false, dark = false)

    @Config(qualifiers = "ar-w390dp-h1100dp-xxhdpi")
    @Test fun guardian_today_ar_dark() = shot("timetable_guardian_today_ar_dark", guardian(true, TimetableTab.Today), rtl = true, dark = true)

    @Config(qualifiers = "+ar")
    @Test fun admin_ar() = shot("timetable_admin_ar", TimetableUiState(role = UserRole.ADMIN, isLoading = false), rtl = true, dark = false)

    private fun shot(name: String, state: TimetableUiState, rtl: Boolean, dark: Boolean) {
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) {
                    TimetableContent(state = state, actions = TimetableActions())
                }
            }
        }
    }
}
