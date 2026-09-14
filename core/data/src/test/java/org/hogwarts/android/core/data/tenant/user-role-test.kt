package org.hogwarts.android.core.data.tenant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRoleTest {

    @Test
    fun `parses every Prisma role`() {
        val prismaRoles = listOf(
            "DEVELOPER", "ADMIN", "TEACHER", "STUDENT",
            "GUARDIAN", "ACCOUNTANT", "STAFF", "USER"
        )
        prismaRoles.forEach { wire ->
            assertEquals(wire, UserRole.fromWire(wire).name)
        }
    }

    @Test
    fun `maps legacy aliases`() {
        assertEquals(UserRole.DEVELOPER, UserRole.fromWire("SUPER_ADMIN"))
        assertEquals(UserRole.ADMIN, UserRole.fromWire("PRINCIPAL"))
        assertEquals(UserRole.GUARDIAN, UserRole.fromWire("parent"))
    }

    @Test
    fun `never throws on unexpected input`() {
        assertEquals(UserRole.UNKNOWN, UserRole.fromWire(null))
        assertEquals(UserRole.UNKNOWN, UserRole.fromWire(""))
        assertEquals(UserRole.UNKNOWN, UserRole.fromWire("JANITOR"))
        assertEquals(UserRole.TEACHER, UserRole.fromWire(" teacher "))
    }

    @Test
    fun `admin check covers school admin and developer only`() {
        assertTrue(UserRole.ADMIN.isAdmin)
        assertTrue(UserRole.DEVELOPER.isAdmin)
        assertFalse(UserRole.STAFF.isAdmin)
        assertFalse(UserRole.UNKNOWN.isAdmin)
    }
}
