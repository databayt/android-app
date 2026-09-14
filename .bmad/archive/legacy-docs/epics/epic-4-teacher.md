# Epic 4: Teacher Module

## Overview

Implement teacher-facing features for class management and academic tasks.

## Goals

- View assigned classes
- Mark attendance (manual and QR)
- Submit student grades
- Offline attendance sync

## Stories

### 4.1 Class List View
**Status**: Not Started

**Acceptance Criteria**:
- [ ] List of assigned classes
- [ ] Class details (subject, students count, schedule)
- [ ] Today's classes highlighted
- [ ] Navigate to class details
- [ ] Search/filter classes
- [ ] Cached for offline

**API Endpoint**: `GET /api/classes/teacher/{id}`

**Files**:
- `feature/students/ui/teacher/ClassListScreen.kt`
- `feature/students/ui/teacher/ClassListViewModel.kt`
- `feature/students/ui/components/ClassCard.kt`
- `feature/students/domain/model/Class.kt`
- `feature/students/domain/usecase/GetTeacherClassesUseCase.kt`
- `feature/students/data/repository/ClassRepositoryImpl.kt`
- `feature/students/data/remote/ClassApi.kt`
- `feature/students/data/local/ClassDao.kt`

### 4.2 Mark Attendance (Manual)
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Student list for selected class
- [ ] Status toggle per student (Present/Absent/Late/Excused)
- [ ] Bulk actions (mark all present)
- [ ] Add notes/comments
- [ ] Submit confirmation
- [ ] Works offline (queued)
- [ ] Sync indicator

**API Endpoint**: `POST /api/attendance`

**Files**:
- `feature/attendance/ui/teacher/MarkAttendanceScreen.kt`
- `feature/attendance/ui/teacher/MarkAttendanceViewModel.kt`
- `feature/attendance/ui/components/StudentAttendanceRow.kt`
- `feature/attendance/ui/components/AttendanceStatusSelector.kt`
- `feature/attendance/domain/usecase/MarkAttendanceUseCase.kt`
- `feature/attendance/data/repository/AttendanceRepositoryImpl.kt`

### 4.3 Mark Attendance (QR Scan)
**Status**: Not Started

**Acceptance Criteria**:
- [ ] QR scanner using CameraX
- [ ] Decode student QR code
- [ ] Auto-mark as present
- [ ] Visual/audio feedback on scan
- [ ] Handle duplicate scans
- [ ] Batch submission
- [ ] Works offline (queued)

**Files**:
- `feature/attendance/ui/teacher/QrAttendanceScreen.kt`
- `feature/attendance/ui/teacher/QrAttendanceViewModel.kt`
- `feature/attendance/ui/components/QrScanner.kt`
- `feature/attendance/domain/usecase/ScanAttendanceUseCase.kt`
- `core/common/src/main/java/.../qr/QrDecoder.kt`

### 4.4 Submit Grades
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Select class and exam type
- [ ] Student list with grade input
- [ ] Max score configuration
- [ ] Add comments per student
- [ ] Validation (score <= max)
- [ ] Submit confirmation
- [ ] Works offline (queued)

**API Endpoint**: `POST /api/grades`

**Files**:
- `feature/grades/ui/teacher/SubmitGradesScreen.kt`
- `feature/grades/ui/teacher/SubmitGradesViewModel.kt`
- `feature/grades/ui/components/GradeInputRow.kt`
- `feature/grades/ui/components/ExamSelector.kt`
- `feature/grades/domain/usecase/SubmitGradesUseCase.kt`
- `feature/grades/domain/validation/GradeValidator.kt`

### 4.5 Offline Attendance Sync
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Queue attendance in PendingOperations
- [ ] WorkManager sync when online
- [ ] Conflict resolution (server wins)
- [ ] Retry failed syncs
- [ ] Sync status indicator
- [ ] Manual sync trigger

**Files**:
- `feature/attendance/data/sync/AttendanceSyncWorker.kt`
- `core/data/src/main/java/.../sync/SyncEngine.kt`
- `core/database/src/main/java/.../entity/PendingOperation.kt`

## Dependencies

- Epic 1 (Authentication)
- Epic 2 (Dashboard & Navigation)
- Epic 3 (Student Module - for shared models)

## Technical Notes

### Attendance Payload

```kotlin
data class MarkAttendanceRequest(
    val classId: String,
    val schoolId: String,
    val date: LocalDate,
    val records: List<AttendanceRecord>
)

data class AttendanceRecord(
    val studentId: String,
    val status: AttendanceStatus,
    val note: String?
)
```

### QR Code Format

```json
{
  "type": "STUDENT",
  "id": "student-123",
  "schoolId": "school-456"
}
```

### Grade Submission

```kotlin
data class SubmitGradesRequest(
    val classId: String,
    val schoolId: String,
    val examType: String,
    val maxScore: Double,
    val grades: List<GradeEntry>
)

data class GradeEntry(
    val studentId: String,
    val score: Double,
    val comment: String?
)
```

## Risks

- Camera permission handling
- QR scanning performance
- Offline queue size limits
- Conflict resolution complexity
