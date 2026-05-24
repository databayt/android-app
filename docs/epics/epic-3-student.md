# Epic 3: Student Module

## Overview

Implement student-facing features for viewing academic information.

## Goals

- View grades by subject and term
- View attendance history
- View timetable/schedule
- View fee statements
- Offline caching for all views

## Stories

### 3.1 View Grades List
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Grades grouped by term
- [ ] Filter by subject
- [ ] Grade details (score, max, percentage)
- [ ] Teacher comments
- [ ] Cached for offline
- [ ] Pull-to-refresh
- [ ] Empty state

**API Endpoint**: `GET /api/grades?studentId={id}&schoolId={schoolId}`

**Files**:
- `feature/grades/ui/GradesScreen.kt`
- `feature/grades/ui/GradesViewModel.kt`
- `feature/grades/ui/components/GradeCard.kt`
- `feature/grades/ui/components/GradeFilter.kt`
- `feature/grades/domain/model/Grade.kt`
- `feature/grades/domain/usecase/GetGradesUseCase.kt`
- `feature/grades/data/repository/GradeRepositoryImpl.kt`
- `feature/grades/data/remote/GradeApi.kt`
- `feature/grades/data/local/GradeDao.kt`
- `feature/grades/data/local/entity/GradeEntity.kt`

### 3.2 View Attendance History
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Calendar view of attendance
- [ ] List view option
- [ ] Status indicators (Present, Absent, Late, Excused)
- [ ] Filter by date range
- [ ] Attendance percentage
- [ ] Cached for offline
- [ ] RTL calendar support

**API Endpoint**: `GET /api/attendance/student/{id}?startDate={}&endDate={}`

**Files**:
- `feature/attendance/ui/student/AttendanceHistoryScreen.kt`
- `feature/attendance/ui/student/AttendanceHistoryViewModel.kt`
- `feature/attendance/ui/components/AttendanceCalendar.kt`
- `feature/attendance/ui/components/AttendanceStatusBadge.kt`
- `feature/attendance/domain/model/Attendance.kt`
- `feature/attendance/domain/usecase/GetAttendanceHistoryUseCase.kt`
- `feature/attendance/data/repository/AttendanceRepositoryImpl.kt`
- `feature/attendance/data/local/AttendanceDao.kt`

### 3.3 View Timetable
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Weekly timetable view
- [ ] Daily view option
- [ ] Current class indicator
- [ ] Room/location info
- [ ] Teacher name
- [ ] Cached for offline
- [ ] RTL layout support

**API Endpoint**: `GET /api/timetable/student/{id}`

**Files**:
- `feature/timetable/ui/TimetableScreen.kt`
- `feature/timetable/ui/TimetableViewModel.kt`
- `feature/timetable/ui/components/TimetableGrid.kt`
- `feature/timetable/ui/components/ClassSlot.kt`
- `feature/timetable/domain/model/TimetableEntry.kt`
- `feature/timetable/domain/usecase/GetTimetableUseCase.kt`
- `feature/timetable/data/repository/TimetableRepositoryImpl.kt`

### 3.4 View Fee Statements
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Fee list with amounts
- [ ] Payment status (Paid, Pending, Overdue)
- [ ] Due dates
- [ ] Payment history
- [ ] Download receipt (PDF)
- [ ] Cached for offline

**API Endpoint**: `GET /api/fees/student/{id}`

**Files**:
- `feature/fees/ui/student/FeeStatementScreen.kt`
- `feature/fees/ui/student/FeeStatementViewModel.kt`
- `feature/fees/ui/components/FeeCard.kt`
- `feature/fees/ui/components/PaymentStatusBadge.kt`
- `feature/fees/domain/model/Fee.kt`
- `feature/fees/domain/usecase/GetFeesUseCase.kt`
- `feature/fees/data/repository/FeeRepositoryImpl.kt`

### 3.5 Offline Grade Caching
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Grades stored in Room
- [ ] Auto-refresh when online
- [ ] Last sync timestamp
- [ ] Stale data indicator
- [ ] Background sync via WorkManager

**Files**:
- `feature/grades/data/local/GradeDao.kt`
- `feature/grades/data/local/entity/GradeEntity.kt`
- `feature/grades/data/sync/GradeSyncWorker.kt`

## Dependencies

- Epic 1 (Authentication)
- Epic 2 (Dashboard & Navigation)

## Technical Notes

### Data Model: Grade

```kotlin
data class Grade(
    val id: String,
    val studentId: String,
    val schoolId: String,
    val subjectId: String,
    val subjectName: String,
    val examType: String,
    val score: Double,
    val maxScore: Double,
    val percentage: Double,
    val term: String,
    val teacherComment: String?,
    val gradedAt: Instant
)
```

### Attendance Status

```kotlin
enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED
}
```

## Risks

- Large data sets for grades history
- Calendar RTL complexity
- PDF download for receipts
