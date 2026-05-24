package org.hogwarts.android.feature.messaging.domain.config

import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.messaging.domain.model.ContactCategory
import org.hogwarts.android.feature.messaging.domain.model.SidebarFilter

/** Which contact categories each role sees, in display order.
 *  Mirrors hogwarts/src/components/school-dashboard/messaging/contacts/config.ts:7-22. */
val ROLE_CATEGORIES: Map<UserRole, List<ContactCategory>> = mapOf(
    UserRole.SUPER_ADMIN to listOf(
        ContactCategory.ADMIN,
        ContactCategory.TEACHERS,
        ContactCategory.STUDENTS,
        ContactCategory.PARENTS,
        ContactCategory.STAFF,
        ContactCategory.ACCOUNTANTS,
    ),
    UserRole.ADMIN to listOf(
        ContactCategory.TEACHERS,
        ContactCategory.STUDENTS,
        ContactCategory.PARENTS,
        ContactCategory.STAFF,
        ContactCategory.ACCOUNTANTS,
    ),
    UserRole.TEACHER to listOf(
        ContactCategory.MY_STUDENTS,
        ContactCategory.PARENTS,
        ContactCategory.TEACHERS,
        ContactCategory.STAFF,
        ContactCategory.ADMIN,
    ),
    UserRole.STUDENT to listOf(
        ContactCategory.MY_TEACHERS,
        ContactCategory.CLASSMATES,
        ContactCategory.ADMIN,
    ),
    UserRole.GUARDIAN to listOf(
        ContactCategory.MY_CHILDREN_TEACHERS,
        ContactCategory.ADMIN,
    ),
)

/** Sidebar filter chips: All, Unread, [role categories], Favourites.
 *  Mirrors getSidebarFilters() in contacts/config.ts:39-52. */
fun getSidebarFilters(role: UserRole?): List<SidebarFilter> {
    val categories = role?.let { ROLE_CATEGORIES[it] } ?: emptyList()
    return buildList {
        add(SidebarFilter.All)
        add(SidebarFilter.Unread)
        categories.forEach { add(SidebarFilter.Category(it)) }
        add(SidebarFilter.Favourites)
    }
}

const val MAX_CONTACTS_PER_CATEGORY = 50
