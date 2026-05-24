package org.hogwarts.android.navigation

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeepLinkHandlerTest {

    @Test
    fun `parse attendance deep link`() {
        val uri = Uri.parse("hogwarts://app/attendance?studentId=student-1")
        val result = DeepLinkHandler.parseUri(uri)

        assertNotNull(result)
        assertTrue(result is DeepLinkDestination.Attendance)
        assertEquals("student-1", (result as DeepLinkDestination.Attendance).studentId)
    }

    @Test
    fun `parse grades deep link`() {
        val uri = Uri.parse("hogwarts://app/grades?studentId=student-1")
        val result = DeepLinkHandler.parseUri(uri)

        assertNotNull(result)
        assertTrue(result is DeepLinkDestination.Grades)
        assertEquals("student-1", (result as DeepLinkDestination.Grades).studentId)
    }

    @Test
    fun `parse chat deep link with conversation id`() {
        val uri = Uri.parse("hogwarts://app/chat/conv-123")
        val result = DeepLinkHandler.parseUri(uri)

        assertNotNull(result)
        assertTrue(result is DeepLinkDestination.Chat)
        assertEquals("conv-123", (result as DeepLinkDestination.Chat).conversationId)
    }

    @Test
    fun `parse fees deep link`() {
        val uri = Uri.parse("hogwarts://app/fees?feeId=fee-1")
        val result = DeepLinkHandler.parseUri(uri)

        assertNotNull(result)
        assertTrue(result is DeepLinkDestination.Fees)
        assertEquals("fee-1", (result as DeepLinkDestination.Fees).feeId)
    }

    @Test
    fun `parse timetable deep link`() {
        val uri = Uri.parse("hogwarts://app/timetable")
        val result = DeepLinkHandler.parseUri(uri)

        assertNotNull(result)
        assertTrue(result is DeepLinkDestination.Timetable)
    }

    @Test
    fun `parse unknown path returns null`() {
        val uri = Uri.parse("hogwarts://app/unknown")
        val result = DeepLinkHandler.parseUri(uri)

        assertNull(result)
    }

    @Test
    fun `parse empty path returns null`() {
        val uri = Uri.parse("hogwarts://app/")
        val result = DeepLinkHandler.parseUri(uri)

        assertNull(result)
    }

    @Test
    fun `create deep link uri for attendance`() {
        val uri = DeepLinkHandler.createDeepLinkUri(
            DeepLinkDestination.Attendance(studentId = "student-1")
        )
        assertEquals("hogwarts", uri.scheme)
        assertEquals("app", uri.authority)
        assertEquals("/attendance", uri.path)
        assertEquals("student-1", uri.getQueryParameter("studentId"))
    }

    @Test
    fun `create deep link uri for chat`() {
        val uri = DeepLinkHandler.createDeepLinkUri(
            DeepLinkDestination.Chat(conversationId = "conv-456")
        )
        assertEquals("hogwarts", uri.scheme)
        assertEquals("/chat/conv-456", uri.path)
    }

    @Test
    fun `parse announcements deep link`() {
        val uri = Uri.parse("hogwarts://app/announcements/ann-1")
        val result = DeepLinkHandler.parseUri(uri)

        assertNotNull(result)
        assertTrue(result is DeepLinkDestination.Announcements)
        assertEquals("ann-1", (result as DeepLinkDestination.Announcements).announcementId)
    }

    @Test
    fun `parse exams deep link`() {
        val uri = Uri.parse("hogwarts://app/exams/exam-1")
        val result = DeepLinkHandler.parseUri(uri)

        assertNotNull(result)
        assertTrue(result is DeepLinkDestination.Exams)
        assertEquals("exam-1", (result as DeepLinkDestination.Exams).examId)
    }

    @Test
    fun `parse profile deep link`() {
        val uri = Uri.parse("hogwarts://app/profile")
        val result = DeepLinkHandler.parseUri(uri)

        assertNotNull(result)
        assertTrue(result is DeepLinkDestination.Profile)
    }

    @Test
    fun `parse settings deep link`() {
        val uri = Uri.parse("hogwarts://app/settings")
        val result = DeepLinkHandler.parseUri(uri)

        assertNotNull(result)
        assertTrue(result is DeepLinkDestination.Settings)
    }

    @Test
    fun `parse notifications deep link`() {
        val uri = Uri.parse("hogwarts://app/notifications")
        val result = DeepLinkHandler.parseUri(uri)

        assertNotNull(result)
        assertTrue(result is DeepLinkDestination.Notifications)
    }
}
