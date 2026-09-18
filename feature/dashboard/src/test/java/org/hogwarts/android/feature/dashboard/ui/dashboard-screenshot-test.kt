package org.hogwarts.android.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.dashboard.data.remote.DashboardDto
import org.hogwarts.android.feature.dashboard.data.remote.DashboardSectionsDto
import org.hogwarts.android.feature.dashboard.testing.Fixtures
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h2400dp-xxhdpi")
class DashboardScreenshotTest {

    @Config(qualifiers = "+ar")
    @Test fun teacher_ar() = shot("dashboard_teacher_ar", Fixtures.teacher, UserRole.TEACHER, Fixtures.teacherSections, rtl = true, dark = false)

    @Config(qualifiers = "+ar")
    @Test fun teacher_ar_dark() = shot("dashboard_teacher_ar_dark", Fixtures.teacher, UserRole.TEACHER, Fixtures.teacherSections, rtl = true, dark = true)

    @Config(qualifiers = "+ar")
    @Test fun student_ar() = shot("dashboard_student_ar", Fixtures.student, UserRole.STUDENT, Fixtures.teacherSections, rtl = true, dark = false)

    /** The sections route is not deployed yet — both tables stay off the page. */
    @Test fun accountant_en() = shot("dashboard_accountant_en", Fixtures.accountant, UserRole.ACCOUNTANT, null, rtl = false, dark = false)

    /** An admin gets no day grid (the web filters it to students and teachers) and an empty invoice table. */
    @Test fun admin_en() = shot("dashboard_admin_en", Fixtures.admin, UserRole.ADMIN, Fixtures.adminSections, rtl = false, dark = false)

    private fun shot(
        name: String,
        data: DashboardDto,
        role: UserRole,
        sections: DashboardSectionsDto?,
        rtl: Boolean,
        dark: Boolean,
    ) {
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) {
                    // The page's ground comes from the app shell, so the
                    // capture has to paint it or a dark golden is light text
                    // on white and nothing in it can be judged.
                    Box(Modifier.fillMaxSize().background(HogwartsTheme.colors.background)) {
                        DashboardContent(
                            state = DashboardUiState(
                                isLoading = false,
                                role = role,
                                data = data,
                                sections = sections,
                                // The fixtures state their own `is_today`, so
                                // the device weekday never decides the heading.
                                weekday = 0,
                            ),
                            onOpenHref = {},
                            onAcknowledge = {},
                            onRefresh = {},
                        )
                    }
                }
            }
        }
    }
}
