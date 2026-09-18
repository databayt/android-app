package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.dashboard.R
import org.hogwarts.android.feature.dashboard.data.remote.InvoiceDto
import org.hogwarts.android.feature.dashboard.data.remote.ResourceUsageDto
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * The two sections every role dashboard renders under its quick actions on the
 * web: `resource-usage-section.tsx` then `invoice-history-section.tsx`, each a
 * heading over a bordered table.
 *
 * `chart-section.tsx`, which sits between them on the web, is not here: its
 * `generateBarChartData()` is deterministic placeholder data carrying a
 * "TODO: Replace with real data" comment, so there is nothing real to draw.
 *
 * Both tables scroll sideways inside their border, as the web's
 * `overflow-x-auto` containers do, rather than reflowing into cards — the
 * columns stay the web's columns at 390dp.
 */
@Composable
fun RoleSections(
    role: UserRole,
    resources: List<ResourceUsageDto>,
    invoices: List<InvoiceDto>,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(Modifier.fillMaxWidth()) {
            SectionHeader(stringResource(resourceTitle(role)))
            ResourceUsageTable(resources)
        }
        Column(Modifier.fillMaxWidth()) {
            SectionHeader(stringResource(invoiceTitle(role)))
            InvoiceHistoryTable(invoices)
        }
    }
}

// ---------------------------------------------------------------------------
// Resource usage — `billingsdk/detailed-usage-table.tsx`
// ---------------------------------------------------------------------------

private val RESOURCE_COL = 180.dp
private val NUMBER_COL = 128.dp
private val USAGE_COL = 168.dp

@Composable
private fun ResourceUsageTable(resources: List<ResourceUsageDto>) {
    val number = tableNumberFormat()
    TableFrame(width = RESOURCE_COL + NUMBER_COL * 2 + USAGE_COL) {
        TableHeaderRow {
            HeadCell(stringResource(R.string.dash_table_resource), RESOURCE_COL)
            HeadCell(stringResource(R.string.dash_table_used), NUMBER_COL, TextAlign.End)
            HeadCell(stringResource(R.string.dash_table_limit), NUMBER_COL, TextAlign.End)
            HeadCell(stringResource(R.string.dash_table_usage), USAGE_COL, TextAlign.End)
        }
        if (resources.isEmpty()) {
            EmptyRow(stringResource(R.string.dash_table_no_resources))
            return@TableFrame
        }
        resources.forEach { resource ->
            val unit = resource.unit?.let { " " + unitLabel(it) }.orEmpty()
            val percent = resource.percent
                ?: if (resource.limit > 0) resource.used / resource.limit * 100 else 0.0
            TableRow {
                BodyCell(resourceName(resource), RESOURCE_COL, emphasis = true)
                BodyCell(number.format(resource.used) + unit, NUMBER_COL, align = TextAlign.End)
                BodyCell(
                    number.format(resource.limit) + unit,
                    NUMBER_COL,
                    align = TextAlign.End,
                    color = HogwartsTheme.colors.mutedForeground,
                )
                Box(Modifier.width(USAGE_COL).padding(horizontal = 12.dp, vertical = 12.dp)) {
                    UsageBar(percent)
                }
            }
        }
    }
}

