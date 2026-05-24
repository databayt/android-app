# Epic 3: Student Module

**Epic ID:** EPIC-03
**Title:** Student Features
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Sprint:** 2-3

---

## 1. Overview

Implement student-specific features for viewing grades, attendance, and schedule.

### Business Value
- Students can check their academic progress on-the-go
- Reduce calls to school administration
- Enable offline access to grades and schedule

### Success Criteria
- View grades by subject with details
- View attendance history and summary
- View class timetable
- All data available offline

---

## 2. Stories

### Story 3.1: Grades List
**Status:** Ready for Dev
**Points:** 5

**As a** student,
**I want** to see my grades for all subjects,
**So that** I can track my academic progress.

**Acceptance Criteria:**
- [ ] List of subjects with current grade
- [ ] Grade displayed as letter/number
- [ ] Pull-to-refresh for updates
- [ ] Loading skeleton while fetching
- [ ] Empty state if no grades
- [ ] Offline support with cached data

**Tasks:**
1. Create GradesListScreen composable
2. Create GradesViewModel
3. Create GradeItem component
4. Implement GetGradesUseCase
5. Create GradesRepository with offline-first
6. Create GradeEntity for Room
7. Create GradeDao

---

### Story 3.2: Grade Details
**Status:** Ready for Dev
**Points:** 3

**As a** student,
**I want** to see details of a grade,
**So that** I understand how I was evaluated.

**Acceptance Criteria:**
- [ ] Show subject name
- [ ] Show grade value and percentage
- [ ] Show teacher name
- [ ] Show date graded
- [ ] Show grading period
- [ ] Show comments (if any)

**Tasks:**
1. Create GradeDetailScreen composable
2. Add navigation with grade ID parameter
3. Load grade details from repository
4. Display formatted grade information

---

### Story 3.3: Attendance Summary
**Status:** Ready for Dev
**Points:** 5

**As a** student,
**I want** to see my attendance summary,
**So that** I know my attendance percentage.

**Acceptance Criteria:**
- [ ] Total days present/absent/late
- [ ] Attendance percentage
- [ ] Visual progress indicator
- [ ] Filter by date range
- [ ] List of recent attendance records
- [ ] Offline support

**Tasks:**
1. Create AttendanceScreen composable
2. Create AttendanceViewModel
3. Create AttendanceSummaryCard component
4. Create AttendanceRecordItem component
5. Implement GetAttendanceUseCase
6. Create AttendanceRepository
7. Create AttendanceEntity and Dao

---

### Story 3.4: Attendance History
**Status:** Ready for Dev
**Points:** 3

**As a** student,
**I want** to see my attendance history,
**So that** I can review specific days.

**Acceptance Criteria:**
- [ ] Calendar view of attendance
- [ ] Color-coded days (present/absent/late)
- [ ] Tap day to see details
- [ ] Filter by month

**Tasks:**
1. Create AttendanceCalendar component
2. Implement calendar logic
3. Color-code attendance status
4. Add month navigation

---

### Story 3.5: Class Schedule / Timetable
**Status:** Ready for Dev
**Points:** 5

**As a** student,
**I want** to see my class schedule,
**So that** I know where to be and when.

**Acceptance Criteria:**
- [ ] Weekly timetable view
- [ ] Today's classes highlighted
- [ ] Show subject, teacher, room, time
- [ ] Switch between days
- [ ] Current class indicator
- [ ] Offline support

**Tasks:**
1. Create TimetableScreen composable
2. Create TimetableViewModel
3. Create ClassSlot component
4. Create DaySelector component
5. Implement GetTimetableUseCase
6. Create TimetableRepository
7. Create TimetableEntity and Dao

---

### Story 3.6: Today's Schedule Widget
**Status:** Ready for Dev
**Points:** 3

**As a** student,
**I want** to see today's classes on dashboard,
**So that** I have a quick overview.

**Acceptance Criteria:**
- [ ] Compact list of today's classes
- [ ] Current/next class highlighted
- [ ] Time and room shown
- [ ] Tap to see full timetable

**Tasks:**
1. Create TodayScheduleCard component
2. Filter timetable for today
3. Highlight current class
4. Add navigation to full timetable

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Dashboard (Epic 2) | Not started |
| Room database | Configured |
| Retrofit API | Configured |
| Multi-tenant context | Done |

---

## 4. API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/mobile/students/{id}/grades | GET | Get student grades |
| /api/mobile/students/{id}/attendance | GET | Get attendance records |
| /api/mobile/students/{id}/timetable | GET | Get class schedule |

---

## 5. Technical Architecture

```
feature/grades/
├── ui/
│   ├── grades-list-screen.kt
│   ├── grade-detail-screen.kt
│   ├── grades-view-model.kt
│   └── components/
│       └── grade-item.kt
├── domain/
│   ├── model/grade.kt
│   └── usecase/get-grades-use-case.kt
├── data/
│   ├── repository/grades-repository-impl.kt
│   ├── remote/
│   │   ├── grades-api.kt
│   │   └── dto/grade-dto.kt
│   └── local/
│       ├── grade-dao.kt
│       └── entity/grade-entity.kt
└── navigation/grades-nav-graph.kt

feature/attendance/
└── [similar structure]

feature/timetable/
└── [similar structure]
```

---

## 6. Data Models

### Grade Entity
```kotlin
@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val studentId: String,
    val subjectId: String,
    val subjectName: String,
    val value: Double,
    val letterGrade: String?,
    val teacherName: String,
    val gradingPeriod: String,
    val gradedAt: Long,
    val comments: String?,
    val lastSynced: Long
)
```

### Attendance Entity
```kotlin
@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val studentId: String,
    val date: Long,
    val status: String, // PRESENT, ABSENT, LATE, EXCUSED
    val notes: String?,
    val markedBy: String,
    val lastSynced: Long
)
```

---

## 7. Risks

| Risk | Mitigation |
|------|------------|
| Large grade history | Paginate API, limit cache |
| Timetable changes | Background sync with WorkManager |
| Date/timezone issues | Use UTC, display in local |
