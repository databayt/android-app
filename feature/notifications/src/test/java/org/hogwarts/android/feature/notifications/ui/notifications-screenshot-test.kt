package org.hogwarts.android.feature.notifications.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel
import org.hogwarts.android.feature.notifications.domain.model.NotificationKind
import org.hogwarts.android.feature.notifications.domain.model.NotificationPage
import org.hogwarts.android.feature.notifications.domain.model.NotificationPriority
import org.hogwarts.android.feature.notifications.domain.model.PreferenceMatrix
import org.hogwarts.android.feature.notifications.testing.notification
import org.hogwarts.android.feature.notifications.ui.preferences.PreferencesContent
import org.hogwarts.android.feature.notifications.ui.preferences.PreferencesUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Duration
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h844dp-xxhdpi")
class NotificationsScreenshotTest {

    private val now = Instant.parse("2026-09-14T12:00:00Z")

    private val english = listOf(
        notification("n-1", "fee_overdue", priority = NotificationPriority.Urgent, title = "Term fee overdue",
            body = "The second instalment for Layla Haddad was due last week.", createdAt = now.minus(Duration.ofMinutes(5))),
        notification("n-2", "attendance_alert", priority = NotificationPriority.High, title = "Absence alert",
            body = "Omar Nasser was absent from period 3.", createdAt = now.minus(Duration.ofHours(3))),
        notification("n-3", "announcement", read = true, title = "Science fair moves to the main hall",
            body = "Projects are set up from eight on Sunday.", createdAt = now.minus(Duration.ofDays(2))),
        notification("n-4", "event_reminder", read = true, title = "Event reminder",
            body = "Parents evening starts at five.", createdAt = now.minus(Duration.ofDays(40))),
    )

    private val arabic = listOf(
        notification("n-1", "fee_overdue", priority = NotificationPriority.Urgent, title = "رسوم الفصل متأخرة",
            body = "القسط الثاني للطالبة ليلى حداد كان مستحقاً الأسبوع الماضي.", createdAt = now.minus(Duration.ofMinutes(5))),
        notification("n-2", "attendance_alert", priority = NotificationPriority.High, title = "غياب في الحصة الثالثة",
            body = "تغيّب الطالب عمر ناصر عن الحصة الثالثة.", createdAt = now.minus(Duration.ofHours(3))),
        notification("n-3", "announcement", read = true, title = "نقل معرض العلوم إلى القاعة الرئيسية",
            body = "تُجهَّز المشاريع من الثامنة يوم الأحد.", createdAt = now.minus(Duration.ofDays(2))),
        notification("n-4", "event_reminder", read = true, title = "تذكير بحدث",
            body = "يبدأ لقاء أولياء الأمور في الخامسة.", createdAt = now.minus(Duration.ofDays(40))),
    )

    private fun list(items: List<org.hogwarts.android.feature.notifications.domain.model.AppNotification>) =
        NotificationsUiState(isLoading = false, page = NotificationPage(items, 45, 2, 1, 20), unreadCount = 2)

    @Test fun list_en() = shot("notifications_list_en", rtl = false, dark = false) { List(list(english)) }
    @Config(qualifiers = "+ar")
    @Test fun list_ar() = shot("notifications_list_ar", rtl = true, dark = false) { List(list(arabic)) }
    @Config(qualifiers = "+ar")
    @Test fun list_ar_dark() = shot("notifications_list_ar_dark", rtl = true, dark = true) { List(list(arabic)) }

    @Config(qualifiers = "+ar")
    @Test fun empty_ar() = shot("notifications_empty_ar", rtl = true, dark = false) {
        List(NotificationsUiState(tab = NotificationsTab.Unread, isLoading = false, page = NotificationPage(emptyList(), 0, 0, 1, 20)))
    }

    private val matrix = PreferenceMatrix.from(emptyList())
        .with(NotificationKind.Message, NotificationChannel.Push, true)
        .with(NotificationKind.MessageMention, NotificationChannel.Email, true)

    @Test fun preferences_en() = shot("notifications_preferences_en", rtl = false, dark = false) { Prefs() }
    @Config(qualifiers = "+ar")
    @Test fun preferences_ar() = shot("notifications_preferences_ar", rtl = true, dark = false) { Prefs() }
    @Config(qualifiers = "+ar")
    @Test fun preferences_ar_dark() = shot("notifications_preferences_ar_dark", rtl = true, dark = true) { Prefs() }

    @Composable
    private fun List(state: NotificationsUiState) = NotificationsContent(
        state = state, now = now, onSelectTab = {}, onMarkAllRead = {}, onOpen = {}, onDelete = {}, onPage = {}, onRetry = {},
    )

    @Composable
    private fun Prefs() = PreferencesContent(
        state = PreferencesUiState(isLoading = false, saved = matrix, matrix = matrix, unreadCount = 2),
        onSelectTab = {}, onMarkAllRead = {}, onToggle = { _, _, _ -> }, onReset = {}, onSave = {}, onRetry = {},
    )

    private fun shot(name: String, rtl: Boolean, dark: Boolean, content: @Composable () -> Unit) {
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) { content() }
            }
        }
    }
}
