package org.hogwarts.android.feature.dashboard.ui.components

import org.hogwarts.android.core.data.tenant.UserRole

/**
 * Stable identifier for each home-grid tile. Used by [HomeTileSpec.id] for
 * role-based filtering ([HomeTileVisibility]) and server-driven ordering
 * (matched against `DashboardUiState.enabledModules` via [serverName]).
 *
 * Adding a tile here is a one-line change that propagates through filtering,
 * ordering, and any future analytics/personalization that keys off the same
 * identifier. Server names mirror the hogwarts module taxonomy in
 * `src/components/template/platform-sidebar/config.ts`.
 */
enum class HomeTileId(val serverName: String) {
    Grades("grades"),
    Fees("fees"),
    Stream("stream"),
    Subjects("subjects"),
    Settings("settings"),
    AtomStudio("atom-studio"),
    Notifications("notifications"),
    Exams("exams"),
    Assignments("assignments"),
    Library("library"),
    Events("events"),
    Profile("profile"),
    Students("students"),
    Attendance("attendance"),
    Schedule("schedule"),
    Messages("messages"),
    Announcements("announcements")
}

/**
 * Per-role visibility map for home-grid tiles. Mirrors the hogwarts platform
 * sidebar matrix (`src/components/template/platform-sidebar/config.ts`).
 *
 * Today the local [UserRole] enum carries 5 values; the server returns 8
 * (DEVELOPER, ADMIN, TEACHER, STUDENT, GUARDIAN, ACCOUNTANT, STAFF, USER).
 * The mapping below is structured so that E02.S01 (8-role expansion) only
 * needs to add new entries; existing entries don't need to change.
 *
 * Mapping rationale:
 * - `SUPER_ADMIN` ≈ server `DEVELOPER`: sees everything including `AtomStudio`
 * - `ADMIN` sees everything except `AtomStudio` (dev-only)
 * - `TEACHER` sees teaching tiles + `Students` roster + `Assignments` marking;
 *   no `Fees` (teachers aren't billed) or `AtomStudio`
 * - `STUDENT` sees own academics + comms + profile; no admin/roster tiles
 * - `GUARDIAN` sees child-scoped views (the destination screen does the
 *   child-vs-self routing) + payments + comms; no marking/admin tiles
 */
object HomeTileVisibility {

    private val student: Set<HomeTileId> = setOf(
        HomeTileId.Grades, HomeTileId.Fees, HomeTileId.Stream, HomeTileId.Subjects,
        HomeTileId.Settings, HomeTileId.Notifications, HomeTileId.Exams,
        HomeTileId.Library, HomeTileId.Events, HomeTileId.Profile,
        HomeTileId.Attendance, HomeTileId.Schedule, HomeTileId.Messages,
        HomeTileId.Announcements
    )

    private val teacher: Set<HomeTileId> = setOf(
        HomeTileId.Grades, HomeTileId.Stream, HomeTileId.Subjects, HomeTileId.Settings,
        HomeTileId.Notifications, HomeTileId.Exams, HomeTileId.Assignments,
        HomeTileId.Library, HomeTileId.Events, HomeTileId.Profile,
        HomeTileId.Students, HomeTileId.Attendance, HomeTileId.Schedule,
        HomeTileId.Messages, HomeTileId.Announcements
    )

    private val guardian: Set<HomeTileId> = setOf(
        HomeTileId.Grades, HomeTileId.Fees, HomeTileId.Settings,
        HomeTileId.Notifications, HomeTileId.Events, HomeTileId.Profile,
        HomeTileId.Attendance, HomeTileId.Schedule, HomeTileId.Messages,
        HomeTileId.Announcements
    )

    private val admin: Set<HomeTileId> = HomeTileId.values().toSet() - HomeTileId.AtomStudio

    private val superAdmin: Set<HomeTileId> = HomeTileId.values().toSet()

    private val byRole: Map<UserRole, Set<HomeTileId>> = mapOf(
        UserRole.STUDENT to student,
        UserRole.TEACHER to teacher,
        UserRole.GUARDIAN to guardian,
        UserRole.ADMIN to admin,
        UserRole.SUPER_ADMIN to superAdmin
    )

    /**
     * Returns the set of tile IDs visible to [role]. Unknown roles get an
     * empty set (fail-closed: a tenant misconfiguration shouldn't leak admin
     * tiles).
     */
    fun tilesFor(role: UserRole): Set<HomeTileId> = byRole[role] ?: emptySet()
}

/**
 * Filters [tiles] to only those visible to [role], then reorders by
 * [enabledModules] when the server has supplied a manifest.
 *
 * Ordering rule: tiles whose [HomeTileId.serverName] appears in
 * [enabledModules] are placed first in the order specified by the server;
 * remaining visible tiles keep their original `buildHomeTiles` order
 * afterwards. This lets the server drift a tile up the grid without the
 * client knowing about new modules ahead of time.
 *
 * If [enabledModules] is empty, only the role filter is applied — keeps the
 * grid usable when the server endpoint (`/api/mobile/dashboard`, E08.S06)
 * hasn't shipped yet.
 */
fun List<HomeTileSpec>.filterAndOrderForRole(
    role: UserRole,
    enabledModules: List<String>
): List<HomeTileSpec> {
    val visible = HomeTileVisibility.tilesFor(role)
    val filtered = filter { it.id in visible }
    if (enabledModules.isEmpty()) return filtered

    val orderByName = enabledModules
        .map { it.lowercase() }
        .withIndex()
        .associate { (i, name) -> name to i }

    val (ordered, rest) = filtered.partition { it.id.serverName.lowercase() in orderByName }
    return ordered.sortedBy { orderByName[it.id.serverName.lowercase()] ?: Int.MAX_VALUE } + rest
}
