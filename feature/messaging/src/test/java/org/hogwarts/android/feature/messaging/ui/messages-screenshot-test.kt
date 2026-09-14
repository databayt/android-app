package org.hogwarts.android.feature.messaging.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.messaging.FakeMessagingRepository.Companion.ME
import org.hogwarts.android.feature.messaging.chat
import org.hogwarts.android.feature.messaging.domain.model.MessagingViewer
import org.hogwarts.android.feature.messaging.message
import org.hogwarts.android.feature.messaging.ui.chats.ChatListActions
import org.hogwarts.android.feature.messaging.ui.format.MessagingFormat
import org.hogwarts.android.feature.messaging.ui.shell.MessagesShellContent
import org.hogwarts.android.feature.messaging.ui.shell.MessagesTab
import org.hogwarts.android.feature.messaging.ui.shell.ShellUiState
import org.hogwarts.android.feature.messaging.ui.thread.ThreadContent
import org.hogwarts.android.feature.messaging.ui.thread.ThreadUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

/**
 * Goldens for the Messages chat list and thread at the web's 390px measure,
 * compared by eye against hogwarts `public/whatsapp/File (1).png`, `File (2).png`
 * and `File (4).png`. The rows are test fixtures in the shape the mobile API
 * returns (no participants, no photos for 1:1s).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h844dp-xxhdpi")
class MessagesScreenshotTest {

    private val today = LocalDate.parse("2026-09-14")
    private fun format(locale: Locale) = MessagingFormat(locale, ZoneOffset.UTC, { today })

    private val chatsAr = listOf(
        chat("conv-year5-parents", "أولياء أمور الصف الخامس", type = "class", unread = 5, last = "نماذج الرحلة مطلوبة يوم الخميس، أرسلوها مع أبنائكم", lastSender = "سارة أحمد", at = "2026-09-14T07:29:00Z"),
        chat("conv-omar-hassan", "عمر حسن", last = "شكرًا، أراك غدًا", lastSender = "Minerva", status = "delivered", at = "2026-09-14T07:06:00Z"),
        chat("conv-huda-farouk", "هدى فاروق", last = "تمام", lastSender = "هدى فاروق", at = "2026-09-13T12:00:00Z"),
        chat("c4", "مكتب الاستقبال", type = "department", last = "طُبعت وهي على مكتبك", lastSender = "Minerva", status = "read", at = "2026-09-13T10:00:00Z"),
        chat("c5", "كريم صالح", unread = 1, last = "تمام، نلتقي الساعة التاسعة", lastSender = "كريم صالح", at = "2026-09-13T09:00:00Z"),
        chat("c6", "قسم العلوم", type = "group", pinned = true, last = "حجزنا المختبر يوم الأربعاء", lastSender = "هناء", at = "2026-09-12T09:00:00Z"),
        chat("c7", "ليلى ناصر", last = "مساء الغد يناسبني", lastSender = "Minerva", status = "read", at = "2026-09-10T09:00:00Z"),
    )

    private val chatsEn = listOf(
        chat("conv-year5-parents", "Year 5 · Parents", type = "class", unread = 5, last = "The trip forms are due on Thursday, please send them with your child", lastSender = "Sara Ahmed", at = "2026-09-14T07:29:00Z"),
        chat("conv-omar-hassan", "Omar Hassan", last = "Thank you, see you tomorrow", lastSender = "Minerva", status = "delivered", at = "2026-09-14T07:06:00Z"),
        chat("conv-huda-farouk", "Huda Farouk", last = "Fine", lastSender = "Huda Farouk", at = "2026-09-13T12:00:00Z"),
        chat("c4", "Front office", type = "department", last = "Printed and on your desk", lastSender = "Minerva", status = "read", at = "2026-09-13T10:00:00Z"),
        chat("c5", "Karim Saleh", unread = 1, last = "Great, we meet at nine", lastSender = "Karim Saleh", at = "2026-09-13T09:00:00Z"),
        chat("c6", "Science department", type = "group", pinned = true, last = "Lab booked for Wednesday", lastSender = "Hana", at = "2026-09-12T09:00:00Z"),
        chat("c7", "Layla Nasser", last = "Tomorrow evening works", lastSender = "Minerva", status = "read", at = "2026-09-10T09:00:00Z"),
    )

    private val viewer = MessagingViewer(ME, "Minerva", null, null, "Hogwarts", null)

    @Test fun chats_en() = shell("messages_chats_en", chatsEn, Locale.ENGLISH, rtl = false, dark = false)

    @Config(qualifiers = "+ar")
    @Test fun chats_ar() = shell("messages_chats_ar", chatsAr, Locale.forLanguageTag("ar"), rtl = true, dark = false)

    @Config(qualifiers = "+ar-night")
    @Test fun chats_ar_dark() = shell("messages_chats_ar_dark", chatsAr, Locale.forLanguageTag("ar"), rtl = true, dark = true)

    @Config(qualifiers = "+ar")
    @Test fun settings_ar() = shell("messages_settings_ar", chatsAr, Locale.forLanguageTag("ar"), rtl = true, dark = false, tab = MessagesTab.Settings)

    @Test fun thread_en() = thread("messages_thread_en", en = true, rtl = false, dark = false)

    @Config(qualifiers = "+ar")
    @Test fun thread_ar() = thread("messages_thread_ar", en = false, rtl = true, dark = false)

    @Config(qualifiers = "+ar-night")
    @Test fun thread_ar_dark() = thread("messages_thread_ar_dark", en = false, rtl = true, dark = true)

    private fun shell(name: String, chats: List<org.hogwarts.android.feature.messaging.domain.model.ChatSummary>, locale: Locale, rtl: Boolean, dark: Boolean, tab: MessagesTab = MessagesTab.Chats) =
        capture(name, rtl, dark) {
            MessagesShellContent(
                state = ShellUiState(chats = chats, viewer = viewer, loaded = true, tab = tab),
                showNotice = false,
                onSelectTab = {},
                chatActions = ChatListActions(),
                onOpenProfile = {},
                onOpenNotifications = {},
                onOpenDashboard = {},
                format = format(locale),
            )
        }

    private fun thread(name: String, en: Boolean, rtl: Boolean, dark: Boolean) {
        val t = { s: String -> Instant.parse(s) }
        val other = "u-yusuf"
        val messages = if (en) listOf(
            message("m1", "c", ME, "Good morning Mr Yusuf, how are you? Omar joins our team this term and runs the reading programme", t("2026-09-09T10:30:00Z"), status = "read"),
            message("m2", "c", other, "Good morning, and welcome", t("2026-09-11T00:11:00Z")),
            message("m3", "c", other, "A warm welcome to Omar", t("2026-09-11T00:11:30Z")),
            message("m4", "c", other, "Thank you", t("2026-09-11T00:11:40Z")),
            message("m5", "c", ME, "See you at nine", t("2026-09-14T09:20:00Z"), status = "failed"),
        ) else listOf(
            message("m1", "c", ME, "صباح الخير\nأهلًا أستاذ يوسف، كيف حالك؟ عمر ينضم إلى فريقنا هذا الفصل ويتولى برنامج القراءة", t("2026-09-09T10:30:00Z"), status = "read"),
            message("m2", "c", other, "صباح النور", t("2026-09-11T00:11:00Z")),
            message("m3", "c", other, "أهلًا وسهلًا بعمر", t("2026-09-11T00:11:30Z")),
            message("m4", "c", other, "مرحبًا بكم", t("2026-09-11T00:11:40Z")),
            message("m5", "c", ME, "نلتقي الساعة التاسعة", t("2026-09-14T09:20:00Z"), status = "sending"),
        )
        val conversation = chat("c", if (en) "Yusuf Karam" else "يوسف كرم")
        capture(name, rtl, dark) {
            ThreadContent(
                state = ThreadUiState("c", conversation, messages, ME, loaded = true, hasMore = false),
                draft = "",
                onDraftChange = {},
                onSend = {},
                onBack = {},
                onRetry = {},
                onLoadOlder = {},
                onActivity = {},
                format = format(if (en) Locale.ENGLISH else Locale.forLanguageTag("ar")),
            )
        }
    }

    private fun capture(name: String, rtl: Boolean, dark: Boolean, content: @Composable () -> Unit) {
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) { content() }
            }
        }
    }
}
