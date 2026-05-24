# Epic 5: Guardian Module

## Overview

Implement guardian-facing features for monitoring children's academic progress.

## Goals

- View list of children
- Monitor child's grades and attendance
- Receive attendance alerts
- View fee statements

## Stories

### 5.1 Children List
**Status**: Not Started

**Acceptance Criteria**:
- [ ] List of guardian's children
- [ ] Child avatar and name
- [ ] Quick stats per child (grade, attendance %)
- [ ] Select child to view details
- [ ] Cached for offline
- [ ] Empty state for no children

**API Endpoint**: `GET /api/guardians/{id}/children`

**Files**:
- `feature/dashboard/ui/guardian/ChildrenListScreen.kt`
- `feature/dashboard/ui/guardian/ChildrenListViewModel.kt`
- `feature/dashboard/ui/components/ChildCard.kt`
- `feature/dashboard/domain/model/Child.kt`
- `feature/dashboard/domain/usecase/GetChildrenUseCase.kt`
- `feature/dashboard/data/repository/GuardianRepositoryImpl.kt`
- `feature/dashboard/data/remote/GuardianApi.kt`
- `feature/dashboard/data/local/ChildDao.kt`

### 5.2 Child Progress View
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Selected child's full profile
- [ ] Grades summary by subject
- [ ] Attendance summary (calendar/stats)
- [ ] Recent notifications
- [ ] Teacher contact info
- [ ] Cached for offline
- [ ] Tab navigation (Grades, Attendance, Fees)

**Files**:
- `feature/dashboard/ui/guardian/ChildProgressScreen.kt`
- `feature/dashboard/ui/guardian/ChildProgressViewModel.kt`
- `feature/dashboard/ui/components/ProgressTabs.kt`
- `feature/dashboard/ui/components/GradeSummaryCard.kt`
- `feature/dashboard/ui/components/AttendanceSummaryCard.kt`
- `feature/dashboard/domain/usecase/GetChildProgressUseCase.kt`

### 5.3 Attendance Notifications
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Alert when child marked absent
- [ ] Alert when child marked late
- [ ] Push notification support
- [ ] In-app notification list
- [ ] Notification preferences
- [ ] Mark as read

**Files**:
- `feature/messaging/ui/NotificationCenterScreen.kt`
- `feature/messaging/ui/NotificationCenterViewModel.kt`
- `feature/messaging/ui/components/NotificationCard.kt`
- `feature/messaging/domain/model/Notification.kt`
- `feature/messaging/domain/usecase/GetNotificationsUseCase.kt`
- `feature/messaging/data/repository/NotificationRepositoryImpl.kt`

## Dependencies

- Epic 1 (Authentication)
- Epic 2 (Dashboard & Navigation)
- Epic 3 (Student Module - reuses grade/attendance views)

## Technical Notes

### Child Data Model

```kotlin
data class Child(
    val id: String,
    val studentId: String,
    val schoolId: String,
    val givenName: String,
    val familyName: String,
    val avatarUrl: String?,
    val className: String,
    val gradeAverage: Double?,
    val attendancePercentage: Double?
)
```

### Guardian Relationship

```kotlin
// Guardian can have multiple children
// Each child is a Student record
// Linked via guardian_student relation table
```

### Notification Types

```kotlin
enum class NotificationType {
    ATTENDANCE_ABSENT,
    ATTENDANCE_LATE,
    GRADE_POSTED,
    FEE_DUE,
    ANNOUNCEMENT,
    MESSAGE
}
```

### Progress Summary

```kotlin
data class ChildProgress(
    val child: Child,
    val recentGrades: List<Grade>,
    val attendanceSummary: AttendanceSummary,
    val pendingFees: List<Fee>,
    val recentNotifications: List<Notification>
)

data class AttendanceSummary(
    val totalDays: Int,
    val presentDays: Int,
    val absentDays: Int,
    val lateDays: Int,
    val percentage: Double
)
```

## Risks

- Multiple children selection complexity
- Real-time notification delivery
- Data freshness for alerts
