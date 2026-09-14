package org.hogwarts.android.feature.attendance.ui.quick

import org.hogwarts.android.feature.attendance.domain.model.MarkStatus
import org.hogwarts.android.feature.attendance.domain.model.QuickSection
import org.hogwarts.android.feature.attendance.domain.model.RosterStudent
import java.time.LocalDate

data class QuickAttendanceUiState(
    val today: LocalDate,
    val isSchoolDay: Boolean = true,
    /** null while the sections load. */
    val sections: List<QuickSection>? = null,
    val loadFailed: Boolean = false,
    val selectedSectionId: String? = null,
    /** null while the selected section's roster loads. */
    val roster: List<RosterStudent>? = null,
    val search: String = "",
    val saving: Boolean = false,
    val saved: SavedPanel? = null,
    /** The last save got a refusal (or no verdict); the server's code when it gave one. */
    val saveError: SaveError? = null,
) {
    val counts: MarkCounts
        get() = MarkCounts(
            present = roster.orEmpty().count { it.status == MarkStatus.Present },
            absent = roster.orEmpty().count { it.status == MarkStatus.Absent },
            late = roster.orEmpty().count { it.status == MarkStatus.Late },
        )

    val visibleRoster: List<RosterStudent>
        get() {
            val term = search.trim().lowercase()
            val all = roster.orEmpty()
            return if (term.isEmpty()) all else all.filter { it.name.lowercase().contains(term) }
        }

    val showSaveBar: Boolean get() = saved == null && !roster.isNullOrEmpty()
}

data class MarkCounts(val present: Int, val absent: Int, val late: Int)

data class SaveError(val code: String?)

/** The panel after a save — the server's summary, or the device's when it was parked offline. */
data class SavedPanel(
    val present: Int,
    val absent: Int,
    val late: Int,
    /** null when the server has not answered yet (queued) or did not count them. */
    val guardiansNotified: Int?,
    val absentNames: List<String>,
    val queued: Boolean,
)
