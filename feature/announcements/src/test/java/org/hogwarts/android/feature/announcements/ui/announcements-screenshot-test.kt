package org.hogwarts.android.feature.announcements.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.announcements.testing.announcement
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Fixtures are invented — android-app is public. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h844dp-xxhdpi")
class AnnouncementsScreenshotTest {

    private val enItems = listOf(
        announcement("ann-1", "Sports day moves to Thursday", priority = "urgent", day = 14),
        announcement("ann-2", "Library opens late this week", scope = "class", day = 13),
        announcement("ann-3", "Staff meeting agenda for the new term", scope = "role", targetRole = "TEACHER", published = false, day = 12),
        announcement("ann-4", "Science fair registration", priority = "high", day = 11),
        announcement("ann-5", "Uniform reminder", day = 9),
    )

    private val arItems = listOf(
        announcement("ann-1", "نقل اليوم الرياضي إلى الخميس", priority = "urgent", day = 14),
        announcement("ann-2", "تأخر افتتاح المكتبة هذا الأسبوع", scope = "class", day = 13),
        announcement("ann-3", "جدول اجتماع المعلمين للفصل الجديد", scope = "role", targetRole = "TEACHER", published = false, day = 12),
        announcement("ann-4", "التسجيل في معرض العلوم", priority = "high", day = 11),
        announcement("ann-5", "تذكير بالزي المدرسي", day = 9),
    )

    private fun list(role: UserRole, items: List<org.hogwarts.android.feature.announcements.domain.model.Announcement>, hasMore: Boolean = true) =
        AnnouncementsUiState(role = role, items = items, page = 1, hasMore = hasMore, isLoading = false)

    @Test fun teacher_en_light() = shot("announcements_teacher_en_light", rtl = false, dark = false) { List(list(UserRole.TEACHER, enItems)) }

    @Config(qualifiers = "+ar")
    @Test fun teacher_ar_light() = shot("announcements_teacher_ar_light", rtl = true, dark = false) { List(list(UserRole.ADMIN, arItems)) }

    @Config(qualifiers = "+ar")
    @Test fun student_ar_dark() = shot("announcements_student_ar_dark", rtl = true, dark = true) {
        List(list(UserRole.STUDENT, arItems.filter { it.isPublished && it.targetRole == null }, hasMore = false))
    }

    @Test fun empty_en_light() = shot("announcements_empty_en_light", rtl = false, dark = false) {
        List(AnnouncementsUiState(role = UserRole.GUARDIAN, isLoading = false))
    }

    @Test fun detail_en_light() = shot("announcement_detail_en_light", rtl = false, dark = false) {
        AnnouncementDetailContent(
            state = AnnouncementDetailUiState.Ready(
                announcement(
                    "ann-3", "Staff meeting agenda for the new term", scope = "role", targetRole = "TEACHER",
                    published = false, priority = "high",
                    body = "We meet in the main hall after the last period.\n\nPlease bring your term plans and one question for the team.",
                ),
            ),
            onBack = {}, onRetry = {},
        )
    }

    @Config(qualifiers = "+ar")
    @Test fun detail_ar_light() = shot("announcement_detail_ar_light", rtl = true, dark = false) { Detail(arDetail) }

    @Config(qualifiers = "+ar")
    @Test fun detail_ar_dark() = shot("announcement_detail_ar_dark", rtl = true, dark = true) { Detail(arDetail) }

    @Config(qualifiers = "+ar")
    @Test fun detail_not_found_ar_light() = shot("announcement_not_found_ar_light", rtl = true, dark = false) {
        AnnouncementDetailContent(state = AnnouncementDetailUiState.NotFound, onBack = {}, onRetry = {})
    }

    private val arDetail = announcement(
        "ann-1", "نقل اليوم الرياضي إلى الخميس", priority = "urgent", day = 14,
        body = "نُقل اليوم الرياضي إلى يوم الخميس بسبب الطقس. يحضر الطلاب بالزي الرياضي، وتبدأ الفعاليات بعد الطابور الصباحي.",
    )

    @Composable
    private fun List(state: AnnouncementsUiState) =
        AnnouncementsContent(state = state, onQueryChange = {}, onOpenAnnouncement = {}, onOpenHref = {}, onLoadMore = {}, onRetry = {})

    @Composable
    private fun Detail(item: org.hogwarts.android.feature.announcements.domain.model.Announcement) =
        AnnouncementDetailContent(state = AnnouncementDetailUiState.Ready(item), onBack = {}, onRetry = {})

    private fun shot(name: String, rtl: Boolean, dark: Boolean, content: @Composable () -> Unit) {
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) { content() }
            }
        }
    }
}
