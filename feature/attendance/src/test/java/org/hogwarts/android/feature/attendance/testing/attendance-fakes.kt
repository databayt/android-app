package org.hogwarts.android.feature.attendance.testing

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.hogwarts.android.core.data.tenant.CurrentUser
import org.hogwarts.android.core.data.tenant.SessionManager
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.database.dao.PendingOperationDao
import org.hogwarts.android.core.database.entity.OperationStatus
import org.hogwarts.android.core.database.entity.PendingOperationEntity
import org.hogwarts.android.feature.attendance.data.remote.AttendanceApi
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceRecordsResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceSummaryDto
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceTotalsDto
import org.hogwarts.android.feature.attendance.data.remote.dto.DashboardTodayDto
import org.hogwarts.android.feature.attendance.data.remote.dto.GuardianChildrenResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.OfflineSyncRequest
import org.hogwarts.android.feature.attendance.data.remote.dto.OfflineSyncResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.ProfileDto
import org.hogwarts.android.feature.attendance.data.remote.dto.SectionAttendanceResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.TeacherClassesResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.TeacherScheduleResponse
import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepository
import org.hogwarts.android.feature.attendance.domain.model.ChildAttendance
import org.hogwarts.android.feature.attendance.domain.model.MarkStatus
import org.hogwarts.android.feature.attendance.domain.model.QuickContext
import org.hogwarts.android.feature.attendance.domain.model.QuickMark
import org.hogwarts.android.feature.attendance.domain.model.QuickSection
import org.hogwarts.android.feature.attendance.domain.model.RosterStudent
import org.hogwarts.android.feature.attendance.domain.model.SaveOutcome
import org.hogwarts.android.feature.attendance.domain.model.SectionRoster
import org.hogwarts.android.feature.attendance.domain.model.StudentAttendance
import org.hogwarts.android.feature.attendance.domain.model.TodayTotals
import java.time.LocalDate
import java.time.LocalTime

fun section(id: String, name: String, students: Int = 3, marked: Int = 0, current: Boolean = false, start: String? = null) =
    QuickSection(
        id = id,
        name = name,
        gradeName = "Grade 5",
        studentCount = students,
        markedCount = marked,
        scheduledToday = start != null,
        periodName = start?.let { "Period 1" },
        periodStart = start,
        periodEnd = start?.let { "09:00" },
        isCurrent = current,
    )

fun roster(sectionId: String, vararg names: String) = SectionRoster(
    sectionId = sectionId,
    markedCount = 0,
    students = names.mapIndexed { i, n -> RosterStudent("$sectionId-s$i", n, MarkStatus.Present) },
)

class FakeAttendanceRepository : AttendanceRepository {
    var context: QuickContext? = null
    var contextError: Exception? = null
    val rosters = mutableMapOf<String, SectionRoster>()
    val submitted = mutableListOf<QuickMark>()
    var outcome: SaveOutcome = SaveOutcome.Queued
    /** When set, a save waits here so a test can observe the in-flight state. */
    var saveGate: CompletableDeferred<Unit>? = null
    var totals: TodayTotals? = null
    var totalsError: Exception? = null
    var student: StudentAttendance? = null
    var children: List<ChildAttendance> = emptyList()
    var guardianError: Exception? = null

    override suspend fun quickContext(today: LocalDate, now: LocalTime): QuickContext {
        contextError?.let { throw it }
        return context ?: QuickContext(today, true, emptyList(), emptyMap())
    }

    override suspend fun roster(sectionId: String, date: LocalDate): SectionRoster =
        rosters[sectionId] ?: SectionRoster(sectionId, 0, emptyList())

    override suspend fun submitQuick(mark: QuickMark): SaveOutcome {
        submitted += mark
        saveGate?.await()
        return outcome
    }

    override suspend fun todayTotals(date: LocalDate): TodayTotals {
        totalsError?.let { throw it }
        return totals ?: TodayTotals(0, 0, 0, 0)
    }

    override suspend fun studentAttendance(): StudentAttendance? = student

    override suspend fun guardianAttendance(): List<ChildAttendance> {
        guardianError?.let { throw it }
        return children
    }
}

