package org.hogwarts.android.shell

import androidx.annotation.StringRes
import org.hogwarts.android.R
import org.hogwarts.android.core.data.tenant.UserRole

/**
 * Literal port of hogwarts `src/components/template/platform-sidebar/config.ts`
 * — the menu a role sees on the phone web. Order, keys, hrefs, roles and
 * `alwaysVisible` must stay identical; `platform-nav-test.kt` pins them.
 */
data class PlatformNavItem(
    val key: String,
    @StringRes val titleRes: Int,
    val href: String,
    val roles: Set<UserRole>,
    val alwaysVisible: Boolean = false,
)

private val ALL = setOf(
    UserRole.DEVELOPER, UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT,
    UserRole.GUARDIAN, UserRole.ACCOUNTANT, UserRole.STAFF, UserRole.USER,
)

private fun roles(vararg r: UserRole) = r.toSet()

val platformNav: List<PlatformNavItem> = listOf(
    PlatformNavItem("dashboard", R.string.menu_overview, "/dashboard", ALL, alwaysVisible = true),
    PlatformNavItem("school", R.string.menu_school, "/school", roles(UserRole.ADMIN, UserRole.DEVELOPER), alwaysVisible = true),
    PlatformNavItem("sales", R.string.menu_sales, "/sales", roles(UserRole.ADMIN, UserRole.DEVELOPER)),
    PlatformNavItem("announcements", R.string.menu_announcements, "/announcements", ALL),
    PlatformNavItem("finance", R.string.menu_finance, "/finance", ALL),
    PlatformNavItem("grades", R.string.menu_grades, "/grades", roles(UserRole.ADMIN, UserRole.STAFF, UserRole.TEACHER, UserRole.STUDENT, UserRole.GUARDIAN)),
    PlatformNavItem("subjects", R.string.menu_subjects, "/subjects", roles(UserRole.ADMIN, UserRole.STAFF, UserRole.TEACHER, UserRole.STUDENT)),
    PlatformNavItem("parents", R.string.menu_parents, "/parents", roles(UserRole.ADMIN, UserRole.STAFF)),
    PlatformNavItem("admission", R.string.menu_admission, "/admission", roles(UserRole.ADMIN, UserRole.STAFF, UserRole.ACCOUNTANT, UserRole.DEVELOPER)),
    PlatformNavItem("students", R.string.menu_students, "/students", roles(UserRole.ADMIN, UserRole.STAFF, UserRole.TEACHER)),
    PlatformNavItem("teachers", R.string.menu_teachers, "/teachers", roles(UserRole.ADMIN, UserRole.STAFF)),
    PlatformNavItem("classrooms", R.string.menu_classrooms, "/classrooms", roles(UserRole.ADMIN, UserRole.STAFF, UserRole.TEACHER)),
    PlatformNavItem("exams", R.string.menu_exams, "/exams", roles(UserRole.ADMIN, UserRole.STAFF, UserRole.TEACHER, UserRole.STUDENT, UserRole.GUARDIAN)),
    PlatformNavItem("events", R.string.menu_events, "/events", roles(UserRole.ADMIN, UserRole.STAFF, UserRole.TEACHER)),
    PlatformNavItem("attendance", R.string.menu_attendance, "/attendance", roles(UserRole.ADMIN, UserRole.STAFF, UserRole.TEACHER, UserRole.STUDENT, UserRole.GUARDIAN)),
    PlatformNavItem("timetable", R.string.menu_timetable, "/timetable", roles(UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT)),
    PlatformNavItem("library", R.string.menu_library, "/library", ALL),
    PlatformNavItem("transportation", R.string.menu_transportation, "/transportation", roles(UserRole.ADMIN, UserRole.STAFF, UserRole.DEVELOPER)),
    PlatformNavItem("transportationFees", R.string.menu_transport_fees, "/transportation/fees", roles(UserRole.ACCOUNTANT, UserRole.DEVELOPER)),
    PlatformNavItem("myTransportation", R.string.menu_my_transportation, "/transportation/me", roles(UserRole.STUDENT, UserRole.GUARDIAN)),
    PlatformNavItem("myAssignments", R.string.menu_my_assignments, "/my-assignments", roles(UserRole.STUDENT)),
    PlatformNavItem("parentPortal", R.string.menu_parent_portal, "/parent", roles(UserRole.GUARDIAN, UserRole.DEVELOPER)),
    PlatformNavItem("transportationTrips", R.string.menu_trips, "/transportation/trips", roles(UserRole.TEACHER)),
    PlatformNavItem("lumos", R.string.menu_lumos, "/lumos", ALL),
    PlatformNavItem("liveClasses", R.string.menu_live, "/live", ALL),
    PlatformNavItem("compliance", R.string.menu_compliance, "/compliance", roles(UserRole.ADMIN, UserRole.STAFF, UserRole.DEVELOPER)),
    PlatformNavItem("profile", R.string.menu_profile, "/profile", ALL, alwaysVisible = true),
    PlatformNavItem("settings", R.string.menu_settings, "/settings", ALL, alwaysVisible = true),
    PlatformNavItem("charts", R.string.menu_charts, "/charts", roles(UserRole.DEVELOPER)),
    PlatformNavItem("stats", R.string.menu_stats, "/stats", roles(UserRole.DEVELOPER)),
    PlatformNavItem("billing", R.string.menu_billing, "/billing", roles(UserRole.DEVELOPER)),
)

/**
 * The items a role sees, with the school's module toggles applied the way the
 * web sidebar applies them: `alwaysVisible || enabledModules == null || key in enabledModules`.
 */
fun visibleNav(role: UserRole?, enabledModules: List<String>?): List<PlatformNavItem> =
    platformNav.filter { item ->
        role != null && role in item.roles &&
            (item.alwaysVisible || enabledModules == null || item.key in enabledModules)
    }
