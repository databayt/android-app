package org.hogwarts.android.shell

import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.attendance.navigation.Attendance
import org.hogwarts.android.feature.fees.navigation.Fees
import org.hogwarts.android.feature.guardian.navigation.GuardianChildren
import org.hogwarts.android.feature.messaging.navigation.Messaging
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HrefRouterTest {
    @Test fun `menu paths map to native screens`() {
        assertEquals(Attendance, routeForHref("/attendance", UserRole.TEACHER))
        assertEquals(Messaging, routeForHref("/messages", UserRole.STUDENT))
    }

    @Test fun `nested paths use the longest matching menu item`() {
        assertEquals(Fees, routeForHref("/finance/invoice?status=overdue", UserRole.ACCOUNTANT))
    }

    @Test fun `guardian my-children door opens the parent portal`() {
        assertEquals(GuardianChildren, routeForHref("/parents", UserRole.GUARDIAN))
        assertNull(routeForHref("/parents", UserRole.ADMIN))
    }

    @Test fun `pages without a native screen hand off to the web`() {
        assertNull(routeForHref("/school", UserRole.ADMIN))
        assertNull(routeForHref("/staff", UserRole.ADMIN))
        assertNull(routeForHref("/assignments", UserRole.TEACHER))
    }
}
