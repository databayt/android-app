package org.hogwarts.android.feature.attendance.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.attendance.data.outbox.AttendanceOutbox
import org.hogwarts.android.feature.attendance.data.remote.AttendanceApi
import org.hogwarts.android.feature.attendance.di.AttendanceClock
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceRecordDto
import org.hogwarts.android.feature.attendance.data.remote.dto.OfflineSyncItemDto
import org.hogwarts.android.feature.attendance.data.remote.dto.OfflineSyncRequest
import org.hogwarts.android.feature.attendance.data.remote.dto.QuickAttendancePayloadDto
import org.hogwarts.android.feature.attendance.data.remote.dto.QuickSummaryDto
import org.hogwarts.android.feature.attendance.data.remote.dto.SectionAttendanceResponse
import org.hogwarts.android.feature.attendance.domain.model.AttendanceEntry
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStats
import org.hogwarts.android.feature.attendance.domain.model.ChildAttendance
import org.hogwarts.android.feature.attendance.domain.model.MarkStatus
import org.hogwarts.android.feature.attendance.domain.model.QuickContext
import org.hogwarts.android.feature.attendance.domain.model.QuickMark
import org.hogwarts.android.feature.attendance.domain.model.QuickSection
import org.hogwarts.android.feature.attendance.domain.model.QuickSummary
import org.hogwarts.android.feature.attendance.domain.model.RosterStudent
import org.hogwarts.android.feature.attendance.domain.model.SaveOutcome
import org.hogwarts.android.feature.attendance.domain.model.SectionRoster
import org.hogwarts.android.feature.attendance.domain.model.StudentAttendance
import org.hogwarts.android.feature.attendance.domain.model.TodayTotals
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val api: AttendanceApi,
    private val outbox: AttendanceOutbox,
    private val tenantContext: TenantContext,
    private val json: Json,
    @AttendanceClock private val clock: Clock,
) : AttendanceRepository {

    /**
     * No mobile route mirrors the web's `getQuickMarkingContext`, so it is
     * assembled: sections from `teacher/classes`, today's slots from
     * `teacher/schedule` joined on section + grade name (the schedule carries
     * no section id), the school day from the dashboard's closure, and each
     * section's marked count from its roster for today.
     */
    override suspend fun quickContext(today: LocalDate, now: LocalTime): QuickContext = coroutineScope {
        val classesCall = async { api.teacherClasses().data }
        val dayCall = async { runCatching { api.dashboard().todayTimetable }.getOrNull() }
        val classes = classesCall.await().distinctBy { it.sectionId }
        val day = dayCall.await()
        val jsDay = day?.dayOfWeek ?: (today.dayOfWeek.value % 7)

        val slots = try {
            api.teacherSchedule(jsDay).data
        } catch (e: HttpException) {
            // 403/404: the user has no teacher record — no slots, sections still listed.
            if (e.code() == 403 || e.code() == 404) emptyList() else throw e
        }

        data class Slot(val name: String?, val start: String?, val end: String?)
        val slotBySection = mutableMapOf<Pair<String, String>, Slot>()
        for (s in slots) {
            val key = (s.sectionName ?: continue) to (s.gradeName ?: "")
            val slot = Slot(s.periodName, timeOfDay(s.startTime), timeOfDay(s.endTime))
            val existing = slotBySection[key]
            if (existing == null || (slot.start != null && existing.start != null && slot.start < existing.start)) {
                slotBySection[key] = slot
            }
        }

        val rosters = classes.map { c ->
            async { c.sectionId to runCatching { roster(c.sectionId, today) }.getOrNull() }
        }.awaitAll().mapNotNull { (id, r) -> r?.let { id to it } }.toMap()

        val nowHHmm = hhmm(now.hour, now.minute)
        val sections = classes.map { c ->
            val slot = slotBySection[c.sectionName to (c.gradeName ?: "")]
            QuickSection(
                id = c.sectionId,
                name = c.sectionName,
                gradeName = c.gradeName.orEmpty(),
                studentCount = c.studentCount,
                markedCount = rosters[c.sectionId]?.markedCount ?: 0,
                scheduledToday = slot != null,
                periodName = slot?.name,
                periodStart = slot?.start,
                periodEnd = slot?.end,
                isCurrent = slot?.start != null && slot.end != null && nowHHmm >= slot.start && nowHHmm <= slot.end,
            )
        }.sortedWith(QUICK_ORDER)

        QuickContext(today = today, isSchoolDay = day?.closure == null, sections = sections, rosters = rosters)
    }

    override suspend fun roster(sectionId: String, date: LocalDate): SectionRoster =
        api.sectionAttendance(sectionId, date.toString()).toRoster()

    override suspend fun submitQuick(mark: QuickMark): SaveOutcome {
        val item = OfflineSyncItemDto(
            idempotencyKey = UUID.randomUUID().toString(),
            kind = SYNC_KIND,
            payload = QuickAttendancePayloadDto(
                sectionId = mark.sectionId,
                date = mark.date.toString(),
                absentStudentIds = mark.absentStudentIds,
                lateStudentIds = mark.lateStudentIds,
            ),
            createdAt = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS).toString(),
        )
        return try {
            val verdict = api.offlineSync(OfflineSyncRequest(listOf(item))).results
                .firstOrNull { it.idempotencyKey == item.idempotencyKey }
                ?: return SaveOutcome.Rejected(null)
            when (verdict.result) {
                "applied" -> {
                    val summary = verdict.data
                        ?.let { runCatching { json.decodeFromJsonElement(QuickSummaryDto.serializer(), it) }.getOrNull() }
                        ?: QuickSummaryDto(
                            total = mark.absentStudentIds.size + mark.lateStudentIds.size,
                            absent = mark.absentStudentIds.size,
                            late = mark.lateStudentIds.size,
                        )
                    SaveOutcome.Saved(
                        QuickSummary(summary.total, summary.present, summary.absent, summary.late, summary.guardiansNotified),
                    )
                }
                "duplicate" -> SaveOutcome.AlreadyNewer
                else -> SaveOutcome.Rejected(verdict.code)
            }
        } catch (e: IOException) {
            outbox.enqueue(item, mark.coalesceKey, tenantContext.requireSchoolId())
            SaveOutcome.Queued
        } catch (e: HttpException) {
            SaveOutcome.Rejected("HTTP_${e.code()}")
        }
    }

    override suspend fun todayTotals(date: LocalDate): TodayTotals {
        val t = api.totals(date.toString(), date.toString())
        return TodayTotals(marked = t.totalDays, present = t.presentCount, absent = t.absentCount, late = t.lateCount)
    }

    override suspend fun studentAttendance(): StudentAttendance? = coroutineScope {
        val studentId = api.profile().student?.id ?: return@coroutineScope null
        val summary = async { api.summary(studentId) }
        val records = async { api.studentRecords(studentId, perPage = RECENT_RECORDS) }
        StudentAttendance(
            stats = summary.await().let { AttendanceStats(it.total, it.present, it.absent, it.late, it.excused) },
            records = records.await().data.map { it.toEntry() },
        )
    }

    override suspend fun guardianAttendance(): List<ChildAttendance> = coroutineScope {
        api.guardianChildren().data.map { child ->
            async {
                val summary = async { api.summary(child.id) }
                val records = async { api.childRecords(child.id, perPage = CHILD_RECORD_WINDOW) }
                ChildAttendance(
                    studentId = child.id,
                    name = listOfNotNull(child.givenName, child.familyName).joinToString(" "),
                    className = listOfNotNull(child.grade, child.section).joinToString(" · "),
                    stats = summary.await().let { AttendanceStats(it.total, it.present, it.absent, it.late, it.excused) },
                    recentAbsences = records.await().data
                        .filter { it.status in ABSENCE_STATUSES }
                        .take(3)
                        .map { it.toEntry() },
                )
            }
        }.awaitAll()
    }

    private fun SectionAttendanceResponse.toRoster() = SectionRoster(
        sectionId = sectionId,
        markedCount = marked,
        students = data.map { RosterStudent(it.studentId, it.studentName, MarkStatus.fromWire(it.status)) },
    )

    private fun AttendanceRecordDto.toEntry() = AttendanceEntry(
        id = id,
        date = parseDay(date),
        status = status,
        className = listOfNotNull(periodName, section).joinToString(" · ").ifEmpty { null },
    )

    companion object {
        const val SYNC_KIND = "attendance"
        const val RECENT_RECORDS = 10
        const val CHILD_RECORD_WINDOW = 50
        private val ABSENCE_STATUSES = setOf("ABSENT", "LATE", "EXCUSED")

        /** Current period first, then today's schedule by start time, then the rest by name. */
        val QUICK_ORDER: Comparator<QuickSection> = Comparator { a, b ->
            when {
                a.isCurrent != b.isCurrent -> if (a.isCurrent) -1 else 1
                a.scheduledToday != b.scheduledToday -> if (a.scheduledToday) -1 else 1
                a.periodStart != null && b.periodStart != null && a.periodStart != b.periodStart ->
                    a.periodStart.compareTo(b.periodStart)
                else -> a.name.compareTo(b.name)
            }
        }

        /** "HH:mm" in UTC from a `@db.Time` value ("1970-01-01T08:00:00.000Z"), or "08:00" as-is. */
        internal fun timeOfDay(value: String?): String? {
            if (value.isNullOrBlank()) return null
            return runCatching {
                val t = Instant.parse(value).atOffset(ZoneOffset.UTC).toLocalTime()
                hhmm(t.hour, t.minute)
            }.getOrElse { value.takeIf { it.length >= 5 && it[2] == ':' }?.take(5) }
        }

        /** Always Latin digits: these strings are compared, never shown. */
        private fun hhmm(hour: Int, minute: Int) = String.format(Locale.ROOT, "%02d:%02d", hour, minute)

        internal fun parseDay(value: String): LocalDate =
            runCatching { Instant.parse(value).atOffset(ZoneOffset.UTC).toLocalDate() }
                .getOrElse { LocalDate.parse(value.take(10)) }
    }
}