class FakeSessionManager(role: UserRole) : SessionManager {
    override var currentUser: CurrentUser? = CurrentUser("u1", "u1@school.test", "school-1", role, "Test", "User")
    override val isAuthenticated: Boolean get() = currentUser != null
    override suspend fun setUser(user: CurrentUser) { currentUser = user }
    override suspend fun clearSession() { currentUser = null }
}

fun tenant(role: UserRole) = TenantContext(FakeSessionManager(role))

/** An in-memory `pending_operations` table. */
class FakePendingOperationDao : PendingOperationDao {
    val rows = MutableStateFlow<Map<String, PendingOperationEntity>>(emptyMap())
    private fun ordered() = rows.value.values.sortedBy { it.createdAt }

    override suspend fun getOperationsByStatus(status: OperationStatus) = ordered().filter { it.status == status }
    override suspend fun getAllPending() = ordered().filter { it.status == OperationStatus.PENDING }
    override fun observePendingCount(status: OperationStatus): Flow<Int> = rows.map { m -> m.values.count { it.status == status } }
    override fun observeOperations(statuses: List<OperationStatus>): Flow<List<PendingOperationEntity>> =
        rows.map { m -> m.values.filter { it.status in statuses } }
    override suspend fun getOperationById(id: String) = rows.value[id]
    override suspend fun getOperationsForEntity(entityType: String, entityId: String) =
        ordered().filter { it.entityType == entityType && it.entityId == entityId }
    override suspend fun insertOperation(operation: PendingOperationEntity) { rows.value = rows.value + (operation.id to operation) }
    override suspend fun updateOperation(operation: PendingOperationEntity) = insertOperation(operation)
    override suspend fun markCompleted(id: String) = update(id) { it.copy(status = OperationStatus.COMPLETED) }
    override suspend fun markFailed(id: String, error: String) = update(id) { it.copy(status = OperationStatus.FAILED, lastError = error) }
    override suspend fun markProcessing(id: String) = update(id) { it.copy(status = OperationStatus.PROCESSING) }
    override suspend fun deleteOperation(id: String) { rows.value = rows.value - id }
    override suspend fun deleteByStatus(status: OperationStatus) { rows.value = rows.value.filterValues { it.status != status } }
    override suspend fun deleteStaleOperations(status: OperationStatus, before: Long) = Unit
    override suspend fun clearAll() { rows.value = emptyMap() }

    private fun update(id: String, change: (PendingOperationEntity) -> PendingOperationEntity) {
        rows.value[id]?.let { rows.value = rows.value + (id to change(it)) }
    }
}

/** Only the calls a test sets up answer; the rest fail loudly. */
open class FakeAttendanceApi : AttendanceApi {
    var syncHandler: suspend (OfflineSyncRequest) -> OfflineSyncResponse = { error("offlineSync not stubbed") }
    val syncRequests = mutableListOf<OfflineSyncRequest>()
    var classes = TeacherClassesResponse()
    var schedule = TeacherScheduleResponse()
    var dashboardToday = DashboardTodayDto()
    val sections = mutableMapOf<String, SectionAttendanceResponse>()

    override suspend fun teacherClasses() = classes
    override suspend fun teacherSchedule(day: Int) = schedule
    override suspend fun dashboard() = dashboardToday
    override suspend fun sectionAttendance(sectionId: String, date: String) = sections[sectionId] ?: error("no roster for $sectionId")
    override suspend fun offlineSync(body: OfflineSyncRequest): OfflineSyncResponse {
        syncRequests += body
        return syncHandler(body)
    }
    override suspend fun totals(startDate: String, endDate: String): AttendanceTotalsDto = error("not stubbed")
    override suspend fun profile(): ProfileDto = error("not stubbed")
    override suspend fun summary(studentId: String): AttendanceSummaryDto = error("not stubbed")
    override suspend fun studentRecords(studentId: String, perPage: Int): AttendanceRecordsResponse = error("not stubbed")
    override suspend fun guardianChildren(): GuardianChildrenResponse = error("not stubbed")
    override suspend fun childRecords(childId: String, perPage: Int): AttendanceRecordsResponse = error("not stubbed")
}