/** `getPercentageBar`: an emerald fill that turns orange at 75% and red at 90%. */
@Composable
private fun UsageBar(percent: Double) {
    val colors = HogwartsTheme.colors
    val clamped = percent.coerceIn(0.0, 100.0)
    val fill = when {
        percent >= 90 -> colors.destructive
        percent >= 75 -> BrandColors.Upcoming
        else -> BrandColors.Live
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier
                .weight(1f)
                .height(8.dp)
                .clip(HogwartsShapes.Pill)
                .background(colors.muted),
        ) {
            Box(
                Modifier
                    .fillMaxWidth((clamped / 100.0).toFloat())
                    .fillMaxHeight()
                    .clip(HogwartsShapes.Pill)
                    .background(fill),
            )
        }
        Text(
            // The digits belong to the bar beside them, so the percent reads
            // left to right whichever way the row runs.
            "⁦${percent.roundToInt()}%⁩",
            style = HogwartsTheme.type.caption,
            color = colors.foreground,
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// Invoice history — `billingsdk/invoice-history.tsx`
// ---------------------------------------------------------------------------

private val DATE_COL = 120.dp
private val DESC_COL = 200.dp
private val AMOUNT_COL = 104.dp
private val STATUS_COL = 96.dp
private val ACTION_COL = 72.dp

@Composable
private fun InvoiceHistoryTable(invoices: List<InvoiceDto>) {
    val invoiceWord = stringResource(R.string.dash_invoice_word)
    TableFrame(width = DATE_COL + DESC_COL + AMOUNT_COL + STATUS_COL + ACTION_COL) {
        TableHeaderRow {
            HeadCell(stringResource(R.string.dash_invoice_date), DATE_COL)
            HeadCell(stringResource(R.string.dash_invoice_description), DESC_COL)
            HeadCell(stringResource(R.string.dash_invoice_amount), AMOUNT_COL, TextAlign.End)
            HeadCell(stringResource(R.string.dash_invoice_status), STATUS_COL, TextAlign.End)
            HeadCell(stringResource(R.string.dash_invoice_action), ACTION_COL, TextAlign.End)
        }
        if (invoices.isEmpty()) {
            EmptyRow(stringResource(R.string.dash_invoice_none))
            return@TableFrame
        }
        invoices.forEach { invoice ->
            TableRow {
                BodyCell(
                    "⁨${invoiceDate(invoice.date)}⁩",
                    DATE_COL,
                    color = HogwartsTheme.colors.mutedForeground,
                )
                BodyCell(describe(invoice.description, invoiceWord), DESC_COL)
                BodyCell(
                    "⁨${invoiceAmount(invoice.amount)}⁩",
                    AMOUNT_COL,
                    align = TextAlign.End,
                    emphasis = true,
                )
                Box(
                    Modifier.width(STATUS_COL).padding(horizontal = 12.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    StatusBadge(invoice.status)
                }
                // The web's download button is wired to an `onDownload` that no
                // dashboard ever passes, so it is a glyph with nothing behind
                // it. Kept as the column's placeholder rather than made to look
                // like a working action.
                Box(
                    Modifier.width(ACTION_COL).padding(horizontal = 12.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text("⤓", style = HogwartsTheme.type.body, color = HogwartsTheme.colors.mutedForeground)
                }
            }
        }
    }
}

/**
 * Both tables print LATIN digits, and only these two tables do.
 *
 * The app's default is Eastern Arabic under `ar`, out of `i18n-format.ts` —
 * but `DetailedUsageTable` and `InvoiceHistory` never call it. They use a bare
 * `new Intl.NumberFormat()` with no locale argument, which resolves to the
 * runtime's own locale rather than the page's, so the live Arabic dashboard
 * prints `3,111` and `98`. Matching what the reader actually sees on the web
 * wins here, so these two tables stay Latin while the calendar, the banner and
 * the day grid above them stay Eastern.
 *
 * `maximumFractionDigits = 3` is `Intl.NumberFormat`'s own default.
 */
internal fun tableNumberFormat(): NumberFormat =
    NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 3 }

/**
 * `getInvoicesByRole` formats the amount as `$${row.amount.toFixed(2)}` — a
 * dollar sign and two decimals on every row.
 *
 * The `currency` the route sends beside the amount is deliberately ignored,
 * because the web ignores it: `queries.ts` pins `INVOICE_CURRENCY = "USD"`
 * under the comment "The web prints every amount with a `$`". Printing ج.س
 * here would put a different number in front of the same invoice on the phone
 * and on the laptop, so this is a web fix, not a phone one.
 */
internal fun invoiceAmount(amount: Double): String = "$" + String.format(Locale.US, "%.2f", amount)

/**
 * `formatDate(row.date, "ar")` — `Intl.DateTimeFormat` with a 2-digit day and
 * month over a numeric year, which under `ar` renders `04‏/03‏/2031`: day
 * first, Latin digits, and a RLM after the day and the month so the parts hold
 * their order inside Arabic text.
 *
 * The locale is hardcoded to `ar` in the web's server action, so the English
 * dashboard shows a day-first date too; this mirrors that rather than quietly
 * localizing. Built from the ISO instant's own date part, because the web
 * formats on a server that runs in UTC and reading it on the device's clock
 * would slide an invoice a day either way.
 */
internal fun invoiceDate(iso: String): String {
    val (year, month, day) = (ISO_DATE.find(iso) ?: return iso).destructured
    return "$day‏/$month‏/$year"
}

private val ISO_DATE = Regex("""^(\d{4})-(\d{2})-(\d{2})""")

/** `translateDesc`: the server's "Invoice …" prefix takes the reader's word for it. */
private fun describe(description: String?, invoiceWord: String): String {
    if (description.isNullOrBlank()) return invoiceWord
    return if (description.startsWith("Invoice ")) description.replaceFirst("Invoice ", "$invoiceWord ") else description
}

@Composable
private fun StatusBadge(status: String) = when (status.lowercase()) {
    "paid" -> LabelBadge(stringResource(R.string.dash_invoice_paid), tint = BrandColors.Live)
    "refunded" -> LabelBadge(stringResource(R.string.dash_invoice_refunded), variant = BadgeVariant.Secondary)
    "open" -> LabelBadge(stringResource(R.string.dash_invoice_open), variant = BadgeVariant.Outline)
    "void" -> LabelBadge(stringResource(R.string.dash_invoice_void), variant = BadgeVariant.Outline)
    else -> LabelBadge(status, variant = BadgeVariant.Outline)
}

// ---------------------------------------------------------------------------
// Table chrome — `ui/table.tsx` inside `rounded-md border overflow-x-auto`
// ---------------------------------------------------------------------------

/**
 * `rounded-md border` around an `overflow-x-auto` table. The columns keep the
 * web's widths and the frame scrolls sideways; at 390dp both tables are wider
 * than the phone, which is exactly the web's behaviour there.
 */
@Composable
private fun TableFrame(width: Dp, content: @Composable () -> Unit) {
    val colors = HogwartsTheme.colors
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Md)
            .border(1.dp, colors.border, HogwartsShapes.Md),
    ) {
        // `w-full` on the table, `overflow-x-auto` on the frame: the columns
        // keep their widths and the frame scrolls when they do not fit, and
        // the table stretches when there is room to spare.
        val table = max(width, maxWidth)
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Column(Modifier.width(table)) { content() }
        }
    }
}

