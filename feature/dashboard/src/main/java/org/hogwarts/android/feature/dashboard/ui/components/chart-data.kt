package org.hogwarts.android.feature.dashboard.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.locale.currentLocale
import org.hogwarts.android.feature.dashboard.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle

/**
 * The chart section's numbers and wording, per role — a port of
 * `chart-section.tsx`'s `defaultDataByRole` and the `chartLabels` dictionary
 * that renames its parts.
 *
 * Every figure here is the web's placeholder figure, carried over so the two
 * dashboards agree on screen. `chart-section.tsx` marks them "TODO: Replace
 * with real data from dashboard actions when metrics are available"; when that
 * lands on the web, this table is what has to follow it.
 *
 * The web has a PRINCIPAL row too. [UserRole] has no PRINCIPAL, so it is not
 * ported; if the role is ever added, its row is in `chart-section.tsx`.
 */
internal data class RoleChartData(
    @StringRes val barTitle: Int,
    @StringRes val barDescription: Int,
    @StringRes val barPrimaryLabel: Int,
    @StringRes val barSecondaryLabel: Int,
    val radialValue: Float,
    val radialMax: Float,
    @StringRes val radialLabel: Int,
    val radialTrend: Float,
    @StringRes val radialTrendLabel: Int,
    val areaTrend: Float,
    @StringRes val areaTrendLabel: Int,
    val areaPoints: List<AreaPoint>,
)

/** One column of the stacked area chart: the pair that stacks, and its tick. */
internal data class AreaPoint(val label: AreaLabel, val primary: Float, val secondary: Float)

/**
 * An area chart's tick. The web writes them in English and translates at
 * render time — days and months through `Intl`, "Week"/"Term" through the
 * dictionary — so they are held here as what they mean, not as text.
 */
internal sealed interface AreaLabel {
    data class OfMonth(val month: Month) : AreaLabel
    data class OfWeekday(val day: DayOfWeek) : AreaLabel
    data class OfWeek(val number: Int) : AreaLabel
    data class OfTerm(val number: Int) : AreaLabel
}

@Composable
internal fun areaLabel(label: AreaLabel): String {
    val locale = currentLocale()
    return when (label) {
        is AreaLabel.OfMonth -> label.month.getDisplayName(TextStyle.SHORT, locale)
        is AreaLabel.OfWeekday -> label.day.getDisplayName(TextStyle.SHORT, locale)
        is AreaLabel.OfWeek -> "${stringResource(R.string.dash_chart_week)} ${label.number}"
        is AreaLabel.OfTerm -> "${stringResource(R.string.dash_chart_term)} ${label.number}"
    }
}

/** One day of the bar chart. */
internal data class BarPoint(val date: LocalDate, val primary: Int, val secondary: Int)

/**
 * `generateBarChartData(3)` — the last three months of days, each value keyed
 * off the day of the year so the series is stable across renders rather than
 * random. Reproduced arithmetic-for-arithmetic, including the web's own
 * off-by-one day-of-year (it measures from December 31st, not January 1st).
 */
internal fun barSeries(today: LocalDate, months: Int = 3): List<BarPoint> =
    (months * 30 downTo 0).map { back ->
        val date = today.minusDays(back.toLong())
        val dayOfYear = date.dayOfYear
        BarPoint(
            date = date,
            primary = 200 + (dayOfYear * 17) % 300,
            secondary = 150 + (dayOfYear * 13) % 200,
        )
    }

/** `getChartSectionTitle` — `dashboard.charts` keyed by role. */
@StringRes
internal fun sectionTitle(role: UserRole): Int = when (role) {
    UserRole.STUDENT -> R.string.dash_charts_student
    UserRole.TEACHER -> R.string.dash_charts_teacher
    UserRole.GUARDIAN -> R.string.dash_charts_guardian
    UserRole.STAFF -> R.string.dash_charts_staff
    UserRole.ACCOUNTANT -> R.string.dash_charts_accountant
    UserRole.ADMIN -> R.string.dash_charts_admin
    UserRole.DEVELOPER -> R.string.dash_charts_developer
    else -> R.string.dash_charts_default
}

