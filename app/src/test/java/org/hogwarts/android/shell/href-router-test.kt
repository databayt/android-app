package org.hogwarts.android.shell

import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.announcements.navigation.AnnouncementDetail
import org.hogwarts.android.feature.announcements.navigation.Announcements
import org.hogwarts.android.feature.attendance.navigation.Attendance
import org.hogwarts.android.feature.exams.navigation.ExamDetail
import org.hogwarts.android.feature.exams.navigation.ExamsUpcoming
import org.hogwarts.android.feature.exams.navigation.QuestionBank
import org.hogwarts.android.feature.guardian.navigation.GuardianChildren
import org.hogwarts.android.feature.messaging.navigation.Messaging
import org.hogwarts.android.feature.notifications.navigation.NotificationPreferences
import org.hogwarts.android.feature.notifications.navigation.NotificationsUnread
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HrefRouterTest {
    @Test fun `menu paths map to native screens`() {
        assertEquals(Attendance, routeForHref("/attendance", UserRole.TEACHER))
        assertEquals(Messaging, routeForHref("/messages", UserRole.STUDENT))
    }

    @Test fun `deeper pages hand off unless a native screen mirrors them`() {
        assertNull(routeForHref("/finance/invoice?status=overdue", UserRole.ACCOUNTANT))
        assertNull(routeForHref("/exams/result", UserRole.STUDENT))
        assertEquals(ExamsUpcoming, routeForHref("/exams/upcoming", UserRole.STUDENT))
        assertEquals(ExamDetail("exam-1"), routeForHref("/exams/exam-1", UserRole.STUDENT))
        assertEquals(QuestionBank, routeForHref("/exams/qbank", UserRole.TEACHER))
        assertNull(routeForHref("/exams/qbank", UserRole.STUDENT))
        assertEquals(Announcements, routeForHref("/announcements", UserRole.STUDENT))
        assertEquals(AnnouncementDetail("ann-1"), routeForHref("/announcements/ann-1", UserRole.STUDENT))
        assertNull(routeForHref("/announcements/templates", UserRole.ADMIN))
        assertNull(routeForHref("/announcements/archived", UserRole.ADMIN))
        assertEquals(NotificationsUnread, routeForHref("/notifications/unread", UserRole.STUDENT))
        assertEquals(NotificationPreferences, routeForHref("/notifications/preferences", UserRole.STUDENT))
    }

    @Test fun `guardian my-children door opens the parent portal`() {
        assertEquals(GuardianChildren, routeForHref("/parents", UserRole.GUARDIAN))
        assertNull(routeForHref("/parents", UserRole.ADMIN))
    }

    @Test fun `pages without a native screen hand off to the web`() {
        assertNull(routeForHref("/school", UserRole.ADMIN))
        assertNull(routeForHref("/staff", UserRole.ADMIN))
        assertNull(routeForHref("/assignments", UserRole.TEACHER))
        assertNull(routeForHref("/timetable", UserRole.ADMIN))
        assertNull(routeForHref("/timetable/generate", UserRole.ADMIN))
    }
}
