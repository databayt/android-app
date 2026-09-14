package org.hogwarts.android.feature.attendance.data

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.ResponseBody.Companion.toResponseBody
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.database.entity.OperationStatus
import org.hogwarts.android.core.database.entity.PendingOperationKinds
import org.hogwarts.android.feature.attendance.data.outbox.AttendanceOutbox
import org.hogwarts.android.feature.attendance.data.outbox.DrainResult
import org.hogwarts.android.feature.attendance.data.remote.dto.ClosureDto
import org.hogwarts.android.feature.attendance.data.remote.dto.DashboardTodayDto
import org.hogwarts.android.feature.attendance.data.remote.dto.OfflineSyncResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.OfflineSyncResultDto
import org.hogwarts.android.feature.attendance.data.remote.dto.SectionAttendanceResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.SectionStudentDto
import org.hogwarts.android.feature.attendance.data.remote.dto.TeacherClassDto
import org.hogwarts.android.feature.attendance.data.remote.dto.TeacherClassesResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.TeacherScheduleResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.TeacherSlotDto
import org.hogwarts.android.feature.attendance.data.remote.dto.TodayTimetableDto
import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepositoryImpl
import org.hogwarts.android.feature.attendance.domain.model.MarkStatus
import org.hogwarts.android.feature.attendance.domain.model.QuickMark
import org.hogwarts.android.feature.attendance.domain.model.QuickSummary
import org.hogwarts.android.feature.attendance.domain.model.SaveOutcome
import org.hogwarts.android.feature.attendance.testing.FakeAttendanceApi
import org.hogwarts.android.feature.attendance.testing.FakePendingOperationDao
import org.hogwarts.android.feature.attendance.testing.tenant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class AttendanceOfflineSaveTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val clock = Clock.fixed(Instant.parse("2026-09-14T08:30:00Z"), ZoneOffset.UTC)
    private val dao = FakePendingOperationDao()
    private var scheduled = 0
    private val outbox = AttendanceOutbox(dao, json) { scheduled++ }
    private val api = FakeAttendanceApi()
    private val repository = AttendanceRepositoryImpl(api, outbox, tenant(UserRole.TEACHER), json, clock)
    private val mark = QuickMark("sec-a", LocalDate.of(2026, 9, 14), listOf("s1"), listOf("s2"))

    @Test
    fun `online save returns the server summary and parks nothing`() = runTest {
        api.syncHandler = { req ->
            val item = req.items.single()
            OfflineSyncResponse(
                listOf(
                    OfflineSyncResultDto(
                        item.idempotencyKey,
                        "applied",
                        data = buildJsonObject {
                            put("total", 30); put("present", 28); put("absent", 1); put("late", 1); put("guardians_notified", 1)
                        },
                    ),
                ),
            )
        }
        val outcome = repository.submitQuick(mark)
        assertEquals(SaveOutcome.Saved(QuickSummary(30, 28, 1, 1, 1)), outcome)
        assertTrue(dao.rows.value.isEmpty())

        val item = api.syncRequests.single().items.single()
        assertEquals("attendance", item.kind)
        assertEquals("sec-a", item.payload.sectionId)
        assertEquals("2026-09-14", item.payload.date)
        assertEquals(listOf("s1"), item.payload.absentStudentIds)
        assertEquals(listOf("s2"), item.payload.lateStudentIds)
        assertEquals("2026-09-14T08:30:00Z", item.createdAt)
        // The server's pattern: ^[A-Za-z0-9_-]{8,64}$ — no colons in the key.
        assertTrue(Regex("^[A-Za-z0-9_-]{8,64}$").matches(item.idempotencyKey))
    }

    @Test
    fun `no connection parks one row per section and day and schedules the drain`() = runTest {
        api.syncHandler = { throw IOException("offline") }
        assertEquals(SaveOutcome.Queued, repository.submitQuick(mark))
        assertEquals(SaveOutcome.Queued, repository.submitQuick(mark.copy(absentStudentIds = emptyList())))

        val rows = dao.rows.value.values.toList()
        assertEquals(1, rows.size)
        assertEquals(PendingOperationKinds.ATTENDANCE_QUICK, rows.single().entityType)
        assertEquals("attendance:sec-a:2026-09-14", rows.single().entityId)
        assertEquals(OperationStatus.PENDING, rows.single().status)
        assertTrue("the re-mark replaced the parked one", rows.single().payload.contains("\"absent_student_ids\":[]"))
        assertEquals(2, scheduled)
    }

    @Test
    fun `a refusal is reported and never parked`() = runTest {
        api.syncHandler = { req -> OfflineSyncResponse(listOf(OfflineSyncResultDto(req.items.single().idempotencyKey, "rejected", code = "FORBIDDEN"))) }
        assertEquals(SaveOutcome.Rejected("FORBIDDEN"), repository.submitQuick(mark))
        api.syncHandler = { throw HttpException(Response.error<Any>(500, "".toResponseBody())) }
        assertEquals(SaveOutcome.Rejected("HTTP_500"), repository.submitQuick(mark))
        assertTrue(dao.rows.value.isEmpty())
    }

    @Test
    fun `drain deletes only what the server answered for`() = runTest {
        api.syncHandler = { throw IOException("offline") }
        repository.submitQuick(mark)
        repository.submitQuick(mark.copy(sectionId = "sec-b"))
        repository.submitQuick(mark.copy(sectionId = "sec-c"))
        val keys = dao.rows.value.values.associate { it.entityId to it.id }

        // Still offline: nothing leaves.
        assertEquals(DrainResult.RetryLater, outbox.drain(api))
        assertEquals(3, dao.getAllPending().size)

        api.syncHandler = {
            OfflineSyncResponse(
                listOf(
                    OfflineSyncResultDto(keys.getValue("attendance:sec-a:2026-09-14"), "applied"),
                    OfflineSyncResultDto(keys.getValue("attendance:sec-b:2026-09-14"), "duplicate"),
                    OfflineSyncResultDto(keys.getValue("attendance:sec-c:2026-09-14"), "rejected", code = "FORBIDDEN"),
                ),
            )
        }
        assertEquals(DrainResult.Done, outbox.drain(api))
        val left = dao.rows.value.values.single()
        assertEquals("attendance:sec-c:2026-09-14", left.entityId)
        assertEquals(OperationStatus.FAILED, left.status)
        assertEquals("FORBIDDEN", left.lastError)
    }

    @Test
    fun `quick context joins today's slots to sections and orders the current period first`() = runTest {
        api.classes = TeacherClassesResponse(
            listOf(
                TeacherClassDto("sec-b", "B", "Grade 5", "Math", 2),
                TeacherClassDto("sec-a", "A", "Grade 5", "Math", 2),
                TeacherClassDto("sec-a", "A", "Grade 5", "Science", 2),
                TeacherClassDto("sec-c", "C", "Grade 6", null, 1),
            ),
        )
        api.schedule = TeacherScheduleResponse(
            listOf(
                TeacherSlotDto("t1", 1, "B", "Grade 5", "P1", "1970-01-01T07:30:00.000Z", "1970-01-01T08:15:00.000Z"),
                TeacherSlotDto("t2", 1, "A", "Grade 5", "P2", "1970-01-01T08:20:00.000Z", "1970-01-01T09:00:00.000Z"),
            ),
        )
        api.dashboardToday = DashboardTodayDto(TodayTimetableDto(dayOfWeek = 1, closure = ClosureDto("Holiday", "HOLIDAY")))
        api.sections["sec-a"] = SectionAttendanceResponse(
            "2026-09-14", "sec-a", 2, marked = 2,
            data = listOf(SectionStudentDto("s1", "Amal", status = "ABSENT"), SectionStudentDto("s2", "Bashir", status = null)),
        )
        api.sections["sec-b"] = SectionAttendanceResponse("2026-09-14", "sec-b", 2, marked = 0)

        val ctx = repository.quickContext(LocalDate.of(2026, 9, 14), LocalTime.of(8, 30))
        assertEquals(listOf("sec-a", "sec-b", "sec-c"), ctx.sections.map { it.id })
        val a = ctx.sections[0]
        assertTrue(a.isCurrent)
        assertEquals("08:20", a.periodStart)
        assertTrue(a.fullyMarked)
        assertTrue(ctx.sections[1].scheduledToday)
        assertFalse(ctx.sections[1].isCurrent)
        assertFalse(ctx.sections[2].scheduledToday)
        assertFalse("a declared closure means no school today", ctx.isSchoolDay)
        assertEquals(listOf(MarkStatus.Absent, MarkStatus.Present), ctx.rosters.getValue("sec-a").students.map { it.status })
    }
}
