package org.hogwarts.android.feature.dashboard.ui.components

import org.hogwarts.android.core.data.tenant.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Locks in the role → visible-tile matrix and the server-driven ordering rules
 * for the home grid. Any future tile addition or role permission change must
 * update this matrix explicitly.
 */
class HomeTileVisibilityTest {

    // ---- role visibility ----

    @Test
    fun `student does not see AtomStudio, Students roster, or Assignments`() {
        val visible = HomeTileVisibility.tilesFor(UserRole.STUDENT)
        assertFalse(HomeTileId.AtomStudio in visible)
        assertFalse(HomeTileId.Students in visible)
        assertFalse(HomeTileId.Assignments in visible)
    }

    @Test
    fun `student sees core academic + comms tiles`() {
        val visible = HomeTileVisibility.tilesFor(UserRole.STUDENT)
        assertTrue(HomeTileId.Grades in visible)
        assertTrue(HomeTileId.Attendance in visible)
        assertTrue(HomeTileId.Exams in visible)
        assertTrue(HomeTileId.Fees in visible)
        assertTrue(HomeTileId.Notifications in visible)
        assertTrue(HomeTileId.Messages in visible)
    }

    @Test
    fun `teacher sees Students roster and Assignments but not Fees or AtomStudio`() {
        val visible = HomeTileVisibility.tilesFor(UserRole.TEACHER)
        assertTrue(HomeTileId.Students in visible)
        assertTrue(HomeTileId.Assignments in visible)
        assertFalse(HomeTileId.Fees in visible)
        assertFalse(HomeTileId.AtomStudio in visible)
    }

    @Test
    fun `guardian sees child-scoped views, fees, and comms`() {
        val visible = HomeTileVisibility.tilesFor(UserRole.GUARDIAN)
        assertTrue(HomeTileId.Attendance in visible)
        assertTrue(HomeTileId.Grades in visible)
        assertTrue(HomeTileId.Fees in visible)
        assertTrue(HomeTileId.Messages in visible)
        assertTrue(HomeTileId.Announcements in visible)
        // Roster + dev tools are admin/teacher concerns
        assertFalse(HomeTileId.Students in visible)
        assertFalse(HomeTileId.AtomStudio in visible)
        assertFalse(HomeTileId.Assignments in visible)
    }

    @Test
    fun `admin sees every tile except AtomStudio`() {
        val visible = HomeTileVisibility.tilesFor(UserRole.ADMIN)
        val expected = HomeTileId.values().toSet() - HomeTileId.AtomStudio
        assertEquals(expected, visible)
    }

    @Test
    fun `super admin sees every tile including AtomStudio`() {
        val visible = HomeTileVisibility.tilesFor(UserRole.SUPER_ADMIN)
        assertEquals(HomeTileId.values().toSet(), visible)
    }

    // ---- filterAndOrderForRole ----

    @Test
    fun `filterAndOrderForRole drops tiles the role can't see`() {
        val tiles = HomeTileId.values().map { tile(it) }
        val filtered = tiles.filterAndOrderForRole(UserRole.STUDENT, emptyList())
        assertFalse(filtered.any { it.id == HomeTileId.AtomStudio })
        assertFalse(filtered.any { it.id == HomeTileId.Students })
    }

    @Test
    fun `filterAndOrderForRole keeps default order when enabledModules is empty`() {
        val original = listOf(
            tile(HomeTileId.Grades),
            tile(HomeTileId.Fees),
            tile(HomeTileId.Messages)
        )
        val ordered = original.filterAndOrderForRole(UserRole.STUDENT, emptyList())
        assertEquals(listOf(HomeTileId.Grades, HomeTileId.Fees, HomeTileId.Messages), ordered.map { it.id })
    }

    @Test
    fun `filterAndOrderForRole honors enabledModules ordering for matched tiles`() {
        val original = listOf(
            tile(HomeTileId.Grades),
            tile(HomeTileId.Fees),
            tile(HomeTileId.Messages),
            tile(HomeTileId.Notifications)
        )
        // Server says: messages first, fees second; grades + notifications get
        // appended in their original order.
        val ordered = original.filterAndOrderForRole(
            role = UserRole.STUDENT,
            enabledModules = listOf("messages", "fees")
        )
        assertEquals(
            listOf(HomeTileId.Messages, HomeTileId.Fees, HomeTileId.Grades, HomeTileId.Notifications),
            ordered.map { it.id }
        )
    }

    @Test
    fun `filterAndOrderForRole is case-insensitive against enabledModules`() {
        val original = listOf(tile(HomeTileId.Grades), tile(HomeTileId.Fees))
        val ordered = original.filterAndOrderForRole(
            role = UserRole.STUDENT,
            enabledModules = listOf("FEES", "GRADES")
        )
        assertEquals(listOf(HomeTileId.Fees, HomeTileId.Grades), ordered.map { it.id })
    }

    // Stub tile used purely for filter/order tests — visuals aren't exercised.
    private fun tile(id: HomeTileId): HomeTileSpec = HomeTileSpec(
        id = id,
        labelRes = 0,
        icon = androidx.compose.material.icons.Icons.Filled.Science,
        background = androidx.compose.ui.graphics.Color.Unspecified,
        onClick = {}
    )
}
