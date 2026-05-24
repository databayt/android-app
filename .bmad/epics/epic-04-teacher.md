# Epic 4: Teacher Module

**Epic ID:** EPIC-04
**Title:** Teacher Features
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Sprint:** 3

---

## 1. Overview

Implement teacher-specific features for viewing classes and students, with write operations for attendance and grades in v1.1.

### Business Value
- Teachers can view their assigned classes on mobile
- Enable future attendance marking on-the-go
- Reduce administrative overhead

### Success Criteria
- View assigned classes with roster
- View student grades (read-only in MVP)
- View today's teaching schedule
- Offline support for all read operations

---

## 2. Stories

### Story 4.1: View Assigned Classes
**Status:** Ready for Dev
**Points:** 3

**As a** teacher,
**I want** to see my assigned classes,
**So that** I know which classes I'm teaching.

**Acceptance Criteria:**
- [ ] List of classes with subject name
- [ ] Show grade/section
- [ ] Show number of students
- [ ] Show schedule (days/time)
- [ ] Offline support

**Tasks:**
1. Create ClassesListScreen composable
2. Create ClassesViewModel
3. Create ClassCard component
4. Implement GetTeacherClassesUseCase
5. Create ClassEntity and Dao
6. Navigate to class detail on tap

---

### Story 4.2: View Class Roster
**Status:** Ready for Dev
**Points:** 3

**As a** teacher,
**I want** to see the students in a class,
**So that** I know who I'm teaching.

**Acceptance Criteria:**
- [ ] List of students with photos
- [ ] Show student name
- [ ] Show attendance summary
- [ ] Search/filter students
- [ ] Navigate to student detail

**Tasks:**
1. Create ClassRosterScreen composable
2. Create StudentListItem component
3. Implement GetClassStudentsUseCase
4. Add search functionality
5. Cache students locally

---

### Story 4.3: View Student Grades (Read-Only)
**Status:** Ready for Dev
**Points:** 3

**As a** teacher,
**I want** to view a student's grades,
**So that** I can see their performance.

**Acceptance Criteria:**
- [ ] Show grades for my subject
- [ ] Show grade history
- [ ] Show class average comparison
- [ ] Read-only in MVP

**Tasks:**
1. Create StudentGradesScreen composable
2. Filter grades by teacher's subject
3. Calculate class statistics
4. Display comparison chart

---

### Story 4.4: Today's Teaching Schedule
**Status:** Ready for Dev
**Points:** 3

**As a** teacher,
**I want** to see today's teaching schedule,
**So that** I know my daily plan.

**Acceptance Criteria:**
- [ ] List of today's classes
- [ ] Show time, subject, room, grade
- [ ] Current/next class indicator
- [ ] Quick access to class roster

**Tasks:**
1. Create TeacherScheduleCard component
2. Filter schedule for today
3. Highlight current class
4. Add navigation to roster

---

### Story 4.5: Mark Attendance [POST-MVP]
**Status:** Backlog
**Points:** 8

**As a** teacher,
**I want** to mark attendance for a class,
**So that** attendance records are updated.

**Acceptance Criteria:**
- [ ] Select class and date
- [ ] Show student roster with attendance toggles
- [ ] Mark present/absent/late
- [ ] Add notes for individual students
- [ ] Submit attendance
- [ ] Offline queue if no network

---

### Story 4.6: Submit Grades [POST-MVP]
**Status:** Backlog
**Points:** 8

**As a** teacher,
**I want** to submit grades for students,
**So that** their academic records are updated.

**Acceptance Criteria:**
- [ ] Select class and grading period
- [ ] Enter grades for each student
- [ ] Validate grade format
- [ ] Submit grades
- [ ] Offline queue support

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Dashboard (Epic 2) | Not started |
| Student module (Epic 3) | Not started |
| API endpoints | Pending backend |

---

## 4. API Endpoints (Pending)

| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/mobile/teachers/{id}/classes | GET | Get assigned classes |
| /api/mobile/classes/{id}/students | GET | Get class roster |
| /api/mobile/teachers/{id}/schedule | GET | Get teaching schedule |

---

## 5. Technical Architecture

```
feature/teachers/
├── ui/
│   ├── classes-list-screen.kt
│   ├── class-roster-screen.kt
│   ├── student-grades-screen.kt
│   └── components/
│       ├── class-card.kt
│       └── teacher-schedule-card.kt
├── domain/
│   ├── model/
│   │   └── teacher-class.kt
│   └── usecase/
│       ├── get-teacher-classes-use-case.kt
│       └── get-class-students-use-case.kt
└── data/
    ├── repository/teacher-repository-impl.kt
    └── local/
        └── entity/teacher-class-entity.kt
```

---

## 6. Risks

| Risk | Mitigation |
|------|------------|
| Write operation failures | Robust offline queue |
| Attendance conflicts | Server-side conflict resolution |
| Large class rosters | Pagination |
