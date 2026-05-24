package org.hogwarts.android.feature.guardian.data.repository

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.feature.guardian.domain.model.Child

/**
 * Repository interface for guardian data.
 */
interface GuardianRepository {
    fun observeChildren(): Flow<List<Child>>
    suspend fun getChildren(): List<Child>
    suspend fun getChild(childId: String): Child
    suspend fun getChildAttendance(childId: String, startDate: String? = null, endDate: String? = null): List<ChildAttendanceRecord>
    suspend fun getChildGrades(childId: String, termId: String? = null): List<ChildGradeRecord>
    suspend fun getChildFees(childId: String, status: String? = null): List<ChildFeeRecord>
    suspend fun getChildTimetable(childId: String): List<ChildTimetableSlot>
    suspend fun getChildTeachers(childId: String): List<ChildTeacher>
}

data class ChildAttendanceRecord(
    val id: String,
    val date: String,
    val status: String,
    val period: String? = null,
    val subject: String? = null,
    val note: String? = null,
    val markedBy: String? = null
)

data class ChildGradeRecord(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val assessmentName: String,
    val score: Float,
    val maxScore: Float,
    val percentage: Float,
    val gradeLetter: String? = null,
    val term: String? = null,
    val date: String? = null
)

data class ChildFeeRecord(
    val id: String,
    val name: String,
    val amount: Double,
    val dueDate: String,
    val status: String,
    val paidAmount: Double = 0.0,
    val paidDate: String? = null
)

data class ChildTimetableSlot(
    val id: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val subjectName: String,
    val teacherName: String,
    val roomNumber: String? = null
)

data class ChildTeacher(
    val id: String,
    val givenName: String,
    val familyName: String,
    val subject: String,
    val avatarUrl: String? = null,
    val unreadCount: Int = 0
) {
    val displayName: String get() = "$givenName $familyName"
}
