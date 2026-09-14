package org.hogwarts.android.feature.announcements.data

import kotlinx.serialization.json.Json
import org.hogwarts.android.feature.announcements.data.remote.dto.AnnouncementDto
import org.hogwarts.android.feature.announcements.data.remote.dto.AnnouncementListResponse
import org.hogwarts.android.feature.announcements.data.repository.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class AnnouncementDtoTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; isLenient = true }

    @Test
    fun `the pre-2026-09 list shape still decodes`() {
        val body = """
            {"data":[{"id":"ann-1","title":"Fictional notice","content":"Body","priority":"high",
            "published_at":"2026-09-10T09:00:00.000Z","expires_at":null,"author_name":null,"author_avatar":null}],
            "total":1,"page":1,"per_page":20}
        """.trimIndent()
        val page = json.decodeFromString(AnnouncementListResponse.serializer(), body).toDomain()
        val item = page.items.single()
        assertEquals("school", item.scope)
        assertTrue(item.isPublished)
        assertTrue(item.isNotablePriority)
        assertEquals(Instant.parse("2026-09-10T09:00:00Z"), item.createdAt)
        assertFalse(page.hasMore)
    }

    @Test
    fun `the additive fields map onto the model`() {
        val body = """
            {"id":"ann-2","title":"Draft notice","content":"","priority":"normal","published_at":null,
            "scope":"role","target_role":"TEACHER","class_id":null,"is_published":false,"is_pinned":true,
            "is_featured":false,"lang":"ar","created_at":"2026-09-11T08:00:00.000Z",
            "updated_at":"2026-09-12T08:00:00.000Z","is_read":true,"read_count":3}
        """.trimIndent()
        val item = json.decodeFromString(AnnouncementDto.serializer(), body).toDomain()
        assertEquals("role", item.scope)
        assertEquals("TEACHER", item.targetRole)
        assertFalse(item.isPublished)
        assertTrue(item.isPinned && item.isRead)
        assertEquals(Instant.parse("2026-09-12T08:00:00Z"), item.updatedAt)
    }
}
