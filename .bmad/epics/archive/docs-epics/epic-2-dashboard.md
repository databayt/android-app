# Epic 2: Core Dashboard & Navigation

## Overview

Implement role-based navigation and dashboard screens for all user types.

## Goals

- Type-safe navigation setup
- Role-specific dashboards (Student, Teacher, Guardian, Admin)
- Quick action cards
- Notification summary

## Stories

### 2.1 Navigation Setup
**Status**: Not Started

**Acceptance Criteria**:
- [ ] NavHost with type-safe routes
- [ ] Nested navigation for features
- [ ] Deep link support
- [ ] Back stack management
- [ ] Auth state routing

**Files**:
- `app/src/main/java/.../navigation/HogwartsNavHost.kt`
- `app/src/main/java/.../navigation/Screen.kt`
- `feature/*/navigation/*NavGraph.kt`

### 2.2 Student Dashboard
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Welcome message with student name
- [ ] Quick stats (grades, attendance %)
- [ ] Today's schedule preview
- [ ] Recent notifications
- [ ] Quick action cards
- [ ] Offline indicator
- [ ] RTL layout support

**Files**:
- `feature/dashboard/ui/student/StudentDashboardScreen.kt`
- `feature/dashboard/ui/student/StudentDashboardViewModel.kt`
- `feature/dashboard/ui/components/QuickStatsCard.kt`
- `feature/dashboard/ui/components/SchedulePreview.kt`

### 2.3 Teacher Dashboard
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Today's classes list
- [ ] Pending attendance marking
- [ ] Recent grade submissions
- [ ] Quick action: Mark Attendance
- [ ] Quick action: Submit Grades
- [ ] Offline indicator

**Files**:
- `feature/dashboard/ui/teacher/TeacherDashboardScreen.kt`
- `feature/dashboard/ui/teacher/TeacherDashboardViewModel.kt`
- `feature/dashboard/ui/components/ClassCard.kt`
- `feature/dashboard/ui/components/PendingTaskCard.kt`

### 2.4 Guardian Dashboard
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Children list with avatars
- [ ] Child selector (if multiple)
- [ ] Selected child's quick stats
- [ ] Attendance alerts
- [ ] Fee payment reminders
- [ ] Offline indicator

**Files**:
- `feature/dashboard/ui/guardian/GuardianDashboardScreen.kt`
- `feature/dashboard/ui/guardian/GuardianDashboardViewModel.kt`
- `feature/dashboard/ui/components/ChildCard.kt`
- `feature/dashboard/ui/components/AlertBanner.kt`

### 2.5 Admin Dashboard
**Status**: Not Started

**Acceptance Criteria**:
- [ ] School overview stats
- [ ] System alerts
- [ ] Quick links to admin functions
- [ ] Recent activity feed
- [ ] Offline indicator

**Files**:
- `feature/dashboard/ui/admin/AdminDashboardScreen.kt`
- `feature/dashboard/ui/admin/AdminDashboardViewModel.kt`
- `feature/dashboard/ui/components/StatsGrid.kt`

### 2.6 Role-Based Navigation
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Bottom navigation per role
- [ ] Role-specific menu items
- [ ] Drawer navigation (tablet)
- [ ] Route guards for permissions
- [ ] Smooth transitions

**Files**:
- `app/src/main/java/.../navigation/RoleBasedNavigation.kt`
- `core/designsystem/component/HogwartsBottomNav.kt`
- `core/designsystem/component/HogwartsDrawer.kt`

## Dependencies

- Epic 1 (Authentication) must be complete

## Technical Notes

### Navigation Structure

```
App
├── Auth Graph (unauthenticated)
│   └── Login
│
└── Main Graph (authenticated)
    ├── Dashboard (role-specific)
    ├── Students Graph
    ├── Attendance Graph
    ├── Grades Graph
    ├── Fees Graph
    ├── Messaging Graph
    └── Settings Graph
```

### Role Detection

```kotlin
val role = when (currentUser.role) {
    UserRole.STUDENT -> StudentDashboard
    UserRole.TEACHER -> TeacherDashboard
    UserRole.GUARDIAN -> GuardianDashboard
    UserRole.ADMIN -> AdminDashboard
}
```

## Risks

- Complex role-based routing logic
- Deep link handling with auth state
- Bottom nav state preservation