@Composable
private fun TableHeaderRow(cells: @Composable () -> Unit) {
    Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) { cells() }
    Rule()
}

@Composable
private fun TableRow(cells: @Composable () -> Unit) {
    Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) { cells() }
    Rule()
}

@Composable
private fun Rule() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(HogwartsTheme.colors.border))
}

@Composable
private fun HeadCell(label: String, width: Dp, align: TextAlign = TextAlign.Start) {
    Text(
        label,
        style = HogwartsTheme.type.caption,
        color = HogwartsTheme.colors.mutedForeground,
        textAlign = align,
        maxLines = 1,
        modifier = Modifier.width(width).padding(horizontal = 12.dp, vertical = 12.dp),
    )
}

@Composable
private fun BodyCell(
    text: String,
    width: Dp,
    align: TextAlign = TextAlign.Start,
    emphasis: Boolean = false,
    color: Color = HogwartsTheme.colors.foreground,
) {
    Text(
        text,
        style = if (emphasis) HogwartsTheme.type.bodyMedium else HogwartsTheme.type.body,
        color = color,
        textAlign = align,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.width(width).padding(horizontal = 12.dp, vertical = 12.dp),
    )
}

@Composable
private fun EmptyRow(label: String) {
    Box(
        Modifier.fillMaxWidth().height(96.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = HogwartsTheme.type.body, color = HogwartsTheme.colors.mutedForeground)
    }
}

// ---------------------------------------------------------------------------
// Wording — `school-ar.json` / `school-en.json`, `dashboard.*`
// ---------------------------------------------------------------------------

/** `dashboard.resourceUsage` keyed by role; DEVELOPER reads the admin dashboard. */
private fun resourceTitle(role: UserRole): Int = when (role) {
    UserRole.STUDENT -> R.string.dash_resources_student
    UserRole.TEACHER -> R.string.dash_resources_teacher
    UserRole.GUARDIAN -> R.string.dash_resources_guardian
    UserRole.STAFF -> R.string.dash_resources_staff
    UserRole.ACCOUNTANT -> R.string.dash_resources_accountant
    UserRole.ADMIN, UserRole.DEVELOPER -> R.string.dash_resources_admin
    else -> R.string.dash_resources_default
}

