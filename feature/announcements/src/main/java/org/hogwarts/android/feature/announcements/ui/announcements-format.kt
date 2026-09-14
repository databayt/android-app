package org.hogwarts.android.feature.announcements.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.designsystem.locale.currentLocale
import org.hogwarts.android.feature.announcements.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Locale

/**
 * Dates the way `lib/i18n-format.ts` `formatDate` prints them: Gregorian,
 * Eastern Arabic digits under `ar`, Latin under `en`.
 */
internal class AnnouncementsFormat(locale: Locale, private val zone: ZoneId = ZoneId.systemDefault()) {
    private val arabic = locale.language == "ar"
    private val digits = DecimalStyle.of(if (arabic) Locale.forLanguageTag("ar-u-nu-arab") else Locale.ENGLISH)
    private val locale = if (arabic) Locale.forLanguageTag("ar") else Locale.ENGLISH

    /** The default `{ year, month: "2-digit", day: "2-digit" }` — "09/14/2026" · "١٤\u200F/٠٩\u200F/٢٠٢٦". */
    fun shortDate(instant: Instant?): String = format(instant, if (arabic) "dd\u200F/MM\u200F/yyyy" else "MM/dd/yyyy")

    /** `{ year, month: "long", day }` — "September 14, 2026" · "١٤ سبتمبر ٢٠٢٦". */
    fun longDate(instant: Instant?): String = format(instant, if (arabic) "d MMMM yyyy" else "MMMM d, yyyy")

    private fun format(instant: Instant?, pattern: String): String =
        instant?.atZone(zone)?.format(DateTimeFormatter.ofPattern(pattern, locale).withDecimalStyle(digits)).orEmpty()
}

@Composable
@ReadOnlyComposable
internal fun announcementsFormat(): AnnouncementsFormat = AnnouncementsFormat(currentLocale())

/** The card eyebrow and the Scope row: `schoolWide` · `classSpecific` · `roleSpecific`. */
@Composable
@ReadOnlyComposable
internal fun scopeLabel(scope: String): String = when (scope) {
    "school" -> stringResource(R.string.announcements_scope_school)
    "class" -> stringResource(R.string.announcements_scope_class)
    "role" -> stringResource(R.string.announcements_scope_role)
    else -> scope
}

/** A card's priority chip — `table.tsx` reads `priority.<level>.label`. */
@Composable
@ReadOnlyComposable
internal fun cardPriorityLabel(priority: String): String = when (priority.lowercase()) {
    "urgent" -> stringResource(R.string.announcements_priority_urgent)
    "high" -> stringResource(R.string.announcements_priority_high)
    "low" -> stringResource(R.string.announcements_priority_low)
    "normal" -> stringResource(R.string.announcements_priority_normal)
    else -> priority
}

/** The reading page's priority words — `detail.tsx` reads the flat `high`/`medium`/`low`/`normal` keys. */
@Composable
@ReadOnlyComposable
internal fun detailPriorityLabel(priority: String): String = when (priority.lowercase()) {
    "urgent" -> stringResource(R.string.announcements_priority_urgent)
    "high" -> stringResource(R.string.announcements_detail_high)
    "medium" -> stringResource(R.string.announcements_detail_medium)
    "low" -> stringResource(R.string.announcements_detail_low)
    else -> stringResource(R.string.announcements_detail_normal)
}

@Composable
@ReadOnlyComposable
internal fun roleLabel(role: String): String = when (role.uppercase()) {
    "ADMIN" -> stringResource(R.string.announcements_role_admin)
    "TEACHER" -> stringResource(R.string.announcements_role_teacher)
    "STUDENT" -> stringResource(R.string.announcements_role_student)
    "GUARDIAN" -> stringResource(R.string.announcements_role_guardian)
    "STAFF" -> stringResource(R.string.announcements_role_staff)
    "ACCOUNTANT" -> stringResource(R.string.announcements_role_accountant)
    else -> role
}
