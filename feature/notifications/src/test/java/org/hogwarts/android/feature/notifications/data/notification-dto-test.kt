package org.hogwarts.android.feature.notifications.data

import kotlinx.serialization.json.Json
import org.hogwarts.android.feature.notifications.data.remote.dto.NotificationListResponse
import org.hogwarts.android.feature.notifications.data.repository.toPage
import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel
import org.hogwarts.android.feature.notifications.domain.model.NotificationKind
import org.hogwarts.android.feature.notifications.domain.model.NotificationPriority
import org.hogwarts.android.feature.notifications.domain.model.PreferenceMatrix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    /** The route's real shape (snake_case), with invented content. */
    private val body = """
        {"data":[
          {"id":"n-1","type":"attendance_alert","priority":"high","title":"Absence alert","body":"A student missed two periods.",
           "metadata":{"entityType":"attendance","url":"/attendance"},"is_read":false,"read_at":null,
           "created_at":"2026-09-14T08:00:00.000Z","actor_name":null,"actor_avatar":null},
          {"id":"n-2","type":"fee_paid","priority":"normal","title":"Fee payment received","body":"Thank you.",
           "metadata":null,"is_read":true,"read_at":"2026-09-13T10:00:00.000Z",
           "created_at":"2026-09-13T09:00:00.000Z","actor_name":null,"actor_avatar":null}
        ],"total":41,"unread_count":7,"page":1,"per_page":20}
    """.trimIndent()

    @Test
    fun `decodes the list and lifts metadata url`() {
        val page = json.decodeFromString(NotificationListResponse.serializer(), body).toPage()
        assertEquals(3, page.totalPages)
        assertEquals(7, page.unreadCount)
        val first = page.items[0]
        assertEquals("/attendance", first.url)
        assertEquals(NotificationPriority.High, first.priority)
        assertFalse(first.isRead)
        assertNull(page.items[1].url)
        assertTrue(page.items[1].isRead)
    }

    @Test
    fun `preferences default to in-app only, like the web form`() {
        val empty = PreferenceMatrix.from(emptyList())
        assertTrue(empty.isOn(NotificationKind.Announcement, NotificationChannel.InApp))
        assertFalse(empty.isOn(NotificationKind.Announcement, NotificationChannel.Email))

        val stored = PreferenceMatrix.from(listOf(Triple("fee_due", "whatsapp", true), Triple("fee_due", "in_app", false), Triple("unknown", "push", true)))
        assertTrue(stored.isOn(NotificationKind.FeeDue, NotificationChannel.WhatsApp))
        assertFalse(stored.isOn(NotificationKind.FeeDue, NotificationChannel.InApp))
    }
}
