package org.hogwarts.android.feature.messaging.ui

import org.hogwarts.android.feature.messaging.FakeMessagingRepository.Companion.ME
import org.hogwarts.android.feature.messaging.chat
import org.hogwarts.android.feature.messaging.message
import org.hogwarts.android.feature.messaging.ui.chats.ChatFilter
import org.hogwarts.android.feature.messaging.ui.chats.PreviewLeading
import org.hogwarts.android.feature.messaging.ui.chats.RowLabels
import org.hogwarts.android.feature.messaging.ui.chats.filterChats
import org.hogwarts.android.feature.messaging.ui.chats.toRowData
import org.hogwarts.android.feature.messaging.ui.format.MessagingFormat
import org.hogwarts.android.feature.messaging.ui.format.avatarColorFor
import org.hogwarts.android.feature.messaging.ui.thread.AdaptLabels
import org.hogwarts.android.feature.messaging.ui.thread.BubbleStatus
import org.hogwarts.android.feature.messaging.ui.thread.ChatItem
import org.hogwarts.android.feature.messaging.ui.thread.Side
import org.hogwarts.android.feature.messaging.ui.thread.toChatItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

class MessagingAdaptTest {

    private val today = LocalDate.parse("2026-09-14")
    private val en = MessagingFormat(Locale.ENGLISH, ZoneOffset.UTC, { today })
    private val ar = MessagingFormat(Locale.forLanguageTag("ar"), ZoneOffset.UTC, { today })
    private val rowLabels = RowLabels("Group", "Unknown", "Yesterday")
    private val adapt = AdaptLabels("Today", "Yesterday", "Deleted", "Photo", "Video", "Voice message", "Document", "Attachment", "User")

    @Test
    fun `list stamps follow ios-chat-list, Latin digits under Arabic as on the web`() {
        assertEquals("09:05", en.listStamp(Instant.parse("2026-09-14T09:05:00Z"), "Yesterday"))
        assertEquals("Yesterday", en.listStamp(Instant.parse("2026-09-13T23:00:00Z"), "Yesterday"))
        assertEquals("09/10", en.listStamp(Instant.parse("2026-09-10T10:00:00Z"), "Yesterday"))
        assertEquals("10\u200F/09", ar.listStamp(Instant.parse("2026-09-10T10:00:00Z"), "أمس"))
    }

    @Test
    fun `day pills read like chat-adapt`() {
        assertEquals("September 10", en.daySeparator(Instant.parse("2026-09-10T10:00:00Z"), "Today", "Yesterday"))
        assertEquals("10 سبتمبر", ar.daySeparator(Instant.parse("2026-09-10T10:00:00Z"), "اليوم", "أمس"))
        assertEquals("January 3, 2025", en.daySeparator(Instant.parse("2025-01-03T10:00:00Z"), "Today", "Yesterday"))
        assertEquals("Today", en.daySeparator(Instant.parse("2026-09-14T01:00:00Z"), "Today", "Yesterday"))
    }

    @Test
    fun `eastern arabic digits are one switch away`() {
        val eastern = MessagingFormat(Locale.forLanguageTag("ar"), ZoneOffset.UTC, { today }, easternArabicDigits = true)
        assertEquals("٠٩:٠٥", eastern.clock(Instant.parse("2026-09-14T09:05:00Z")))
    }

    @Test
    fun `avatar colours hash the same way as avatar-tsx`() {
        // Expected indices computed with the web's getAvatarColor in Node.
        val palette = listOf(0xFFCBF2EE, 0xFFE9E0FF, 0xFFFEF1D4, 0xFFFBD8DC)
        fun index(id: String) = palette.indexOf(avatarColorFor(id).background.value.toLong().ushr(32))
        assertEquals(0, index("conv-huda-farouk"))
        assertEquals(3, index("conv-year5-parents"))
        assertEquals(1, index("conv-omar-hassan"))
        assertEquals(1, index("a"))
        assertEquals(0, index("conversation-with-a-long-identifier-0123456789abcdef"))
    }

    @Test
    fun `rows tick my own last message and name the speaker in groups`() {
        val mine = chat("a", "Ali", last = "ok", lastSender = "Minerva", status = "read").toRowData("Minerva", en, rowLabels)
        assertEquals(PreviewLeading.CheckRead, mine.previewLeading)
        assertEquals("ok", mine.preview)

        val group = chat("g", null, type = "group", last = "hello", lastSender = "Sara").toRowData("Minerva", en, rowLabels)
        assertEquals("Group", group.name)
        assertEquals("Sara: hello", group.preview)
        assertNull(group.previewLeading)
    }

    @Test
    fun `filters match the web chips`() {
        val chats = listOf(
            chat("a", "Ali", unread = 1, at = "2026-09-14T08:00:00Z"),
            chat("b", "Class 5", type = "class", at = "2026-09-14T09:00:00Z"),
            chat("c", "Sara", pinned = true, at = "2026-09-14T07:00:00Z"),
        )
        assertEquals(listOf("b", "a", "c"), filterChats(chats, ChatFilter.All, "", rowLabels).map { it.id })
        assertEquals(listOf("a"), filterChats(chats, ChatFilter.Unread, "", rowLabels).map { it.id })
        assertEquals(listOf("b"), filterChats(chats, ChatFilter.Groups, "", rowLabels).map { it.id })
        assertEquals(listOf("c"), filterChats(chats, ChatFilter.Favourites, "", rowLabels).map { it.id })
        assertEquals(listOf("c"), filterChats(chats, ChatFilter.All, "sa", rowLabels).map { it.id })
    }

    @Test
    fun `thread items get day pills, run tails, group sender names and replies`() {
        val t = { s: String -> Instant.parse(s) }
        val messages = listOf(
            message("1", "c", "sara", "hi", t("2026-09-13T08:00:00Z"), senderName = "Sara"),
            message("2", "c", "sara", "there", t("2026-09-13T08:01:00Z"), senderName = "Sara"),
            message("3", "c", ME, "hello", t("2026-09-14T08:02:00Z"), status = "delivered"),
            message("4", "c", ME, "again", t("2026-09-14T08:03:00Z"), status = "failed", replyToId = "1"),
            message("5", "c", "sara", "", t("2026-09-14T08:04:00Z"), senderName = "Sara", contentType = "image"),
        )
        val items = toChatItems(messages, ME, isGroup = true, format = en, labels = adapt)

        assertEquals(listOf("Yesterday", "Today"), items.filterIsInstance<ChatItem.Date>().map { it.label })
        val first = items[1] as ChatItem.Text
        assertEquals("Sara", first.senderName)
        assertEquals(false, first.tail)
        val second = items[2] as ChatItem.Text
        assertNull(second.senderName)
        assertEquals(true, second.tail)
        val mine = items[4] as ChatItem.Text
        assertEquals(Side.Me, mine.side)
        assertEquals(BubbleStatus.Delivered, mine.status)
        val reply = items[5] as ChatItem.Reply
        assertEquals("Sara", reply.replySenderName)
        assertEquals("hi", reply.replyText)
        assertEquals(BubbleStatus.Failed, reply.status)
        assertEquals("Photo", (items[6] as ChatItem.Text).text)
    }
}