/** `dashboard.invoiceHistory` keyed by role. */
private fun invoiceTitle(role: UserRole): Int = when (role) {
    UserRole.STUDENT -> R.string.dash_invoices_student
    UserRole.TEACHER -> R.string.dash_invoices_teacher
    UserRole.GUARDIAN -> R.string.dash_invoices_guardian
    UserRole.STAFF -> R.string.dash_invoices_staff
    UserRole.ACCOUNTANT -> R.string.dash_invoices_accountant
    UserRole.ADMIN, UserRole.DEVELOPER -> R.string.dash_invoices_admin
    else -> R.string.dash_invoices_default
}

/** `dashboard.resourceNames[key]`, falling back to the server's own wording. */
@Composable
private fun resourceName(resource: ResourceUsageDto): String {
    val id = when (resource.key) {
        "assignmentProgress" -> R.string.dash_res_assignment_progress
        "attendanceRate" -> R.string.dash_res_attendance_rate
        "currentGpa" -> R.string.dash_res_current_gpa
        "daysUntilExams" -> R.string.dash_res_days_until_exams
        "lessonsThisWeek" -> R.string.dash_res_lessons_this_week
        "ungradedWork" -> R.string.dash_res_ungraded_work
        "classCoverage" -> R.string.dash_res_class_coverage
        "attendanceMarked" -> R.string.dash_res_attendance_marked
        "childrenEnrolled" -> R.string.dash_res_children_enrolled
        "avgAttendance" -> R.string.dash_res_avg_attendance
        "assignmentsDue" -> R.string.dash_res_assignments_due
        "upcomingEvents" -> R.string.dash_res_upcoming_events
        "tasksAssigned" -> R.string.dash_res_tasks_assigned
        "requestsPending" -> R.string.dash_res_requests_pending
        "daysThisMonth" -> R.string.dash_res_days_this_month
        "efficiencyScore" -> R.string.dash_res_efficiency_score
        "collectionRate" -> R.string.dash_res_collection_rate
        "pendingInvoices" -> R.string.dash_res_pending_invoices
        "monthlyRevenue" -> R.string.dash_res_monthly_revenue
        "overdueAmount" -> R.string.dash_res_overdue_amount
        "enrollment" -> R.string.dash_res_enrollment
        "staffCount" -> R.string.dash_res_staff_count
        "attendanceToday" -> R.string.dash_res_attendance_today
        "budgetUsed" -> R.string.dash_res_budget_used
        "activeUsers" -> R.string.dash_res_active_users
        "storageUsed" -> R.string.dash_res_storage_used
        "activeSessions" -> R.string.dash_res_active_sessions
        "systemHealth" -> R.string.dash_res_system_health
        "schoolsActive" -> R.string.dash_res_schools_active
        "platformUsers" -> R.string.dash_res_platform_users
        "databaseSize" -> R.string.dash_res_database_size
        "systemUptime" -> R.string.dash_res_system_uptime
        else -> null
    }
    return if (id != null) stringResource(id) else resource.name
}

/** `dashboard.units[unit]`, falling back to the server's own unit. */
@Composable
private fun unitLabel(unit: String): String {
    val id = when (unit) {
        "completed" -> R.string.dash_unit_completed
        "days" -> R.string.dash_unit_days
        "lessons" -> R.string.dash_unit_lessons
        "submissions" -> R.string.dash_unit_submissions
        "students" -> R.string.dash_unit_students
        "children" -> R.string.dash_unit_children
        "tasks" -> R.string.dash_unit_tasks
        "events" -> R.string.dash_unit_events
        "requests" -> R.string.dash_unit_requests
        "invoices" -> R.string.dash_unit_invoices
        "users" -> R.string.dash_unit_users
        "sessions" -> R.string.dash_unit_sessions
        "schools" -> R.string.dash_unit_schools
        "staff" -> R.string.dash_unit_staff
        "GB" -> R.string.dash_unit_gb
        "SAR" -> R.string.dash_unit_sar
        else -> null
    }
    return if (id != null) stringResource(id) else unit
}