/** `defaultDataByRole[role] || defaultDataByRole.ADMIN`. */
internal fun roleChartData(role: UserRole): RoleChartData = when (role) {
    UserRole.STUDENT -> Student
    UserRole.TEACHER -> Teacher
    UserRole.GUARDIAN -> Guardian
    UserRole.STAFF -> Staff
    UserRole.ACCOUNTANT -> Accountant
    UserRole.DEVELOPER -> Developer
    else -> Admin
}

private val Student = RoleChartData(
    barTitle = R.string.dash_chart_student_bar_title,
    barDescription = R.string.dash_chart_student_bar_desc,
    barPrimaryLabel = R.string.dash_chart_student_bar_primary,
    barSecondaryLabel = R.string.dash_chart_student_bar_secondary,
    radialValue = 86f,
    radialMax = 100f,
    radialLabel = R.string.dash_chart_student_radial_label,
    radialTrend = 3.2f,
    radialTrendLabel = R.string.dash_chart_student_radial_trend,
    areaTrend = 5.2f,
    areaTrendLabel = R.string.dash_chart_student_area_trend,
    areaPoints = listOf(
        AreaPoint(AreaLabel.OfMonth(Month.SEPTEMBER), 78f, 72f),
        AreaPoint(AreaLabel.OfMonth(Month.OCTOBER), 82f, 74f),
        AreaPoint(AreaLabel.OfMonth(Month.NOVEMBER), 79f, 75f),
        AreaPoint(AreaLabel.OfMonth(Month.DECEMBER), 85f, 76f),
        AreaPoint(AreaLabel.OfMonth(Month.JANUARY), 88f, 77f),
        AreaPoint(AreaLabel.OfMonth(Month.FEBRUARY), 86f, 78f),
    ),
)

private val Teacher = RoleChartData(
    barTitle = R.string.dash_chart_teacher_bar_title,
    barDescription = R.string.dash_chart_teacher_bar_desc,
    barPrimaryLabel = R.string.dash_chart_teacher_bar_primary,
    barSecondaryLabel = R.string.dash_chart_teacher_bar_secondary,
    radialValue = 72f,
    radialMax = 100f,
    radialLabel = R.string.dash_chart_teacher_radial_label,
    radialTrend = -5.0f,
    radialTrendLabel = R.string.dash_chart_teacher_radial_trend,
    areaTrend = 2.1f,
    areaTrendLabel = R.string.dash_chart_teacher_area_trend,
    areaPoints = listOf(
        AreaPoint(AreaLabel.OfWeek(1), 82f, 95f),
        AreaPoint(AreaLabel.OfWeek(2), 78f, 92f),
        AreaPoint(AreaLabel.OfWeek(3), 85f, 88f),
        AreaPoint(AreaLabel.OfWeek(4), 80f, 94f),
        AreaPoint(AreaLabel.OfWeek(5), 84f, 91f),
        AreaPoint(AreaLabel.OfWeek(6), 87f, 96f),
    ),
)

private val Guardian = RoleChartData(
    barTitle = R.string.dash_chart_guardian_bar_title,
    barDescription = R.string.dash_chart_guardian_bar_desc,
    barPrimaryLabel = R.string.dash_chart_guardian_bar_primary,
    barSecondaryLabel = R.string.dash_chart_guardian_bar_secondary,
    radialValue = 88f,
    radialMax = 100f,
    radialLabel = R.string.dash_chart_guardian_radial_label,
    radialTrend = 4.5f,
    radialTrendLabel = R.string.dash_chart_guardian_radial_trend,
    areaTrend = 3.8f,
    areaTrendLabel = R.string.dash_chart_guardian_area_trend,
    areaPoints = listOf(
        AreaPoint(AreaLabel.OfWeek(1), 85f, 95f),
        AreaPoint(AreaLabel.OfWeek(2), 82f, 90f),
        AreaPoint(AreaLabel.OfWeek(3), 88f, 100f),
        AreaPoint(AreaLabel.OfWeek(4), 84f, 85f),
        AreaPoint(AreaLabel.OfWeek(5), 90f, 95f),
        AreaPoint(AreaLabel.OfWeek(6), 92f, 100f),
    ),
)

