package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.kit.AppTileGrid
import org.hogwarts.android.core.designsystem.kit.AppTileItem
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.TileArt
import org.hogwarts.android.core.designsystem.kit.TileTint
import org.hogwarts.android.feature.dashboard.R
import org.hogwarts.android.feature.dashboard.data.remote.QuickActionDto

/**
 * The role's four quick actions as one row of tiles — mirrors
 * `dashboard/phone-quick-actions.tsx` + `quick-actions.tsx` (32dp columns, tiles
 * fill their cell). Art and translated labels are chosen by the server's
 * English label, the way the web's `tileByLabel` does.
 */
@Composable
fun QuickActions(actions: List<QuickActionDto>, onOpen: (href: String) -> Unit, modifier: Modifier = Modifier) {
    if (actions.isEmpty()) return
    Column(modifier.fillMaxWidth()) {
        SectionHeader(stringResource(R.string.dash_quick_actions))
        AppTileGrid(
            items = actions.take(4).mapIndexed { i, action ->
                AppTileItem(
                    key = action.key.ifEmpty { action.label },
                    label = quickActionLabel(action.label) ?: action.label,
                    onClick = { onOpen(action.href) },
                    art = tileArtFor(action.label),
                    tint = FALLBACK_TINTS[i % FALLBACK_TINTS.size],
                )
            },
            columnGap = 32.dp,
            maxFaceSize = null,
            labelLines = 1,
        )
    }
}

private val FALLBACK_TINTS = listOf(TileTint.Blue, TileTint.Green, TileTint.Orange, TileTint.Purple)

/** `tileByLabel` in `quick-actions.tsx`, label for label. */
internal fun tileArtFor(label: String): TileArt? = when (label) {
    "Assignments" -> TileArt.Assignments
    "Grades", "Performance" -> TileArt.Grades
    "Exams", "Reports" -> TileArt.Exams
    "Schedule" -> TileArt.Schedule
    "Messages", "Contact Teacher" -> TileArt.Message
    "Attendance" -> TileArt.Attendance
    "Announcements", "Announce" -> TileArt.Announcements
    "Events" -> TileArt.Events
    "Fees", "Finance", "Invoices", "Receipts" -> TileArt.Wallet
    "Library" -> TileArt.Library
    "Notifications" -> TileArt.Notifications
    "School", "Dashboard" -> TileArt.Home
    "Settings" -> TileArt.Setting
    // The web maps Profile to Apple's grey Contacts book, not the blue person.
    "Profile" -> TileArt.Contacts
    "Staff", "Children", "Classrooms" -> TileArt.Students
    "Subjects" -> TileArt.Subject
    else -> null
}

/**
 * `dashboard.quickActionsSection` keyed by the action's English label, the way
 * the web's `toCamelCase(label)` lookup does. Every label
 * `getQuickActionsByRole` can produce is here; an unknown one keeps its
 * English, which is what the web falls back to too.
 */
@Composable
private fun quickActionLabel(label: String): String? = when (label) {
    "School" -> stringResource(R.string.dash_qa_school)
    "Settings" -> stringResource(R.string.dash_qa_settings)
    "Finance" -> stringResource(R.string.dash_qa_finance)
    "Staff" -> stringResource(R.string.dash_qa_staff)
    "Performance" -> stringResource(R.string.dash_qa_performance)
    "Reports" -> stringResource(R.string.dash_qa_reports)
    "Announce" -> stringResource(R.string.dash_qa_announce)
    "Attendance" -> stringResource(R.string.dash_qa_attendance)
    "Grades" -> stringResource(R.string.dash_qa_grades)
    "Assignments" -> stringResource(R.string.dash_qa_assignments)
    "Exams" -> stringResource(R.string.dash_qa_exams)
    "Schedule" -> stringResource(R.string.dash_qa_schedule)
    "Profile" -> stringResource(R.string.dash_qa_profile)
    "Children" -> stringResource(R.string.dash_qa_children)
    "Contact Teacher" -> stringResource(R.string.dash_qa_contact_teacher)
    "Invoices" -> stringResource(R.string.dash_qa_invoices)
    "Fees" -> stringResource(R.string.dash_qa_fees)
    "Receipts" -> stringResource(R.string.dash_qa_receipts)
    "Dashboard" -> stringResource(R.string.dash_qa_dashboard)
    "Announcements" -> stringResource(R.string.dash_qa_announcements)
    "Events" -> stringResource(R.string.dash_qa_events)
    "Messages" -> stringResource(R.string.dash_qa_messages)
    "Classrooms" -> stringResource(R.string.dash_qa_classrooms)
    "Subjects" -> stringResource(R.string.dash_qa_subjects)
    "Library" -> stringResource(R.string.dash_qa_library)
    "Notifications" -> stringResource(R.string.dash_qa_notifications)
    else -> null
}
