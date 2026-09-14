package org.hogwarts.android.feature.fees.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.kit.AppTileGrid
import org.hogwarts.android.core.designsystem.kit.AppTileItem
import org.hogwarts.android.core.designsystem.kit.ListRow
import org.hogwarts.android.core.designsystem.kit.ListRows
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.StatItem
import org.hogwarts.android.core.designsystem.kit.StatPanel
import org.hogwarts.android.core.designsystem.kit.StatTone
import org.hogwarts.android.core.designsystem.kit.TileArt
import org.hogwarts.android.core.designsystem.kit.TileFace
import org.hogwarts.android.core.designsystem.kit.TileTint
import org.hogwarts.android.feature.fees.R

/**
 * `/finance` for a finance officer — the phone branch of `finance/content.tsx`:
 * the figures panel, the four jobs as app tiles, then the modules as rows.
 *
 * The mobile API has no finance summary: the panel shows the accountant
 * figures `api/mobile/dashboard` already returns (the web's revenue /
 * expenses / pending / unpaid totals, module counts and charts have no
 * route), and ADMIN / DEVELOPER get no figures at all. Every door opens the
 * web page it links to.
 */
/* lucide TrendingUp is not mirrored on the web either. */
@Suppress("DEPRECATION")
@Composable
fun StaffFinanceView(state: StaffUiState, onOpenHref: (String) -> Unit) {
    val f = financeFormat()
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        state.stats?.let { s ->
            StatPanel(
                items = listOf(
                    StatItem(
                        key = "collected",
                        label = stringResource(R.string.staff_collected_today),
                        value = f.compact(s.collectedToday ?: 0.0),
                        tone = StatTone.Positive,
                        wide = true,
                    ),
                    StatItem(
                        key = "pending",
                        label = stringResource(R.string.staff_pending_invoices),
                        value = f.latin(s.pendingInvoices ?: 0),
                        hint = f.compact(s.pendingAmount ?: 0.0),
                        tone = if ((s.pendingInvoices ?: 0) > 0) StatTone.Warning else StatTone.Default,
                    ),
                    StatItem(
                        key = "overdue",
                        label = stringResource(R.string.staff_overdue_invoices),
                        value = f.latin(s.overdueInvoices ?: 0),
                        hint = f.compact(s.overdueAmount ?: 0.0),
                        tone = if ((s.overdueInvoices ?: 0) > 0) StatTone.Negative else StatTone.Default,
                        onClick = { onOpenHref("/finance/invoice") },
                    ),
                ),
            )
        }

        Column {
            SectionHeader(title = stringResource(R.string.staff_quick_actions))
            AppTileGrid(
                items = listOf(
                    AppTileItem("invoice", stringResource(R.string.staff_create_invoice), { onOpenHref("/finance/invoice") }, icon = Icons.Outlined.Description, tint = TileTint.Blue),
                    AppTileItem("payroll", stringResource(R.string.staff_process_payroll), { onOpenHref("/finance/payroll") }, icon = Icons.Outlined.Groups, tint = TileTint.Orange),
                    AppTileItem("expenses", stringResource(R.string.staff_track_expenses), { onOpenHref("/finance/expenses") }, icon = Icons.Outlined.TrendingUp, tint = TileTint.Yellow),
                    AppTileItem("reports", stringResource(R.string.staff_generate_report), { onOpenHref("/finance/reports") }, icon = Icons.Outlined.Assessment, tint = TileTint.Indigo),
                ),
            )
        }

        Column {
            SectionHeader(title = stringResource(R.string.staff_modules))
            ListRows(
                divided = true,
                rows = listOf(
                    { ModuleRow(stringResource(R.string.staff_invoicing), "/finance/invoice", onOpenHref) { TileFace(icon = Icons.Outlined.Description, tint = TileTint.Blue) } },
                    { ModuleRow(stringResource(R.string.staff_fee_collection), "/finance/fees", onOpenHref) { TileFace(art = TileArt.Wallet) } },
                    { ModuleRow(stringResource(R.string.staff_payroll), "/finance/payroll", onOpenHref) { TileFace(icon = Icons.Outlined.Groups, tint = TileTint.Orange) } },
                    { ModuleRow(stringResource(R.string.staff_reports), "/finance/reports", onOpenHref) { TileFace(icon = Icons.Outlined.Assessment, tint = TileTint.Indigo) } },
                ),
            )
        }
    }
}

@Composable
private fun ModuleRow(title: String, href: String, onOpenHref: (String) -> Unit, art: @Composable () -> Unit) {
    ListRow(title = title, art = art, onClick = { onOpenHref(href) }, chevron = true)
}