private val Staff = RoleChartData(
    barTitle = R.string.dash_chart_staff_bar_title,
    barDescription = R.string.dash_chart_staff_bar_desc,
    barPrimaryLabel = R.string.dash_chart_staff_bar_primary,
    barSecondaryLabel = R.string.dash_chart_staff_bar_secondary,
    radialValue = 88f,
    radialMax = 100f,
    radialLabel = R.string.dash_chart_staff_radial_label,
    radialTrend = 2.1f,
    radialTrendLabel = R.string.dash_chart_staff_radial_trend,
    areaTrend = 4.2f,
    areaTrendLabel = R.string.dash_chart_staff_area_trend,
    areaPoints = listOf(
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.MONDAY), 8f, 5f),
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.TUESDAY), 12f, 7f),
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.WEDNESDAY), 10f, 6f),
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.THURSDAY), 15f, 9f),
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.FRIDAY), 9f, 4f),
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.SATURDAY), 3f, 1f),
    ),
)

private val Accountant = RoleChartData(
    barTitle = R.string.dash_chart_accountant_bar_title,
    barDescription = R.string.dash_chart_accountant_bar_desc,
    barPrimaryLabel = R.string.dash_chart_accountant_bar_primary,
    barSecondaryLabel = R.string.dash_chart_accountant_bar_secondary,
    radialValue = 87f,
    radialMax = 100f,
    radialLabel = R.string.dash_chart_accountant_radial_label,
    radialTrend = 5.2f,
    radialTrendLabel = R.string.dash_chart_accountant_radial_trend,
    areaTrend = 8.5f,
    areaTrendLabel = R.string.dash_chart_accountant_area_trend,
    areaPoints = listOf(
        AreaPoint(AreaLabel.OfMonth(Month.JANUARY), 120000f, 95000f),
        AreaPoint(AreaLabel.OfMonth(Month.FEBRUARY), 135000f, 88000f),
        AreaPoint(AreaLabel.OfMonth(Month.MARCH), 128000f, 92000f),
        AreaPoint(AreaLabel.OfMonth(Month.APRIL), 142000f, 98000f),
        AreaPoint(AreaLabel.OfMonth(Month.MAY), 155000f, 105000f),
        AreaPoint(AreaLabel.OfMonth(Month.JUNE), 148000f, 110000f),
    ),
)

private val Admin = RoleChartData(
    barTitle = R.string.dash_chart_admin_bar_title,
    barDescription = R.string.dash_chart_admin_bar_desc,
    barPrimaryLabel = R.string.dash_chart_admin_bar_primary,
    barSecondaryLabel = R.string.dash_chart_admin_bar_secondary,
    radialValue = 98f,
    radialMax = 100f,
    radialLabel = R.string.dash_chart_admin_radial_label,
    radialTrend = 0.5f,
    radialTrendLabel = R.string.dash_chart_admin_radial_trend,
    areaTrend = 12.5f,
    areaTrendLabel = R.string.dash_chart_admin_area_trend,
    areaPoints = listOf(
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.MONDAY), 245f, 1200f),
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.TUESDAY), 312f, 1450f),
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.WEDNESDAY), 298f, 1380f),
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.THURSDAY), 356f, 1620f),
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.FRIDAY), 289f, 1280f),
        AreaPoint(AreaLabel.OfWeekday(DayOfWeek.SATURDAY), 145f, 680f),
    ),
)

private val Developer = RoleChartData(
    barTitle = R.string.dash_chart_developer_bar_title,
    barDescription = R.string.dash_chart_developer_bar_desc,
    barPrimaryLabel = R.string.dash_chart_developer_bar_primary,
    barSecondaryLabel = R.string.dash_chart_developer_bar_secondary,
    radialValue = 99f,
    radialMax = 100f,
    radialLabel = R.string.dash_chart_developer_radial_label,
    radialTrend = 0.1f,
    radialTrendLabel = R.string.dash_chart_developer_radial_trend,
    areaTrend = 15.2f,
    areaTrendLabel = R.string.dash_chart_developer_area_trend,
    areaPoints = listOf(
        AreaPoint(AreaLabel.OfMonth(Month.JULY), 35f, 12000f),
        AreaPoint(AreaLabel.OfMonth(Month.AUGUST), 38f, 15000f),
        AreaPoint(AreaLabel.OfMonth(Month.SEPTEMBER), 42f, 18000f),
        AreaPoint(AreaLabel.OfMonth(Month.OCTOBER), 45f, 22000f),
        AreaPoint(AreaLabel.OfMonth(Month.NOVEMBER), 48f, 25000f),
        AreaPoint(AreaLabel.OfMonth(Month.DECEMBER), 52f, 28000f),
    ),
)
