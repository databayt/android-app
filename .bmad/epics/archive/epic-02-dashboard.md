# Epic 2: Dashboard & Navigation

**Epic ID:** EPIC-02
**Title:** Dashboard & Navigation
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Sprint:** 2

---

## 1. Overview

Implement role-based dashboard and navigation structure that adapts to user type (Student, Teacher, Guardian, Admin).

### Business Value
- Provide quick access to most-used features
- Show personalized information per role
- Enable seamless navigation across features

### Success Criteria
- Dashboard shows role-appropriate content
- Bottom navigation with role-specific tabs
- Quick action cards for common tasks
- Offline indicator when disconnected

---

## 2. Stories

### Story 2.1: Role-Based Dashboard Layout
**Status:** Ready for Dev
**Points:** 5

**As a** user,
**I want** to see a dashboard tailored to my role,
**So that** I can quickly access relevant information.

**Acceptance Criteria:**
- [ ] Student dashboard: grades summary, attendance, today's schedule
- [ ] Teacher dashboard: today's classes, pending attendance, quick actions
- [ ] Guardian dashboard: children overview, notifications
- [ ] Admin dashboard: school stats, quick access to admin features

**Tasks:**
1. Create DashboardScreen composable
2. Create DashboardViewModel with role detection
3. Create DashboardUiState sealed class
4. Implement StudentDashboard content
5. Implement TeacherDashboard content
6. Implement GuardianDashboard content
7. Implement AdminDashboard content

---

### Story 2.2: Bottom Navigation
**Status:** Ready for Dev
**Points:** 3

**As a** user,
**I want** bottom navigation tabs,
**So that** I can switch between main sections.

**Acceptance Criteria:**
- [ ] Student: Home, Grades, Attendance, Schedule, Profile
- [ ] Teacher: Home, Classes, Grades, Messages, Profile
- [ ] Guardian: Home, Children, Fees, Messages, Profile
- [ ] Admin: Home, Users, Reports, Settings, Profile
- [ ] Active tab indicator
- [ ] Badge for notifications

**Tasks:**
1. Create HogwartsBottomNavigation composable
2. Define navigation items per role
3. Implement NavigationBar with Material 3
4. Add badge support for notification count
5. Integrate with Navigation Compose

---

### Story 2.3: Offline Indicator Banner
**Status:** Ready for Dev
**Points:** 2

**As a** user,
**I want** to know when I'm offline,
**So that** I understand data may be stale.

**Acceptance Criteria:**
- [ ] Banner appears when network unavailable
- [ ] Banner shows "You're offline - showing cached data"
- [ ] Banner dismisses when network restored
- [ ] Banner uses warning color scheme

**Tasks:**
1. Create OfflineBanner atom component
2. Create ConnectivityObserver utility
3. Emit connectivity state to ViewModel
4. Show/hide banner based on connectivity

---

### Story 2.4: Quick Action Cards
**Status:** Ready for Dev
**Points:** 3

**As a** user,
**I want** quick action cards on dashboard,
**So that** I can perform common tasks faster.

**Acceptance Criteria:**
- [ ] Student: View Today's Classes, Check Grades
- [ ] Teacher: Mark Attendance, Enter Grades
- [ ] Guardian: Pay Fees, Message Teacher
- [ ] Cards navigate to respective screens

**Tasks:**
1. Create QuickActionCard composable
2. Define actions per role
3. Implement navigation on tap
4. Add icons and descriptions

---

### Story 2.5: Profile Screen
**Status:** Ready for Dev
**Points:** 3

**As a** user,
**I want** to view my profile,
**So that** I can see my information and settings.

**Acceptance Criteria:**
- [ ] Show user avatar (or initials)
- [ ] Display name, email, role
- [ ] Display school name
- [ ] Settings link
- [ ] Logout button

**Tasks:**
1. Create ProfileScreen composable
2. Create ProfileViewModel
3. Load user data from TokenStorage
4. Add logout functionality
5. Add settings navigation

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Authentication (Epic 1) | Done |
| Navigation Compose | Configured |
| Material 3 theme | Configured |
| Connectivity manager | Not started |

---

## 4. Technical Architecture

```
feature/dashboard/
├── ui/
│   ├── dashboard-screen.kt
│   ├── dashboard-view-model.kt
│   ├── dashboard-ui-state.kt
│   └── components/
│       ├── student-dashboard.kt
│       ├── teacher-dashboard.kt
│       ├── guardian-dashboard.kt
│       ├── admin-dashboard.kt
│       └── quick-action-card.kt
└── navigation/
    └── dashboard-nav-graph.kt

core/designsystem/atom/
├── offline-banner.kt
└── hogwarts-bottom-navigation.kt

app/navigation/
└── hogwarts-nav-host.kt
```

---

## 5. UI Specifications

### Dashboard Layout
```
┌─────────────────────────────────┐
│ [Offline Banner - if needed]   │
├─────────────────────────────────┤
│ Welcome, {name}!               │
│ {role} at {school}             │
├─────────────────────────────────┤
│ ┌──────────┐ ┌──────────┐      │
│ │  Quick   │ │  Quick   │      │
│ │ Action 1 │ │ Action 2 │      │
│ └──────────┘ └──────────┘      │
├─────────────────────────────────┤
│ [Role-specific content]        │
│ - Grades summary               │
│ - Today's schedule             │
│ - Recent notifications         │
├─────────────────────────────────┤
│  🏠   📊   📅   💬   👤        │
│ Home Grade Sched Msg Profile   │
└─────────────────────────────────┘
```

---

## 6. Risks

| Risk | Mitigation |
|------|------------|
| Role data not available | Cache role in TokenStorage |
| Complex navigation state | Use Navigation Compose properly |
| Memory with multiple tabs | Use rememberSaveable |
