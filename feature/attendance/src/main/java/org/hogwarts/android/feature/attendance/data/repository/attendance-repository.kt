package org.hogwarts.android.feature.attendance.data.repository

import org.hogwarts.android.feature.attendance.domain.model.ChildAttendance
import org.hogwarts.android.feature.attendance.domain.model.QuickContext
import org.hogwarts.android.feature.attendance.domain.model.QuickMark
import org.hogwarts.android.feature.attendance.domain.model.SaveOutcome
import org.hogwarts.android.feature.attendance.domain.model.SectionRoster
import org.hogwarts.android.feature.attendance.domain.model.StudentAttendance
import org.hogwarts.android.feature.attendance.domain.model.TodayTotals
import java.time.LocalDate
import java.time.LocalTime

/**
 * The attendance landing's data, read straight from the hogwarts mobile API.
 * Reads throw on failure; only [submitQuick] turns a lost connection into an
 * outcome ([SaveOutcome.Queued]).
 */
interface AttendanceRepository {

    /** The teacher's sections for [today], current period first, with each one's marked count. */
    suspend fun quickContext(today: LocalDate, now: LocalTime): QuickContext

    suspend fun roster(sectionId: String, date: LocalDate): SectionRoster

    /** Online through `/offline/sync`; parked in the outbox when there is no connection. */
    suspend fun submitQuick(mark: QuickMark): SaveOutcome

    suspend fun todayTotals(date: LocalDate): TodayTotals

    /** The signed-in student's own figures and records, or null when the user has no student record. */
    suspend fun studentAttendance(): StudentAttendance?

    /** The signed-in guardian's children; empty when the user is not a guardian. */
    suspend fun guardianAttendance(): List<ChildAttendance>
}
