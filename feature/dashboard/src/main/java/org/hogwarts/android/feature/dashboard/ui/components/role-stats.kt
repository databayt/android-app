package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.kit.StatItem
import org.hogwarts.android.core.designsystem.kit.StatPanel
import org.hogwarts.android.core.designsystem.kit.StatTone
import org.hogwarts.android.core.designsystem.locale.currentLocale
import org.hogwarts.android.feature.dashboard.R
import org.hogwarts.android.feature.dashboard.data.remote.DashboardDto
import java.text.NumberFormat

/**
 * The role's headline figures as one stat panel — the kit's rendering of the
 * role sections under the web dashboard (admin, accountant, staff, parent).
 */
@Composable
fun RoleStats(role: UserRole, data: DashboardDto, modifier: Modifier = Modifier) {
    val locale = currentLocale()
    val n = NumberFormat.getIntegerInstance(locale)
    val money = NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 0 }
    fun int(v: Int?) = n.format(v ?: 0)

    val items: List<StatItem> = when (role) {
        UserRole.ADMIN, UserRole.DEVELOPER -> listOf(
            StatItem("students", stringResource(R.string.dash_stat_total_students), int(data.totalStudents)),
            StatItem("teachers", stringResource(R.string.dash_stat_total_teachers), int(data.totalTeachers)),
            StatItem("classes", stringResource(R.string.dash_stat_total_classes), int(data.totalClasses)),
        )
        UserRole.ACCOUNTANT -> listOf(
            StatItem("collected", stringResource(R.string.dash_stat_collected_today), money.format(data.collectedToday ?: 0.0), tone = StatTone.Positive, wide = true),
            StatItem("pending", stringResource(R.string.dash_stat_pending_invoices), int(data.pendingInvoices), hint = money.format(data.pendingAmount ?: 0.0), tone = StatTone.Warning),
            StatItem("overdue", stringResource(R.string.dash_stat_overdue_invoices), int(data.overdueInvoices), hint = money.format(data.overdueAmount ?: 0.0), tone = StatTone.Negative),
        )
        UserRole.STAFF -> listOf(
            StatItem("students", stringResource(R.string.dash_stat_total_students), int(data.totalStudents)),
            StatItem("present", stringResource(R.string.dash_stat_present_today), int(data.presentToday), tone = StatTone.Positive),
            StatItem("events", stringResource(R.string.dash_stat_upcoming_events), int(data.upcomingEvents)),
        )
        UserRole.TEACHER -> listOf(
            StatItem("today", stringResource(R.string.dash_stat_today_classes), int(data.todayClasses)),
            StatItem("classes", stringResource(R.string.dash_stat_total_classes), int(data.totalClasses)),
        )
        UserRole.STUDENT -> listOf(
            StatItem("attendance", stringResource(R.string.dash_stat_attendance), NumberFormat.getPercentInstance(locale).format((data.attendancePercentage ?: 0.0) / 100.0), tone = StatTone.Positive),
            StatItem("exams", stringResource(R.string.dash_stat_upcoming_exams), int(data.upcomingExams)),
        )
        UserRole.GUARDIAN -> listOf(
            StatItem("children", stringResource(R.string.dash_stat_children), int(data.childrenCount)),
            StatItem("notifications", stringResource(R.string.dash_stat_unread_notifications), int(data.unreadNotifications)),
        )
        else -> emptyList()
    }
    StatPanel(items = items, modifier = modifier)
}
