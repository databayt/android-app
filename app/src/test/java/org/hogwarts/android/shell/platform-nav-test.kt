package org.hogwarts.android.shell

import org.hogwarts.android.core.data.tenant.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the port of hogwarts platform-sidebar/config.ts. When the web adds,
 * removes or re-roles a menu item this test must be updated in the same change.
 */
class PlatformNavTest {

    private fun keys(role: UserRole, modules: List<String>? = null) =
        visibleNav(role, modules).map { it.key }

    @Test
    fun `order and hrefs match the web config`() {
        assertEquals(
            listOf(
                "dashboard", "school", "sales", "announcements", "finance", "grades", "subjects",
                "parents", "admission", "students", "teachers", "classrooms", "exams", "events",
                "attendance", "timetable", "library", "transportation", "transportationFees",
                "myTransportation", "assignments", "parentPortal", "transportationTrips", "lumos",
                "liveClasses", "compliance", "profile", "settings", "charts", "stats", "billing",
            ),
            platformNav.map { it.key },
        )
        assertEquals("/transportation/me", platformNav.first { it.key == "myTransportation" }.href)
        assertEquals("/parent", platformNav.first { it.key == "parentPortal" }.href)
    }

    @Test
    fun `student menu`() {
        assertEquals(
            listOf(
                "dashboard", "announcements", "finance", "grades", "subjects", "exams", "attendance",
                "timetable", "library", "myTransportation", "assignments", "lumos", "liveClasses",
                "profile", "settings",
            ),
            keys(UserRole.STUDENT),
        )
    }

    @Test
    fun `every role that does homework sees the one assignments item`() {
        assertEquals("/assignments", platformNav.first { it.key == "assignments" }.href)
        assertEquals(1, platformNav.count { it.key == "assignments" })
        assert(platformNav.none { it.key == "myAssignments" })
        for (r in listOf(UserRole.ADMIN, UserRole.STAFF, UserRole.TEACHER, UserRole.STUDENT, UserRole.GUARDIAN)) {
            assert("assignments" in keys(r)) { "$r should see assignments" }
        }
        assert("assignments" !in keys(UserRole.ACCOUNTANT))
    }

    @Test
    fun `guardian menu`() {
        assertEquals(
            listOf(
                "dashboard", "announcements", "finance", "grades", "exams", "attendance", "library",
                "myTransportation", "assignments", "parentPortal", "lumos", "liveClasses", "profile", "settings",
            ),
            keys(UserRole.GUARDIAN),
        )
    }

    @Test
    fun `accountant menu`() {
        assertEquals(
            listOf(
                "dashboard", "announcements", "finance", "admission", "library", "transportationFees",
                "lumos", "liveClasses", "profile", "settings",
            ),
            keys(UserRole.ACCOUNTANT),
        )
    }

    @Test
    fun `enabled modules hide toggleable items but never core ones`() {
        assertEquals(
            listOf("dashboard", "school", "exams", "profile", "settings"),
            keys(UserRole.ADMIN, modules = listOf("exams")),
        )
    }

    @Test
    fun `unknown or missing role sees nothing`() {
        assertEquals(emptyList<String>(), keys(UserRole.UNKNOWN))
        assertEquals(emptyList<String>(), visibleNav(null, null))
    }
}
