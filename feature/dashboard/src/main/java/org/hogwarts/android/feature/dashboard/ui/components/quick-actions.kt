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

internal fun tileArtFor(label: String): TileArt? = when (label) {
    "Assignments" -> TileArt.Assignments
    "Grades", "My Grades", "Performance" -> TileArt.Grades
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
    "Profile" -> TileArt.Profile
    "Staff", "My Children", "Classrooms" -> TileArt.Students
    "Subjects" -> TileArt.Subject
    "Reports" -> TileArt.Exams
    else -> null
}

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
    "Schedule" -> stringResource(R.string.dash_qa_schedule)
    "My Grades" -> stringResource(R.string.dash_qa_my_grades)
    "Profile" -> stringResource(R.string.dash_qa_profile)
    "My Children" -> stringResource(R.string.dash_qa_my_children)
    "Contact Teacher" -> stringResource(R.string.dash_qa_contact_teacher)
    "Invoices" -> stringResource(R.string.dash_qa_invoices)
    "Fees" -> stringResource(R.string.dash_qa_fees)
    "Receipts" -> stringResource(R.string.dash_qa_receipts)
    "Dashboard" -> stringResource(R.string.dash_qa_dashboard)
    "Announcements" -> stringResource(R.string.dash_qa_announcements)
    "Events" -> stringResource(R.string.dash_qa_events)
    else -> null
}
